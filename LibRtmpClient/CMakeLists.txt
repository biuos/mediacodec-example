cmake_minimum_required(VERSION 3.6)


include_directories(src/main/cpp)

add_definitions(-DNO_CRYPTO)

add_library(rtmp-jni SHARED

        # Provides a relative path to your source file(s).
        src/main/cpp/librtmp/amf.c
        src/main/cpp/librtmp/amf.h
        src/main/cpp/librtmp/bytes.h
        src/main/cpp/librtmp/dh.h
        src/main/cpp/librtmp/dhgroups.h
        src/main/cpp/librtmp/handshake.h
        src/main/cpp/librtmp/hashswf.c
        src/main/cpp/librtmp/http.h
        src/main/cpp/librtmp/log.c
        src/main/cpp/librtmp/log.h
        src/main/cpp/librtmp/parseurl.c
        src/main/cpp/librtmp/rtmp.c
        src/main/cpp/librtmp/rtmp.h
        src/main/cpp/librtmp/rtmp_sys.h

        src/main/cpp/flvmuxer/flv_rtmp.h
        src/main/cpp/flvmuxer/flv_rtmp.c

        src/main/cpp/rtmp_muxer.cpp
        src/main/cpp/rtmp_client.cpp
        )

target_link_libraries(
        rtmp-jni
        log
)
