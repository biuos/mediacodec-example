
package dai.android.media.gl.view.texture;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;

import androidx.annotation.Nullable;

import java.util.List;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.canvas.BasicTexture;
import dai.android.media.gl.canvas.RawTexture;
import dai.android.media.gl.view.texture.gles.EglContextWrapper;

/**
 * <p>
 * This will generate a texture which is in the eglContext of the CanvasGL. And the texture can be used outside.
 * The {@link #setSharedEglContext(EglContextWrapper)} will be called automatically when {@link #onSurfaceTextureAvailable(SurfaceTexture, int, int)}
 * For example, the generated texture can be used in camera preview texture or {@link GLMultiTexConsumerView}.
 * </p>
 * From pause to run: onResume --> createSurface --> onSurfaceChanged
 */
public abstract class GLSurfaceTextureProducerView extends GLMultiTexProducerView {

    public GLSurfaceTextureProducerView(Context context) {
        super(context);
    }

    public GLSurfaceTextureProducerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLSurfaceTextureProducerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected final int getInitialTexCount() {
        return 1;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        super.onSurfaceTextureAvailable(surface, width, height);
        if (mSharedEglContext == null) {
            setSharedEglContext(EglContextWrapper.EGL_NO_CONTEXT_WRAPPER);
        }
    }

    public void setOnSurfaceTextureSet(final OnSurfaceTextureSet onSurfaceTextureSet) {
        setSurfaceTextureCreatedListener(new SurfaceTextureCreatedListener() {
            @Override
            public void onCreated(List<GLTexture> glTextureList) {
                GLTexture glTexture = glTextureList.get(0);
                onSurfaceTextureSet.onSet(glTexture.getSurfaceTexture(), glTexture.getRawTexture());
            }
        });
    }

    public interface OnSurfaceTextureSet {
        void onSet(SurfaceTexture surfaceTexture, RawTexture surfaceTextureRelatedTexture);
    }

    @Override
    protected final void onGLDraw(ICanvasGL canvas, List<GLTexture> producedTextures, List<GLTexture> consumedTextures) {
        GLTexture glTexture = producedTextures.get(0);
        if (!consumedTextures.isEmpty()) {
            GLTexture consumeTexture = consumedTextures.get(0);
            onGLDraw(canvas, glTexture.getSurfaceTexture(),
                    glTexture.getRawTexture(),
                    consumeTexture.getSurfaceTexture(),
                    consumeTexture.getRawTexture());
            onGLDraw(canvas, glTexture, consumeTexture);
        } else {
            onGLDraw(canvas, glTexture.getSurfaceTexture(), glTexture.getRawTexture(), null, null);
            onGLDraw(canvas, glTexture, null);
        }
    }

    @Deprecated
    protected void onGLDraw(ICanvasGL canvas,
                            SurfaceTexture producedSurfaceTexture,
                            RawTexture producedRawTexture,
                            @Nullable SurfaceTexture outsideSurfaceTexture,
                            @Nullable BasicTexture outsideTexture) {
    }

    protected void onGLDraw(ICanvasGL canvas, GLTexture producedGLTexture, @Nullable GLTexture outsideGLTexture) {
    }
}
