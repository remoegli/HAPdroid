# The ARMv7 is significanly faster due to the use of the hardware FPU
APP_ABI := armeabi-v7a
APP_PLATFORM := android-8

APP_STL := gnustl_static
APP_CPPFLAGS += -frtti -fexceptions -D_GLIBCXX_PERMIT_BACKWARD_HASH
