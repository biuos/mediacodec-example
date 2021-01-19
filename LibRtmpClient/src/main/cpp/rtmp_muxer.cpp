#include <jni.h>
#include <malloc.h>

extern "C" {
#include "librtmp/rtmp.h"
#include "flvmuxer/flv_rtmp.h"
}

#include "Logger.h"


extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_open(
    JNIEnv *env, jobject thiz,
    jstring url, jint video_width, jint video_height) {
    LOGD("RTMPMuxer_open: vw=%d vh=%d", video_width, video_height);
    const char *c_url = env->GetStringUTFChars(url, nullptr);

    int result = rtmp_open_for_write(c_url, video_width, video_height);
    if (result == TRUE) {
        LOGD("RTMPMuxer_open: success rtmp_open_for_write: %s", c_url);
    } else {
        LOGD("RTMPMuxer_open: failed rtmp_open_for_write: %s", c_url);
    }

    env->ReleaseStringUTFChars(url, c_url);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_writeVideo(
    JNIEnv *env, jobject thiz,
    jbyteArray data, jint offset, jint length, jlong timestamp) {
    jbyte  *c_data = env->GetByteArrayElements(data, nullptr);
    jint    result = rtmp_sender_write_video_frame(reinterpret_cast<uint8_t *>(&c_data[offset]), length, timestamp, 0, 0);
    env->ReleaseByteArrayElements(data, c_data, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_writeAudio(
    JNIEnv *env, jobject thiz,
    jbyteArray data, jint offset, jint length, jlong timestamp) {
    jbyte  *c_data = env->GetByteArrayElements(data, nullptr);
    jint    result = rtmp_sender_write_audio_frame(reinterpret_cast<uint8_t *>(&c_data[offset]), length, timestamp, 0);
    env->ReleaseByteArrayElements(data, c_data, JNI_ABORT);
    return result;
}


extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_read(
    JNIEnv *env, jobject thiz,
    jbyteArray data, jint offset, jint size) {
    auto *c_data    = (uint8_t *) malloc(size);
    int   readCount = rtmp_read_date(c_data, size);
    if (readCount > 0) {
        env->SetByteArrayRegion(data, offset, readCount, (const jbyte *) c_data);
    }
    free(c_data);
    return readCount;
}

extern "C" JNIEXPORT jint JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_close(JNIEnv *env, jobject thiz) {
    rtmp_close();
    return 0;
}

extern "C" JNIEXPORT void JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_writeFLVHeader(
    JNIEnv *env, jobject thiz, jboolean is_have_audio, jboolean is_have_video) {
    write_flv_header(is_have_audio, is_have_video);
}

extern "C" JNIEXPORT void JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_fileOpen(JNIEnv *env, jobject thiz, jstring filename) {
    const char *c_filename = env->GetStringUTFChars(filename, nullptr);
    flv_file_open(c_filename);
    env->ReleaseStringUTFChars(filename, c_filename);
}

extern "C" JNIEXPORT void JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_fileClose(JNIEnv *env, jobject thiz) {
    flv_file_close();
}

extern "C" JNIEXPORT jboolean JNICALL
Java_dai_android_media_protocol_rtmp_RTMPMuxer_isConnected(JNIEnv *env, jobject thiz) {
    return rtmp_is_connected() != 0;
}

