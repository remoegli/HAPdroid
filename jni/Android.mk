LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)  
LOCAL_MODULE    := flowdump
LOCAL_SRC_FILES := flowdump.c  

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/libpcap   
LOCAL_STATIC_LIBRARIES := libpcap  
LOCAL_LDLIBS := -ldl -llog  

include $(BUILD_EXECUTABLE)   
include $(NDK_ROOT)/external/libpcap/Android.mk  
