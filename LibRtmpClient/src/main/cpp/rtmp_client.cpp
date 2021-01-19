
#include <jni.h>
#include <malloc.h>

extern "C" {
#include "librtmp/rtmp.h"
}

#include "Logger.h"

static
jint throwIllegalStateException(JNIEnv *env, const char *message) {
    jclass exception = env->FindClass("java/lang/IllegalStateException");
    return env->ThrowNew(exception, message);
}

extern "C" JNIEXPORT jlong JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeAlloc(JNIEnv *env, jobject thiz) {
    LOGD("RTMPClient_nativeAlloc");
    RTMP *rtmp = RTMP_Alloc();
    return (jlong)rtmp;
}


extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeOpen(
    JNIEnv  *env, jobject thiz,
    jstring  url_,
    jboolean publish_mode,
    jlong    handler,
    jint     timeout_in_seconds) {
    const char *url = env->GetStringUTFChars(url_, nullptr);
    LOGD("RTMPClient_nativeOpen: url=%s", url);

    RTMP *rtmp = (RTMP *)handler;
    if(nullptr == rtmp) {
        throwIllegalStateException(env, "RTMP open called without allocating rtmp object");
        return FALSE;
    }

    RTMP_Init(rtmp);
    rtmp->Link.timeout = timeout_in_seconds;

    int ret = RTMP_SetupURL(rtmp, const_cast<char *>(url));
    if(ret != TRUE) {
        LOGE("RTMP_SetupURL: failed");
        RTMP_Free(rtmp);
        return ret;
    }

    if(publish_mode) {
        RTMP_EnableWrite(rtmp);
    }

    ret = RTMP_Connect(rtmp, nullptr);
    if(ret != TRUE) {
        LOGE("RTMP_Connect: failed");
        RTMP_Free(rtmp);
        return ret;
    }

    ret = RTMP_ConnectStream(rtmp, 0);
    if(ret != TRUE) {
        LOGE("RTMP_ConnectStream: failed");
        RTMP_Free(rtmp);
        return ret;
    }

    env->ReleaseStringUTFChars(url_, url);
    return TRUE;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeRead(
    JNIEnv *env, jobject thiz,
    jbyteArray data_, jint offset, jint size, jlong handler) {
    // LOGD("RTMPClient_nativeRead");
    RTMP *rtmp = (RTMP *)handler;
    if (rtmp == nullptr) {
        const char *message = "RTMP open function has to be called before read";
        LOGE("RTMPClient_nativeRead failed: %s", message);
        throwIllegalStateException(env, message);
        return FALSE;
    }

    int connected = RTMP_IsConnected(rtmp);
    if(!connected) {
        LOGE("RTMPClient_nativeRead failed: RTMP not connected");
        return FALSE;
    }

    char *data = static_cast<char *>(malloc(size));
    int readCount = RTMP_Read(rtmp, data, size);
    if(readCount > 0) {
        env->SetByteArrayRegion(data_, offset, readCount, reinterpret_cast<const jbyte *>(data));
    }
    free(data);
    return readCount;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeWrite(
    JNIEnv *env, jobject thiz,
    jbyteArray data, jint offset, jint size, jlong handler) {
    RTMP *rtmp = (RTMP *) handler;
    if (nullptr == rtmp) {
        const char *message = "RTMP open function has to be called before write";
        LOGE("RTMPClient_nativeWrite failed: %s", message);
        throwIllegalStateException(env, message);
        return FALSE;
    }

    int connected = RTMP_IsConnected(rtmp);
    if(!connected) {
        LOGE("RTMPClient_nativeWrite failed: RTMP not connected");
        return FALSE;
    }

    auto *buffer = static_cast<jbyte *>(malloc(size));
    env->GetByteArrayRegion(data, offset, size, buffer);
    int ret = RTMP_Write(rtmp, reinterpret_cast<const char *>(buffer), size);
    free(buffer);
    return  ret;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativePause(
    JNIEnv *env, jobject thiz, jboolean pause, jlong handler) {
    LOGD("RTMPClient_nativePause");
    RTMP *rtmp = (RTMP *)handler;
    if (nullptr == rtmp) {
        const char *message = "RTMP open function has to be called before write";
        LOGE("RTMPClient_nativeWrite failed: %s", message);
        throwIllegalStateException(env, message);
        return FALSE;
    }

    return RTMP_Pause(rtmp, pause);
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeIsConnected(
    JNIEnv *env, jobject thiz, jlong handler) {
    RTMP *rtmp = (RTMP *)handler;
    if(rtmp == nullptr) {
        return false;
    }

    int connected = RTMP_IsConnected(rtmp);
    return connected != 0;
}

extern "C" JNIEXPORT void JNICALL
Java_dai_android_media_protocol_rtmp_RTMPClient_nativeClose(
    JNIEnv *env, jobject thiz, jlong handler) {
    RTMP *rtmp = (RTMP *)handler;
    if(!rtmp) {
        RTMP_Close(rtmp);
        RTMP_Free(rtmp);
    }
}
