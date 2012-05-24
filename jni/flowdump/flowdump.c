#define APP_NAME		"flowdump"
#define APP_DESC		"Sniffer example using libpcap"
#define APP_DISCLAIMER	"THERE IS ABSOLUTELY NO WARRANTY FOR THIS PROGRAM."

#include <pcap.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <ctype.h>
#include <errno.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <signal.h>

/* default snap length (maximum bytes per packet to capture) */
#define SNAP_LEN 1518

/* ethernet headers are always exactly 14 bytes [1] */
#define SIZE_ETHERNET 14

/* Ethernet addresses are 6 bytes */
#define ETHER_ADDR_LEN	6

/* Ethernet header */
struct sniff_ethernet {
	u_char ether_dhost[ETHER_ADDR_LEN]; /* destination host address */
	u_char ether_shost[ETHER_ADDR_LEN]; /* source host address */
	u_short ether_type; /* IP? ARP? RARP? etc */
};

/* IP header */
struct sniff_ip {
	u_char ip_vhl; /* version << 4 | header length >> 2 */
	u_char ip_tos; /* type of service */
	u_short ip_len; /* total length */
	u_short ip_id; /* identification */
	u_short ip_off; /* fragment offset field */
#define IP_RF 0x8000            /* reserved fragment flag */
#define IP_DF 0x4000            /* dont fragment flag */
#define IP_MF 0x2000            /* more fragments flag */
#define IP_OFFMASK 0x1fff       /* mask for fragmenting bits */
	u_char ip_ttl; /* time to live */
	u_char ip_p; /* protocol */
	u_short ip_sum; /* checksum */
	struct in_addr ip_src, ip_dst; /* source and dest address */
};
#define IP_HL(ip)               (((ip)->ip_vhl) & 0x0f)
#define IP_V(ip)                (((ip)->ip_vhl) >> 4)

/* TCP header */
typedef u_int tcp_seq;

struct sniff_tcp {
	u_short th_sport; /* source port */
	u_short th_dport; /* destination port */
	tcp_seq th_seq; /* sequence number */
	tcp_seq th_ack; /* acknowledgement number */
	u_char th_offx2; /* data offset, rsvd */
#define TH_OFF(th)      (((th)->th_offx2 & 0xf0) >> 4)
	u_char th_flags;
#define TH_FIN  0x01
#define TH_SYN  0x02
#define TH_RST  0x04
#define TH_PUSH 0x08
#define TH_ACK  0x10
#define TH_URG  0x20
#define TH_ECE  0x40
#define TH_CWR  0x80
#define TH_FLAGS        (TH_FIN|TH_SYN|TH_RST|TH_ACK|TH_URG|TH_ECE|TH_CWR)
	u_short th_win; /* window */
	u_short th_sum; /* checksum */
	u_short th_urp; /* urgent pointer */
};

struct sniff_udp {
	u_short	uh_sport;		/* source port */
	u_short	uh_dport;		/* destination port */
	u_short	uh_ulen;		/* datagram length */
	u_short	uh_sum;			/* datagram checksum */
};

struct sniff_transp {
	u_short	h_sport;		/* source port */
	u_short	h_dport;		/* destination port */
};

int fd = 1;
char *dev = NULL; /* capture device name */

void print_usage(void) {

	printf("Usage: %s [-i interface|-p pcap-file] -s servername\n", APP_NAME);
	printf("\n");
	printf("Options:\n");
	printf("	-i interface    Listen on <interface> for packets.\n");
	printf("	-p pcap-file	Open pcap file for import.\n"
			"	-s servername	Connect to local server socket <servername> for output.\n");
	printf("\n");

	return;
}

void got_packet(u_char *args, const struct pcap_pkthdr *header,
		const u_char *packet) {

	/* declare pointers to packet headers */
	const struct sniff_ethernet *ethernet; /* The ethernet header [1] */
	const struct sniff_ip *ip; /* The IP header */
	const struct sniff_transp *transp; /* The transport protocol header */
	const struct sniff_tcp *tcp; /* The TCP protocol header */

	const struct timeval ts = (struct timeval) (header->ts);

	int size_ip;
	int size_transp;
	int size_payload;

	/* define ethernet header */
	ethernet = (struct sniff_ethernet*) (packet);

	/* define/compute ip header offset */
	ip = (struct sniff_ip*) (packet + SIZE_ETHERNET);
	size_ip = IP_HL(ip) * 4;
	if (size_ip < 20) {
		fprintf(stderr, "   * Invalid IP header length: %u bytes\n", size_ip);
	}

	u_char ip_p = ip->ip_p;
	u_char ip_tos = ip->ip_tos;

	size_transp = 0;
	switch(ip->ip_p) {
		case IPPROTO_TCP:
			tcp = (struct sniff_tcp*)(packet + SIZE_ETHERNET + size_ip);
			size_transp = TH_OFF(tcp)*4;
			break;
		case IPPROTO_UDP:
			size_transp = 8;
			break;
		case IPPROTO_ICMP:
			size_transp = 8;
			break;
		case IPPROTO_IP:
			break;
		default:
			printf("Protocol: unknown\n");
			return;
	}

	transp = (struct sniff_transp*) (packet + SIZE_ETHERNET + size_ip);

	/* get payload size */
	int size_header = size_ip + size_transp;
	size_payload = ntohs(ip->ip_len) - size_header;
//	size_payload = sizeof(packet);

	/*
	 * Print header data.
	 */
	fdprintf(fd, "%s:%d:", dev, ip_p);
	fdprintf(fd, "%s:%d", inet_ntoa(ip->ip_src), ntohs(transp->h_sport));
	fdprintf(fd, "-->");
	fdprintf(fd, "%s:%d", inet_ntoa(ip->ip_dst), ntohs(transp->h_dport));
	fdprintf(fd, ":%d:%d:%d:%ld,%06ld\n", ip_tos, size_header, size_payload, ts.tv_sec,
			ts.tv_usec);

	return;
}

int init_srv_conn(const char* srvname) {
	int sk, err;
	struct sockaddr_un addr;
	socklen_t len;
	addr.sun_family = AF_LOCAL;
	/* use abstract namespace for socket path */
	addr.sun_path[0] = '\0';
	strcpy(&addr.sun_path[1], srvname);
	len = offsetof(struct sockaddr_un, sun_path) + 1
			+ strlen(&addr.sun_path[1]);

	printf("flowdump : Before creating socket\n");
	sk = socket(PF_LOCAL, SOCK_STREAM, 0);
	if (sk < 0) {
		err = errno;
		fprintf(stderr, "Cannot open socket: %s (%s)\n", strerror(err), err);
		errno = err;
		return -1;
	}

	printf("flowdump : Before connecting to Java LocalSocketServer\n");
	if (connect(sk, (struct sockaddr *) &addr, len) < 0) {
		err = errno;
		fprintf(stderr, "connect() failed: %s (%s)\n", strerror(err), err);
		close(sk);
		errno = err;
		return -1;
	}

	printf("flowdump : Connecting to Java LocalSocketServer succeed\n");
	return sk;
}

struct bpf_program fp; /* compiled filter program (expression) */
pcap_t *handle; /* packet capture handle */

void cleanup(int sigtype) {
	pcap_freecode(&fp);
	pcap_close(handle);

	fdprintf(fd, "\nCapture complete.\n");
	close(fd);
	fprintf(stdout, "Device: %s capture process terminated", dev);
}

int main(int argc, char **argv) {
	char *srvname = NULL; /* local network server name */
	char errbuf[PCAP_ERRBUF_SIZE]; /* error buffer */
	char buffer[256];

	struct sigaction sa;
	sigset_t sigmask;

	/* set signal handlers */
	sigemptyset(&sigmask);
	sa.sa_handler = cleanup;
	sa.sa_mask = sigmask;
	sa.sa_flags = 0;
	sigaction(SIGPIPE, &sa, NULL);
	sigaction(SIGTERM, &sa, NULL);
	sigaction(SIGINT, &sa, NULL);

	char filter_exp[] = "ip"; /* filter expression [3] */
	bpf_u_int32 mask; /* subnet mask */
	bpf_u_int32 net; /* ip */
	int num_packets = -1; /* number of packets to capture */

	/* check for command line options */
	int c;
	while ((c = getopt(argc, argv, "i:p:s:h?")) != -1){
		switch (c) {
		case 'i':
			dev = optarg;
			/* open capture device */
			handle = pcap_open_live(dev, SNAP_LEN, 1, 1000, errbuf);
			if (!handle) {
				fprintf(stderr, "Couldn't open device %s: %s\n", dev, errbuf);
				exit(EXIT_FAILURE);
			}
			break;
		case 'p':
			dev = "pcap";
			handle = pcap_open_offline(optarg, errbuf);
			if (!handle) {
				fprintf(stderr, "Couldn't open file %s: %s\n", optarg, errbuf);
				exit(EXIT_FAILURE);
			}
			break;
		case 's':
			srvname = optarg;
			break;
		case 'h':
		case '?':
			fprintf(stderr, "\nerror: unrecognized command-line options\n\n");
			print_usage();
			exit(EXIT_FAILURE);
			break;
		default:
			abort();
			break;
		}
	}

	if(!srvname){
		fprintf(stderr, "missing option -s, using stdout.\n");
		fd = 1;
	} else {
		fd = init_srv_conn(srvname);
	}

	if (!dev) {
		/* find a capture device if not specified on command-line */
		dev = pcap_lookupdev(errbuf);
		if (dev == NULL) {
			fprintf(stderr, "Couldn't find default device: %s\n", errbuf);
			exit(EXIT_FAILURE);
		}
		/* open capture device */
		handle = pcap_open_live(dev, SNAP_LEN, 1, 1000, errbuf);
	}

	/* print capture info */
	fprintf(stdout, "Device: %s\n", dev);
	fprintf(stdout, "using filedescriptor: %i\n", fd);
	fprintf(stdout, "Number of packets: %d\n", num_packets);
	fprintf(stdout, "Filter expression: %s\n", filter_exp);

	/* compile the filter expression */
	if (pcap_compile(handle, &fp, filter_exp, 0, net) == -1) {
		fprintf(stderr, "Couldn't parse filter %s: %s\n", filter_exp,
				pcap_geterr(handle));
		exit(EXIT_FAILURE);
	}

	/* apply the compiled filter */
	if (pcap_setfilter(handle, &fp) == -1) {
		fprintf(stderr, "Couldn't install filter %s: %s\n", filter_exp,
				pcap_geterr(handle));
		exit(EXIT_FAILURE);
	}

	/* now we can set our callback function */
	pcap_loop(handle, num_packets, got_packet, NULL);

	return 0;
}
