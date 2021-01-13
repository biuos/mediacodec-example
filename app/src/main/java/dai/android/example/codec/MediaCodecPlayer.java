package dai.android.example.codec;

import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

class MediaCodecPlayer {
    private static final String TAG = "MediaCodecPlayer";

    private final Surface surface;
    private final String dataSource;

    MediaCodecPlayer(String _source, Surface _surface) {
        dataSource = _source;
        surface = _surface;
    }

    private MediaCodec mediaCodec;


    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MediaExtractor extractor = new MediaExtractor();
            try {
                extractor.setDataSource(dataSource);
            } catch (IOException e) {
                Log.e(TAG, "setDataSource failed", e);
                return;
            }

            for (int i = 0; i < extractor.getTrackCount(); ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                Log.i(TAG, "mime=" + mime);
                if (mime.startsWith("video/")) {
                    extractor.selectTrack(i);
                    try {
                        mediaCodec = MediaCodec.createDecoderByType(mime);
                        mediaCodec.configure(format, surface, null, 0);
                        break;
                    } catch (IOException e) {
                        Log.e(TAG, "createDecoderByType failed", e);
                        return;
                    }
                }
            }

            if (null == mediaCodec) {
                Log.e(TAG, "can not create video decoder");
                return;
            }

            mediaCodec.start();


            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            boolean isEOS = false;
            long startMs = System.currentTimeMillis();

            while (!Thread.interrupted()) {
                if (!isEOS) {
                    int inIndex = mediaCodec.dequeueInputBuffer(10000);
                    if (inIndex >= 0) {
                        ByteBuffer buffer = inputBuffers[inIndex];
                        int sampleSize = extractor.readSampleData(buffer, 0);
                        if (sampleSize < 0) {
                            // We shouldn't stop the playback at this point, just pass the EOS
                            // flag to decoder, we will get it again from the
                            // dequeueOutputBuffer
                            mediaCodec.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            isEOS = true;
                        } else {
                            mediaCodec.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                            extractor.advance();
                        }
                    }
                }

                int outIndex = mediaCodec.dequeueOutputBuffer(info, 10000);
                switch (outIndex) {
                    case MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED: {
                        Log.w(TAG, "INFO_OUTPUT_BUFFERS_CHANGED");
                        outputBuffers = mediaCodec.getOutputBuffers();
                        break;
                    }
                    case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED: {
                        Log.i(TAG, "INFO_OUTPUT_FORMAT_CHANGED, new format=" + mediaCodec.getOutputFormat());
                        break;
                    }
                    case MediaCodec.INFO_TRY_AGAIN_LATER: {
                        Log.d(TAG, "dequeueOutputBuffer timed out!");
                        break;
                    }
                    default: {
                        ByteBuffer buffer = outputBuffers[outIndex];
                        Log.w(TAG, "We can't use this buffer but render it due to the API limit, " + buffer);

                        // We use a very simple clock to keep the video FPS, or the video
                        // playback will be too fast
                        while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                            try {
                                Thread.sleep(10);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                        mediaCodec.releaseOutputBuffer(outIndex, true);

                        break;
                    }
                }

                // All decoded frames have been rendered, we can stop playing now
                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    Log.d(TAG, "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                    break;
                }
            }


            mediaCodec.stop();
            mediaCodec.release();
            extractor.release();
        }
    };

    private final Thread thread = new Thread(runnable);


    public void start() {
        if (!thread.isAlive()) {
            thread.start();
        }
    }


    public void stop() {
        if (thread.isAlive() && !thread.isInterrupted()) {
            thread.interrupt();
        }
    }
}
