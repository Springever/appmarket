LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := package

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(LOCAL_PATH) -type d))

LOCAL_CFLAGS := $(foreach D, $(APP_SUBDIRS), -I$(LOCAL_PATH)/$(D)) \
				-pthread \
				-fexceptions

LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.cpp))

LOCAL_C_INCLUDES += $(LOCAL_PATH)/../utils
LOCAL_ARM_MODE := arm
include $(BUILD_STATIC_LIBRARY)