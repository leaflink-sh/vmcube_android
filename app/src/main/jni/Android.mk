LOCAL_PATH  :=  $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    :=  ucloudJNI
LOCAL_SRC_FILES :=  ucloudJNI.c
include $(BUILD_SHARED_LIBRARY)
