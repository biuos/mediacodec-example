package dai.android.media.encoder.muxer;

import android.media.MediaCodec;

import dai.android.media.encoder.StreamPublisher;


public abstract class AbstractBaseMuxer implements IMuxer {
    protected TimeIndexCounter videoTimeIndexCounter = new TimeIndexCounter();
    protected TimeIndexCounter audioTimeIndexCounter = new TimeIndexCounter();

    @Override
    public int open(StreamPublisher.Parameter parameter) {
        videoTimeIndexCounter.reset();
        audioTimeIndexCounter.reset();
        return 0;
    }

    @Override
    public void writeVideo(byte[] buffer, int offset, int length, MediaCodec.BufferInfo bufferInfo) {
        videoTimeIndexCounter.calcTotalTime(bufferInfo.presentationTimeUs);
    }

    @Override
    public void writeAudio(byte[] buffer, int offset, int length, MediaCodec.BufferInfo bufferInfo) {
        audioTimeIndexCounter.calcTotalTime(bufferInfo.presentationTimeUs);
    }

    @Override
    public int close() {
        return 0;
    }
}
