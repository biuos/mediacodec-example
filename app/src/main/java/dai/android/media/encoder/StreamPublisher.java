package dai.android.media.encoder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dai.android.media.encoder.audio.AACEncoder;
import dai.android.media.encoder.muxer.IMuxer;
import dai.android.media.encoder.video.H264Encoder;
import dai.android.media.gl.view.texture.GLTexture;
import dai.android.media.gl.view.texture.gles.EglContextWrapper;
import dai.android.debug.Logger;

public class StreamPublisher {

    public static class Parameter {
        public int width = 640;
        public int height = 480;
        public int videoBitRate = 2949120;
        public int frameRate = 30;
        public int iFrameInterval = 5;
        public int samplingRate = 44100;
        public int audioBitRate = 192000;
        public int audioSource;
        public int channelCfg = AudioFormat.CHANNEL_IN_STEREO;

        public static final String videoMIMEType = "video/avc";
        public static final String audioMIMEType = "audio/mp4a-latm";

        public int audioBufferSize;

        public String outputFilePath;
        public String outputUrl;
        private MediaFormat videoOutputMediaFormat;
        private MediaFormat audioOutputMediaFormat;

        private int initialTextureCount = 1;

        public Parameter() {
            this(640, 480,
                    2949120, 30, 5, 44100, 192000,
                    MediaRecorder.AudioSource.MIC,
                    AudioFormat.CHANNEL_IN_STEREO
            );
        }

        private Parameter(int width, int height, int videoBitRate, int frameRate,
                          int iFrameInterval, int samplingRate,
                          int audioBitRate, int audioSource, int channelCfg) {
            this.width = width;
            this.height = height;
            this.videoBitRate = videoBitRate;
            this.frameRate = frameRate;
            this.iFrameInterval = iFrameInterval;
            this.samplingRate = samplingRate;
            this.audioBitRate = audioBitRate;
            this.audioBufferSize = AudioRecord.getMinBufferSize(samplingRate, channelCfg, AudioFormat.ENCODING_PCM_16BIT) * 2;
            this.audioSource = audioSource;
            this.channelCfg = channelCfg;
        }

        /**
         * @param initialTextureCount Default is 1
         */
        public void setInitialTextureCount(int initialTextureCount) {
            if (initialTextureCount < 1) {
                throw new IllegalArgumentException("initialTextureCount must >= 1");
            }
            this.initialTextureCount = initialTextureCount;
        }

        public int getInitialTextureCount() {
            return initialTextureCount;
        }

        public MediaFormat createVideoMediaFormat() {
            MediaFormat format = MediaFormat.createVideoFormat(videoMIMEType, width, height);

            // Set some properties.  Failing to specify some of these can cause the MediaCodec
            // configure() call to throw an unhelpful exception.
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, videoBitRate);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, frameRate);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, iFrameInterval);
            return format;
        }

        public MediaFormat createAudioMediaFormat() {
            MediaFormat format = MediaFormat.createAudioFormat(audioMIMEType, samplingRate, 2);
            format.setInteger(MediaFormat.KEY_BIT_RATE, audioBitRate);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, audioBufferSize);

            return format;
        }

        public void setVideoOutputMediaFormat(MediaFormat videoOutputMediaFormat) {
            this.videoOutputMediaFormat = videoOutputMediaFormat;
        }

        public void setAudioOutputMediaFormat(MediaFormat audioOutputMediaFormat) {
            this.audioOutputMediaFormat = audioOutputMediaFormat;
        }

        public MediaFormat getVideoOutputMediaFormat() {
            return videoOutputMediaFormat;
        }

        public MediaFormat getAudioOutputMediaFormat() {
            return audioOutputMediaFormat;
        }

        public static class Builder {
            private int width = 640;
            private int height = 480;
            private int videoBitRate = 2949120;
            private int frameRate = 30;
            private int iFrameInterval = 5;
            private int samplingRate = 44100;
            private int audioBitRate = 192000;
            private int audioSource = MediaRecorder.AudioSource.MIC;
            private int channelCfg = AudioFormat.CHANNEL_IN_STEREO;

            public Builder setWidth(int width) {
                this.width = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.height = height;
                return this;
            }

            public Builder setVideoBitRate(int videoBitRate) {
                this.videoBitRate = videoBitRate;
                return this;
            }

            public Builder setFrameRate(int frameRate) {
                this.frameRate = frameRate;
                return this;
            }

            public Builder setiFrameInterval(int iFrameInterval) {
                this.iFrameInterval = iFrameInterval;
                return this;
            }

            public Builder setSamplingRate(int samplingRate) {
                this.samplingRate = samplingRate;
                return this;
            }

            public Builder setAudioBitRate(int audioBitRate) {
                this.audioBitRate = audioBitRate;
                return this;
            }

            public Builder setAudioSource(int audioSource) {
                this.audioSource = audioSource;
                return this;
            }

            public Builder setChannelCfg(int channelCfg) {
                this.channelCfg = channelCfg;
                return this;
            }

            public Parameter createStreamPublisherParam() {
                return new Parameter(width, height, videoBitRate,
                        frameRate, iFrameInterval, samplingRate,
                        audioBitRate, audioSource, channelCfg);
            }
        }
    }

    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    //
    //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++


    public static final int MSG_OPEN = 1;
    public static final int MSG_WRITE_VIDEO = 2;
    private EglContextWrapper eglCtx;
    private IMuxer muxer;
    private AACEncoder aacEncoder;
    private H264Encoder h264Encoder;
    private boolean isStart;

    private HandlerThread writeVideoHandlerThread;

    private Handler writeVideoHandler;
    private Parameter param;
    private List<GLTexture> sharedTextureList = new ArrayList<>();

    public StreamPublisher(EglContextWrapper eglCtx, IMuxer muxer) {
        this.eglCtx = eglCtx;
        this.muxer = muxer;
    }


    public void prepareEncoder(final Parameter param, H264Encoder.OnDrawListener onDrawListener) {
        this.param = param;

        try {
            h264Encoder = new H264Encoder(param, eglCtx);
            for (GLTexture texture :sharedTextureList ) {
                h264Encoder.addSharedTexture(texture);
            }
            h264Encoder.setOnDrawListener(onDrawListener);
            aacEncoder = new AACEncoder(param);
            aacEncoder.setOnDataComingCallback(new AACEncoder.OnDataComingCallback() {
                private byte[] writeBuffer = new byte[param.audioBitRate / 8];

                @Override
                public void onComing() {
                    MediaCodecInputStream mediaCodecInputStream = aacEncoder.getMediaCodecInputStream();
                    MediaCodecInputStream.readAll(mediaCodecInputStream, writeBuffer, new MediaCodecInputStream.OnReadAllCallback() {
                        @Override
                        public void onReadOnce(byte[] buffer, int readSize, MediaCodec.BufferInfo bufferInfo) {
                            if (readSize <= 0) {
                                return;
                            }
                            muxer.writeAudio(buffer, 0, readSize, bufferInfo);
                        }
                    });
                }
            });

        } catch (IOException | IllegalStateException e) {
            e.printStackTrace();
        }

        writeVideoHandlerThread = new HandlerThread("WriteVideoHandlerThread");
        writeVideoHandlerThread.start();
        writeVideoHandler = new Handler(writeVideoHandlerThread.getLooper()) {
            private final byte[] writeBuffer = new byte[param.videoBitRate / 8 / 2];

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_WRITE_VIDEO) {
                    MediaCodecInputStream mediaCodecInputStream = h264Encoder.getMediaCodecInputStream();
                    MediaCodecInputStream.readAll(mediaCodecInputStream, writeBuffer, new MediaCodecInputStream.OnReadAllCallback() {
                        @Override
                        public void onReadOnce(byte[] buffer, int readSize, MediaCodec.BufferInfo bufferInfo) {
                            if (readSize <= 0) {
                                return;
                            }
                            Log.d("StreamPublisher", String.format("onReadOnce: %d", readSize));
                            muxer.writeVideo(buffer, 0, readSize, bufferInfo);
                        }
                    });
                }
            }
        };
    }

    public void addSharedTexture(GLTexture outsideTexture) {
        sharedTextureList.add(outsideTexture);
    }


    public void start() throws IOException {
        if (!isStart) {
            if (muxer.open(param) <= 0) {
                Logger.e("StreamPublisher", "muxer open fail");
                throw new IOException("muxer open fail");
            }
            h264Encoder.start();
            aacEncoder.start();
            isStart = true;
        }

    }

    public void close() {
        isStart = false;
        if (h264Encoder != null) {
            h264Encoder.close();
        }

        if (aacEncoder != null) {
            aacEncoder.close();
        }
        if (writeVideoHandlerThread != null) {
            writeVideoHandlerThread.quitSafely();
        }
        if (muxer != null) {
            muxer.close();
        }
    }

    public boolean isStart() {
        return isStart;
    }


    public boolean drawAFrame() {
        if (isStart) {
            h264Encoder.requestRender();
            writeVideoHandler.sendEmptyMessage(MSG_WRITE_VIDEO);
            return true;
        }
        return false;
    }
}
