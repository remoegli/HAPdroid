LOCAL_PATH:= $(NDK_ROOT)/external/boost/libs/iostreams/src
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=\
	file_descriptor.cpp\
	gzip.cpp\
	mapped_file.cpp\
	zlib.cpp

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/boost
LOCAL_LDLIBS := -lz
LOCAL_MODULE:= boost_iostreams

include $(BUILD_SHARED_LIBRARY)
