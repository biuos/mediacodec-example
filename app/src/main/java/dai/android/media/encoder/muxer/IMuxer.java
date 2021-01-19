package dai.android.media.encoder.muxer;

import android.media.MediaCodec;

import dai.android.media.encoder.StreamPublisher;

public interface IMuxer {

    int open(StreamPublisher.Parameter parameter);

    void writeVideo(byte[] buffer, int offset, int length, MediaCodec.BufferInfo bufferInfo);

    void writeAudio(byte[] buffer, int offset, int length, MediaCodec.BufferInfo bufferInfo);

    int close();
}
