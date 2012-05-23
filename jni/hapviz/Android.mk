MY_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_PATH := $(MY_PATH)

LOCAL_SRC_FILES :=\
	cflow.cpp\
	gfilter.cpp\
	gfilter_cflow.cpp\
	ggraph.cpp\
	gimport.cpp\
	grole.cpp\
	gsummarynodeinfo.cpp\
	gutil.cpp\
	HashMap.cpp\
	HashMapE.cpp\
	heapsort.cpp\
	IPv6_addr.cpp\
	zfstream.cpp\
	lookup3.cpp

LOCAL_MODULE := hapviz

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/boost
LOCAL_SHARED_LIBRARIES :=\
	boost_iostreams\
	boost_regex
LOCAL_LDLIBS := -lz -llog

include $(BUILD_SHARED_LIBRARY)
include $(MY_PATH)/boost/iostreams.mk
include $(MY_PATH)/boost/thread.mk
include $(MY_PATH)/boost/regex.mk

