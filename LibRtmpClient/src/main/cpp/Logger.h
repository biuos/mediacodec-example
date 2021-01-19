#ifndef RTMP_CLIENT_INCLUDE_H
#define RTMP_CLIENT_INCLUDE_H

#include <android/log.h>

#define  ANDROID_RTMP_CLIENT_LOG_TAG "rtmp-jni"

#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG, ANDROID_RTMP_CLIENT_LOG_TAG, __VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, ANDROID_RTMP_CLIENT_LOG_TAG, __VA_ARGS__)

#endif//RTMP_CLIENT_INCLUDE_H
