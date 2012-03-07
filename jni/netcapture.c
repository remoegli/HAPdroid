#include <jni.h>  
#include <string.h>  
#include <android/log.h>  
#include <pcap.h>

#include "ch_hsr_hapdroid_NetworkCapture.h"

#undef LOG_TAG
#define LOG_TAG "libnetcapture"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARNING, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)



JNIEXPORT void JNICALL Java_ch_hsr_hapdroid_NetworkCapture_startCapture(JNIEnv * env, jclass cl)
{

}

JNIEXPORT jstring JNICALL Java_ch_hsr_hapdroid_NetworkCapture_getResultString(JNIEnv * env, jclass cl)
{
	char *dev, errbuf[PCAP_ERRBUF_SIZE];

	dev = pcap_lookupdev(errbuf);
	if (dev == NULL) {
		LOGE("Error in pcap_lookupdev: errbuf [%s]", errbuf);
		dev = "Error in initializing device\0";
	}
	
	LOGD("pcap_lookupdev: dev [%s]", dev);
	return (*env)->NewStringUTF(env, dev);
}

JNIEXPORT void JNICALL Java_ch_hsr_hapdroid_NetworkCapture_stopCapture(JNIEnv * env, jclass cl)
{

}

