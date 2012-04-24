LOCAL_PATH:= $(NDK_ROOT)/external/boost/libs/regex/src
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=\
	cpp_regex_traits.cpp\
	cregex.cpp\
	c_regex_traits.cpp\
	fileiter.cpp\
	icu.cpp\
	instances.cpp\
	posix_api.cpp\
	regex.cpp\
	regex_debug.cpp\
	regex_raw_buffer.cpp\
	regex_traits_defaults.cpp\
	static_mutex.cpp\
	usinstances.cpp\
	w32_regex_traits.cpp\
	wc_regex_traits.cpp\
	wide_posix_api.cpp\
	winstances.cpp\

LOCAL_C_INCLUDES := $(NDK_ROOT)/external/boost
LOCAL_MODULE:= boost_regex

include $(BUILD_SHARED_LIBRARY)
