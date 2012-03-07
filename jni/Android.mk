LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)  
LOCAL_MODULE    := netcapture
LOCAL_SRC_FILES := netcapture.c  

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/libpcap   
LOCAL_STATIC_LIBRARIES := libpcap  
LOCAL_LDLIBS := -ldl -llog  

include $(BUILD_SHARED_LIBRARY)   
include $(NDK_ROOT)/external/libpcap/Android.mk  
