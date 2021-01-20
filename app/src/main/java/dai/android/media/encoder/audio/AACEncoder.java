package dai.android.media.encoder.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

import dai.android.media.encoder.MediaCodecInputStream;
import dai.android.media.encoder.StreamPublisher;
import dai.android.debug.Logger;

public class AACEncoder {

    private static final String TAG = "AACEncoder";

    private AudioRecord mAudioRecord;

    private final MediaCodec mMediaCodec;
    private final MediaCodecInputStream mediaCodecInputStream;
    private final int bufferSize;
    private final int samplingRate;

    private Thread mThread;
    private OnDataComingCallback onDataComingCallback;

    private boolean isStart;

    public AACEncoder(final StreamPublisher.Parameter params) throws IOException {
        samplingRate = params.samplingRate;

        bufferSize = params.audioBufferSize;
        mMediaCodec = MediaCodec.createEncoderByType(StreamPublisher.Parameter.audioMIMEType);
        mMediaCodec.configure(params.createAudioMediaFormat(), null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodecInputStream = new MediaCodecInputStream(mMediaCodec, new MediaCodecInputStream.MediaFormatCallback() {
            @Override
            public void onChangeMediaFormat(MediaFormat mediaFormat) {
                params.setAudioOutputMediaFormat(mediaFormat);
            }
        });
        mAudioRecord = new AudioRecord(params.audioSource, samplingRate, params.channelCfg, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor noiseSuppressor = NoiseSuppressor.create(mAudioRecord.getAudioSessionId());
        }

    }

    private final Runnable workRunnable = new Runnable() {
        @Override
        public void run() {
            final long startWhen = System.nanoTime();

            int len = 0, bufferIndex = -1;
            ByteBuffer inputBuffer = null;

            while (isStart && !Thread.interrupted()) {
                synchronized (mMediaCodec) {
                    if (!isStart) {
                        Logger.d(TAG, "Thread set not start");
                        return;
                    }
                    bufferIndex = mMediaCodec.dequeueInputBuffer(10000);
                    if (bufferIndex >= 0) {
                        inputBuffer = mMediaCodec.getInputBuffer(bufferIndex);
                        long presentationTimeNs = System.nanoTime();
                        len = mAudioRecord.read(inputBuffer, bufferSize);
                        presentationTimeNs -= (len / samplingRate) / 1000000000;

                        long presentationTimeUs = (presentationTimeNs - startWhen) / 1000;
                        if (len == AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
                            Logger.e(TAG, "An error happened when call the AudioRecord API.");
                        } else {
                            mMediaCodec.queueInputBuffer(bufferIndex, 0, len, presentationTimeUs, 0);
                            if (onDataComingCallback != null) {
                                onDataComingCallback.onComing();
                            }
                        }
                    }
                }
            }
        }
    };

    public void start() {
        mAudioRecord.startRecording();
        mMediaCodec.start();
        final long startWhen = System.nanoTime();
        final ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
//        mThread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                int len, bufferIndex;
//                while (isStart && !Thread.interrupted()) {
//                    synchronized (mMediaCodec) {
//                        if (!isStart) return;
//                        bufferIndex = mMediaCodec.dequeueInputBuffer(10000);
//                        if (bufferIndex >= 0) {
//                            inputBuffers[bufferIndex].clear();
//                            long presentationTimeNs = System.nanoTime();
//                            len = mAudioRecord.read(inputBuffers[bufferIndex], bufferSize);
//                            presentationTimeNs -= (len / samplingRate) / 1000000000;
//                            Log.i(TAG, "Index: " + bufferIndex + " len: " + len + " buffer_capacity: " + inputBuffers[bufferIndex].capacity());
//                            long presentationTimeUs = (presentationTimeNs - startWhen) / 1000;
//                            if (len == AudioRecord.ERROR_INVALID_OPERATION || len == AudioRecord.ERROR_BAD_VALUE) {
//                                Log.e(TAG, "An error occured with the AudioRecord API !");
//                            } else {
//                                mMediaCodec.queueInputBuffer(bufferIndex, 0, len, presentationTimeUs, 0);
//                                if (onDataComingCallback != null) {
//                                    onDataComingCallback.onComing();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        });

        mThread = new Thread(workRunnable);
        mThread.start();

        isStart = true;
    }


    public static void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 4; // 44.1KHz
        int chanCfg = 2; // CPE


        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public void setOnDataComingCallback(OnDataComingCallback onDataComingCallback) {
        this.onDataComingCallback = onDataComingCallback;
    }

    public interface OnDataComingCallback {
        void onComing();
    }


    public MediaCodecInputStream getMediaCodecInputStream() {
        return mediaCodecInputStream;
    }


    public synchronized void close() {
        if (!isStart) {
            return;
        }
        Log.d(TAG, "Interrupting threads...");
        isStart = false;
        mThread.interrupt();
        mediaCodecInputStream.close();
        synchronized (mMediaCodec) {
            mMediaCodec.stop();
            mMediaCodec.release();
        }
        mAudioRecord.stop();
        mAudioRecord.release();
    }

}
