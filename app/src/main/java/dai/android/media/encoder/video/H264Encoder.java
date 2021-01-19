package dai.android.media.encoder.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;
import java.util.List;

import dai.android.media.encoder.MediaCodecInputStream;
import dai.android.media.encoder.StreamPublisher;
import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.MultiTexOffScreenCanvas;
import dai.android.media.gl.view.texture.GLTexture;
import dai.android.media.gl.view.texture.gles.EglContextWrapper;

public class H264Encoder {
    private static final String TAG = "H264Encoder";

    private final Surface mInputSurface;
    private final MediaCodecInputStream mediaCodecInputStream;

    MediaCodec mEncoder;

    private static final String MIME_TYPE = "video/avc";    // H.264 Advanced Video Coding
    private static final int FRAME_RATE = 30;               // 30fps
    private static final int IFRAME_INTERVAL = 5;           // 5 seconds between I-frames
    protected final EncoderCanvas offScreenCanvas;
    private OnDrawListener onDrawListener;
    private boolean isStart;
    private int initialTextureCount = 1;


    public H264Encoder(StreamPublisher.Parameter params) throws IOException {
        this(params, EglContextWrapper.EGL_NO_CONTEXT_WRAPPER);
    }


    /**
     * @param eglCtx can be EGL10.EGL_NO_CONTEXT or outside context
     */
    public H264Encoder(final StreamPublisher.Parameter params, final EglContextWrapper eglCtx) throws IOException {
        MediaFormat format = params.createVideoMediaFormat();
        // Set some properties.  Failing to specify some of these can cause the MediaCodec
        // configure() call to throw an unhelpful exception.
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        format.setInteger(MediaFormat.KEY_BIT_RATE, params.videoBitRate);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, params.frameRate);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, params.iFrameInterval);
        mEncoder = MediaCodec.createEncoderByType(StreamPublisher.Parameter.videoMIMEType);
        mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mInputSurface = mEncoder.createInputSurface();
        mEncoder.start();
        mediaCodecInputStream = new MediaCodecInputStream(mEncoder, new MediaCodecInputStream.MediaFormatCallback() {
            @Override
            public void onChangeMediaFormat(MediaFormat mediaFormat) {
                params.setVideoOutputMediaFormat(mediaFormat);
            }
        });

        this.initialTextureCount = params.getInitialTextureCount();
        offScreenCanvas = new EncoderCanvas(params.width, params.height, eglCtx);
    }

    /**
     * If called, should be called before start() called.
     */
    public void addSharedTexture(GLTexture texture) {
        offScreenCanvas.addConsumeGLTexture(texture);
    }


    public Surface getInputSurface() {
        return mInputSurface;
    }

    public MediaCodecInputStream getMediaCodecInputStream() {
        return mediaCodecInputStream;
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

    public void start() {
        offScreenCanvas.start();
        isStart = true;
    }

    public void close() {
        if (!isStart) return;

        Log.d(TAG, "close");
        offScreenCanvas.end();
        mediaCodecInputStream.close();
        synchronized (mEncoder) {
            mEncoder.stop();
            mEncoder.release();
        }
        isStart = false;
    }

    public boolean isStart() {
        return isStart;
    }

    public void requestRender() {
        offScreenCanvas.requestRender();
    }


    public void requestRenderAndWait() {
        offScreenCanvas.requestRenderAndWait();
    }

    public void setOnDrawListener(OnDrawListener l) {
        this.onDrawListener = l;
    }

    public interface OnDrawListener {
        /**
         * Called when a frame is ready to be drawn.
         *
         * @param canvasGL         The gl canvas
         * @param producedTextures The textures produced by internal. These can be used for camera or video decoder to render.
         * @param consumedTextures See {@link #addSharedTexture(GLTexture)}. The textures you set from outside. Then you can draw the textures render by other Views of OffscreenCanvas.
         */
        void onGLDraw(ICanvasGL canvasGL, List<GLTexture> producedTextures, List<GLTexture> consumedTextures);
    }

    private class EncoderCanvas extends MultiTexOffScreenCanvas {
        public EncoderCanvas(int width, int height, EglContextWrapper eglCtx) {
            super(width, height, eglCtx, H264Encoder.this.mInputSurface);
        }

        @Override
        protected void onGLDraw(ICanvasGL canvas, List<GLTexture> producedTextures, List<GLTexture> consumedTextures) {
            if (onDrawListener != null) {
                onDrawListener.onGLDraw(canvas, producedTextures, consumedTextures);
            }
        }

        @Override
        protected int getInitialTexCount() {
            return initialTextureCount;
        }
    }
}
