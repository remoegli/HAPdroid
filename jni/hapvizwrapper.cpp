#include <cstdio>
#include <set>
#include <jni.h>
#include <util.h>
#include <android/log.h>
#include <iostream>
#include <streambuf>
#include "ch_hsr_hapdroid_HAPvizLibrary.h"

#include <sys/socket.h>
#include <sys/un.h>
#include <errno.h>

#include "hapviz/IPv6_addr.h"
#include "hapviz/gfilter.h"
#include "hapviz/gfilter_cflow.h"
#include "hapviz/gimport.h"
#include "hapviz/ggraph.h"
#include "hapviz/zfstream.h"

#undef LOG_TAG
#define LOG_TAG "hapvizwrapper"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARNING, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

void read_file(const std::string & in_filename, const std::string & srvname,
		const IPv6_addr & local_net, const IPv6_addr & netmask);

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
	LOGI("JNI_OnLoad called");

	return JNI_VERSION_1_6;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *vm, void *reserved) {
	LOGI("JNI_OnUnload called");
}

/**
 * start definitions of the JNI binding functions
 *
 * Class:     ch_hsr_hapdroid_HAPvizLibrary
 * Method:    getTransactions
 * Signature: (Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Z
 */JNIEXPORT jboolean JNICALL Java_ch_hsr_hapdroid_HAPvizLibrary_getTransactions(
		JNIEnv * env, jclass cl, jstring in_file, jstring serv, jstring ip,
		jstring netmask) {
	const char * inf_char = env->GetStringUTFChars(in_file, 0);
	std::string inf(inf_char, env->GetStringLength(in_file));
	const char * local_server_sk_char = env->GetStringUTFChars(serv, 0);
	std::string local_server_sk(local_server_sk_char,
			env->GetStringLength(serv));
	const char * local_ip_char = env->GetStringUTFChars(ip, 0);
	std::string local_ip(local_ip_char, env->GetStringLength(ip));
	const char * local_netmask_char = env->GetStringUTFChars(ip, 0);
	std::string local_netmask(local_netmask_char,
			env->GetStringLength(netmask));

	IPv6_addr ip_c(local_ip);
	IPv6_addr netmask_c(local_netmask);
	read_file(inf, local_server_sk, ip_c, netmask_c);

	env->ReleaseStringUTFChars(netmask, local_netmask_char);
	env->ReleaseStringUTFChars(ip, local_ip_char);
	env->ReleaseStringUTFChars(serv, local_server_sk_char);
	env->ReleaseStringUTFChars(in_file, inf_char);
}

using namespace std;
namespace io = boost::iostreams;

/**
 *	Returns the number of bytes the uncompressed gz-file contains
 *
 *	\param in_filename Filestream for which the size gets looked up
 *
 *	\return Number of bytes
 */
uint32_t getUncompressedFileSize(ifstream & in_filestream) {
	uint32_t isize = 0; // By default, GZIP can just store up to 4GiBytes of data
	long old_pos = in_filestream.tellg();

	in_filestream.seekg(-4, ios::end);

	// read the (uncompressed) filesize
	in_filestream.read((char *) &isize, 4); // FIXME: big endian will freak out here
	in_filestream.seekg(old_pos);
	return isize;
}

void open_infile(ifstream & infs, const string & ifname) {
	infs.open(ifname.c_str(), ios::in | ios_base::binary);

	if (infs.fail()) {
		string error = "ERROR: Opening input file " + ifname + " failed.";
		throw error;
	}
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

void read_file(const string & in_filename, const string & srvname,
		const IPv6_addr & local_net, const IPv6_addr & netmask) {
	std::ifstream cflow_compressed_inputstream;
	uint32_t uncompressed_size = 0;

	try {
		open_infile(cflow_compressed_inputstream, in_filename);
	} catch (...) {
		string errormsg = "ERROR: check input file " + in_filename
				+ " and try again.";
		throw errormsg;
	}

	LOGD("getting uncompressed file size");
	uncompressed_size = getUncompressedFileSize(cflow_compressed_inputstream);

	LOGD("Check if this file can be a cflow_t-file");
	if (uncompressed_size % sizeof(cflow4) != 0) {
		string errortext = in_filename;
		errortext += " does not look like a cflow file (wrong size)";
		throw errortext;
	}

	//close the file for now
	cflow_compressed_inputstream.close();
	unsigned int flowcount = uncompressed_size / (sizeof(struct cflow4));

	LOGD("opening gzifstream");
	gzifstream infs;
	infs.open(in_filename.c_str(), std::ios_base::in);

	prefs_t prefs;
	prefs.summarize_biflows = true;
	prefs.summarize_clt_roles = true;
	prefs.summarize_multclt_roles = true;
	prefs.summarize_p2p_roles = true;
	prefs.summarize_srv_roles = true;
	prefs.summarize_uniflows = true;

	CImport import("something.gz", "something.hpg", prefs);
	LOGD("Reading input file");
	import.read_stream(infs, flowcount, local_net, netmask);
	infs.close();

	LOGD("creating local server connection");
	int sk = init_srv_conn(srvname.c_str());

	CRoleMembership roleMembership; // Manages groups of hosts having same role membership set
	CGraphlet * graphlet = new CGraphlet(cout, roleMembership);
	import.prepare_graphlet(graphlet, roleMembership);
	LOGD("write transactions");
	graphlet->write_transactions(cout);
	cout.flush();

	delete graphlet;
	close(sk);
}
