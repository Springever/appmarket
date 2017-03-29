LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := bz2

LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.c))

LOCAL_LDLIBS := -ldl -llog -lcutils -lutils

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(LOCAL_PATH) -type d))

LOCAL_CFLAGS := $(foreach D, $(APP_SUBDIRS), -I$(LOCAL_PATH)/$(D)) \
				-pthread

include $(BUILD_STATIC_LIBRARY)