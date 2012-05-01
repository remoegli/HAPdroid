LOCAL_PATH:= $(NDK_ROOT)/external/boost/libs/thread/src
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=\
	tss_null.cpp\
	pthread/once.cpp\
	pthread/thread.cpp

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/boost
LOCAL_MODULE:= boost_thread

include $(BUILD_SHARED_LIBRARY)
