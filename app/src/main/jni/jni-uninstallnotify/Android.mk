LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE :=uninstallnotify

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(LOCAL_PATH) -type d))

LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.c))

LOCAL_CFLAGS := $(foreach D, $(APP_SUBDIRS), -I$(LOCAL_PATH)/$(D)) \
				-I$(LOCAL_PATH)/../common \
				-I$(ANDROID_SRC)/frameworks/base/include \
				-I$(ANDROID_SRC)/frameworks/base/cmds/servicemanager \
				-I$(ANDROID_SRC)/system/core/libpixelflinger/tinyutils \
				-I$(ANDROID_SRC)/system/core/include \
				-I$(ANDROID_SRC)/development/ndk/platforms/android-3/include \
				-I$(ANDROID_SRC)/bionic \
				-pthread
				
LOCAL_LDLIBS := -ldl -llog

include $(BUILD_EXECUTABLE)