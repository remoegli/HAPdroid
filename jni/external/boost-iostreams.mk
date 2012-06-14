LOCAL_PATH:= $(call my-dir)/boost/libs/iostreams/src
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=\
	file_descriptor.cpp\
	gzip.cpp\
	mapped_file.cpp\
	zlib.cpp\

LOCAL_C_INCLUDES := $(MY_PATH)/external/boost
LOCAL_MODULE := boost_iostreams
LOCAL_LDLIBS := -lz

include $(BUILD_SHARED_LIBRARY)
