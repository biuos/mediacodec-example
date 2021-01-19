
package dai.android.media.gl.view.texture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.List;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.canvas.BasicTexture;
import dai.android.media.gl.canvas.RawTexture;

/**
 * This class is used to accept eglContext and texture from outside. Then it can use them to draw.
 *
 * @deprecated Use {@link GLMultiTexConsumerView} instead.
 */
public abstract class GLSharedContextView extends GLMultiTexConsumerView {


    protected BasicTexture outsideSharedTexture;
    protected SurfaceTexture outsideSharedSurfaceTexture;

    public GLSharedContextView(Context context) {
        super(context);
    }

    public GLSharedContextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLSharedContextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSharedTexture(RawTexture outsideTexture, SurfaceTexture outsideSurfaceTexture) {
        this.outsideSharedTexture = outsideTexture;
        this.outsideSharedSurfaceTexture = outsideSurfaceTexture;
        if (consumedTextures.isEmpty()) {
            consumedTextures.add(new GLTexture(outsideTexture, outsideSurfaceTexture));
        }
    }

    /**
     * Will not call until @param surfaceTexture not null
     */
    protected abstract void onGLDraw(ICanvasGL canvas, @Nullable SurfaceTexture sharedSurfaceTexture, BasicTexture sharedTexture);

    @Override
    protected final void onGLDraw(ICanvasGL canvas, List<GLTexture> consumedTextures) {
        if (outsideSharedTexture != null && outsideSharedTexture.isRecycled()) {
            outsideSharedTexture = null;
            outsideSharedSurfaceTexture = null;
        }
        onGLDraw(canvas, outsideSharedSurfaceTexture, outsideSharedTexture);
    }


    @Override
    protected void surfaceDestroyed() {
        super.surfaceDestroyed();
        outsideSharedSurfaceTexture = null;
        outsideSharedTexture = null;
    }
}
