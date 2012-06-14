LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

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

LOCAL_C_INCLUDES := $(MY_PATH)/external/boost
LOCAL_SHARED_LIBRARIES :=\
	boost_regex\
	boost_iostreams\
LOCAL_LDLIBS := -lz -llog

include $(BUILD_SHARED_LIBRARY)
include $(MY_PATH)/external/boost-iostreams.mk
include $(MY_PATH)/external/boost-regex.mk

