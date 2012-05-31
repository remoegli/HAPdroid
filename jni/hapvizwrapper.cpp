#include <cstdio>
#include <set>
#include <jni.h>
#include <util.h>
#include <iostream>
#include <streambuf>
#include <boost/iostreams/device/file_descriptor.hpp>
#include <boost/iostreams/stream.hpp>
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

#include "hapviz/android_log.h"

void read_file(const std::string & in_filename, const std::string & srvname,
		const IPv6_addr & local_net, const IPv6_addr & netmask);
void read_buffer(uint8_t * buffer, unsigned int buf_length, std::string local_server_name);

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
 */
JNIEXPORT jboolean JNICALL Java_ch_hsr_hapdroid_HAPvizLibrary_getTransactions__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2(
		JNIEnv * env, jclass cl, jstring in_file, jstring serv, jstring ip,
		jstring netmask) {
	const char * inf_char = env->GetStringUTFChars(in_file, 0);
	const char * local_server_sk_char = env->GetStringUTFChars(serv, 0);
	const char * local_ip_char = env->GetStringUTFChars(ip, 0);
	const char * local_netmask_char = env->GetStringUTFChars(ip, 0);

	if(!inf_char || !local_ip_char || !local_netmask_char || !local_server_sk_char){
		return NULL;
	}

	IPv6_addr ip_c(local_ip_char);
	IPv6_addr netmask_c(local_netmask_char);
	read_file(inf_char, local_server_sk_char, ip_c, netmask_c);

	env->ReleaseStringUTFChars(netmask, local_netmask_char);
	env->ReleaseStringUTFChars(ip, local_ip_char);
	env->ReleaseStringUTFChars(serv, local_server_sk_char);
	env->ReleaseStringUTFChars(in_file, inf_char);

	return NULL;
}

JNIEXPORT jboolean JNICALL Java_ch_hsr_hapdroid_HAPvizLibrary_getTransactions___3BLjava_lang_String_2(
		JNIEnv * env, jclass jcl, jbyteArray jcflows, jstring jserv) {
	const char * local_server_sk_char = env->GetStringUTFChars(jserv, 0);
	std::string local_server_name(local_server_sk_char,
			env->GetStringLength(jserv));

	unsigned int length = env->GetArrayLength(jcflows);
	jbyte* data = env->GetByteArrayElements(jcflows, NULL);
	read_buffer((uint8_t*) data, length, local_server_name);

	env->ReleaseByteArrayElements(jcflows, data, JNI_ABORT);
	env->ReleaseStringUTFChars(jserv, local_server_sk_char);

	return NULL;
}

/**
 * ostream for file descriptors
 */
class fdoutbuf: public std::streambuf {
protected:
	int fd; // file descriptor
public:
	// constructor
	fdoutbuf(int _fd) :
			fd(_fd) {
	}
protected:
	// write one character
	virtual int_type overflow(int_type c) {
		if (c != EOF) {
			char z = c;
			if (write(fd, &z, 1) != 1) {
				return EOF;
			}
		}
		return c;
	}
	// write multiple characters
	virtual std::streamsize xsputn(const char* s, std::streamsize num) {
		return write(fd, s, num);
	}
};

class fdostream: public std::ostream {
protected:
	fdoutbuf buf;
public:
	fdostream(int fd) :
			std::ostream(0), buf(fd) {
		rdbuf(&buf);
	}
};

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

	LOGD("Before creating socket\n");
	sk = socket(PF_LOCAL, SOCK_STREAM, 0);
	if (sk < 0) {
		err = errno;
		LOGE("Cannot open socket: %s (%s)", strerror(err), err);
		errno = err;
		return -1;
	}

	LOGD("Before connecting to Java LocalSocketServer");
	if (connect(sk, (struct sockaddr *) &addr, len) < 0) {
		err = errno;
		LOGE("connect() failed: %s (%s)", strerror(err), err);
		close(sk);
		return -1;
	}

	LOGD("Connecting to Java LocalSocketServer succeed");
	return sk;
}

void read_stream(std::istream & ins, unsigned int flowcount, const string srvname,
		const IPv6_addr & local_net, const IPv6_addr & netmask) {
	prefs_t prefs;
	prefs.summarize_biflows = true;
	prefs.summarize_clt_roles = true;
	prefs.summarize_multclt_roles = true;
	prefs.summarize_p2p_roles = true;
	prefs.summarize_srv_roles = true;
	prefs.summarize_uniflows = true;

	CImport import("something.gz", "something.hpg", prefs);
	LOGD("Reading input stream");
	import.read_stream(ins, flowcount, local_net, netmask);

	LOGD("Creating local server connection");
	int sock_fd = init_srv_conn(srvname.c_str());
	if (sock_fd == -1){
		LOGE("Failed to create local server connection");
		return;
	}
	fdostream out(sock_fd);

	CRoleMembership roleMembership; // Manages groups of hosts having same role membership set
	CGraphlet * graphlet = new CGraphlet(out, roleMembership);
	import.prepare_graphlet(graphlet, roleMembership);
	LOGD("Write transactions");
	graphlet->write_transactions(out);
	out.flush();

	delete graphlet;
	close(sock_fd);
}

void read_file(const string & in_filename, const string & srvname,
		const IPv6_addr & local_net, const IPv6_addr & netmask) {
	std::ifstream cflow_compressed_inputstream;
	uint32_t uncompressed_size = 0;

	LOGD("opening input file");
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
	read_stream(infs, flowcount, srvname, local_net, netmask);
	infs.close();
}

void read_buffer(uint8_t * buffer, unsigned int buf_length, string srvname) {
	stringstream is;
	is.rdbuf()->pubsetbuf((char*)buffer, buf_length);
	unsigned int cflow_count = buf_length / 8;

	read_stream(is, cflow_count, srvname, IPv6_addr(), IPv6_addr());
}
