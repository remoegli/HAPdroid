LOCAL_PATH := $(call my-dir)
MY_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := hapvizwrapper.cpp
LOCAL_MODULE := hapvizwrapper

LOCAL_C_INCLUDES :=\
	$(NDK_ROOT)/external/boost\
	hapviz
	
LOCAL_SHARED_LIBRARIES := hapviz
LOCAL_LDLIBS := -llog

include $(BUILD_SHARED_LIBRARY)
include $(MY_PATH)/hapviz/Android.mk
include jni/flowdump/Android.mk