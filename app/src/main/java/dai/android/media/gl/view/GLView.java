
package dai.android.media.gl.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.AttributeSet;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import dai.android.media.gl.CanvasGL;
import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.OpenGLUtil;


public abstract class GLView extends GLSurfaceView implements GLSurfaceView.Renderer {


    protected CanvasGL mCanvas;

    private OnSizeChangeCallback onSizeChangeCallback;
    protected GL10 gl;

    public GLView(Context context) {
        super(context);
        init();
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    protected void init() {
        setZOrderOnTop(true);
        setEGLContextClientVersion(2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        } else {
            setEGLConfigChooser(5, 6, 5, 8, 0, 0);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setPreserveEGLContextOnPause(true);
        }
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mCanvas = new CanvasGL();

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mCanvas.setSize(width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        this.gl = gl;
        mCanvas.clearBuffer();
        onGLDraw(mCanvas);
    }


    /**
     * May call twice at first.
     */
    protected abstract void onGLDraw(ICanvasGL canvas);


    public void restart() {
        onResume();
    }

    public void stop() {
        onPause();
        if (mCanvas != null) {
            mCanvas.pause();
        }
    }

    /**
     * Force clear texture and bitmap cache of the canvas. This is not necessary needed.
     * The canvas uses weak HashMap to reference bitmap and will recycle the texture when finalize
     */
    public void clearBitmapCache() {
        if (mCanvas != null) {
            mCanvas.clearBitmapCache();
        }
    }

    public void setOnSizeChangeCallback(OnSizeChangeCallback onSizeChangeCallback) {
        this.onSizeChangeCallback = onSizeChangeCallback;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onSizeChangeCallback != null) {
            onSizeChangeCallback.onSizeChange(w, h, oldw, oldh);
        }
    }

    public interface OnSizeChangeCallback {
        void onSizeChange(int w, int h, int oldw, int oldh);
    }


    public void getDrawingBitmap(final Rect rect, final GetDrawingCacheCallback getDrawingCacheCallback) {

        queueEvent(new Runnable() {
            @Override
            public void run() {
                if (gl == null) {
                    return;
                }
                onDrawFrame(gl);
                onDrawFrame(gl);
                final Bitmap bitmapFromGLSurface = OpenGLUtil.createBitmapFromGLSurface(rect.left, rect.top, rect.right, rect.bottom, getHeight());

                post(new Runnable() {
                    @Override
                    public void run() {
                        getDrawingCacheCallback.onFetch(bitmapFromGLSurface);
                    }
                });
            }
        });
        requestRender();
    }

    public interface GetDrawingCacheCallback {
        void onFetch(Bitmap bitmap);
    }
}
