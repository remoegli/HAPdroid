LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := netdump.c
LOCAL_MODULE    := netdump

LOCAL_C_INCLUDES := $(MY_PATH)/external/libpcap   
LOCAL_LDLIBS :=  -ldl -llog  
LOCAL_STATIC_LIBRARIES := libpcap  

include $(BUILD_EXECUTABLE)
include $(MY_PATH)/external/libpcap/Android.mk
