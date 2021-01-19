package dai.android.media.encoder.muxer;

import android.media.MediaCodec;

public class BufferInfoEx {
    private final MediaCodec.BufferInfo bufferInfo;
    private final int totalTime;

    public BufferInfoEx(MediaCodec.BufferInfo bufferInfo, int totalTime) {
        this.bufferInfo = bufferInfo;
        this.totalTime = totalTime;
    }

    public MediaCodec.BufferInfo getBufferInfo() {
        return bufferInfo;
    }

    public int getTotalTime() {
        return totalTime;
    }
}
