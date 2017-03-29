LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := diff

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(LOCAL_PATH) -type d))

LOCAL_CFLAGS := $(foreach D, $(APP_SUBDIRS), -I$(LOCAL_PATH)/$(D)) \
				#-I$(LOCAL_PATH)/../bzip2 \
				-pthread

LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.c))
LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.cpp))
#LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/../android_libs/*.so))

LOCAL_C_INCLUDES := $(LOCAL_PATH)/../bzip2		
						
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../utils
LOCAL_ARM_MODE := arm
include $(BUILD_STATIC_LIBRARY)