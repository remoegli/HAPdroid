#ifndef ANDROID_LOG_H_
#define ANDROID_LOG_H_

#include <android/log.h>

#undef LOG_TAG
#define LOG_TAG "hapvizwrapper"

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARNING, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#endif /* ANDROID_LOG_H_ */
