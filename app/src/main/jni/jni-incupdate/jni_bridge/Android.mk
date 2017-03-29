LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := incupdate

APP_SUBDIRS := $(patsubst $(LOCAL_PATH)/%, %, $(shell find $(LOCAL_PATH) -type d))

LOCAL_CFLAGS := $(foreach D, $(APP_SUBDIRS), -I$(LOCAL_PATH)/$(D)) \
				-pthread \
				-fexceptions

LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.cpp))
LOCAL_SRC_FILES += $(notdir $(wildcard $(LOCAL_PATH)/*.c))
LOCAL_C_INCLUDES := $(LOCAL_PATH)/../bzip2		
						
LOCAL_STATIC_LIBRARIES := diff
LOCAL_STATIC_LIBRARIES += bz2
LOCAL_STATIC_LIBRARIES += package
LOCAL_ARM_MODE := arm
LOCAL_C_INCLUDES += $(LOCAL_PATH)/../utils

LOCAL_LDLIBS := -ldl -llog -lz 
	
include $(BUILD_SHARED_LIBRARY)