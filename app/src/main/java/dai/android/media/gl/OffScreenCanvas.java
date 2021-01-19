package dai.android.media.gl;

import android.graphics.SurfaceTexture;

import androidx.annotation.Nullable;

import java.util.List;

import dai.android.media.gl.canvas.BasicTexture;
import dai.android.media.gl.canvas.RawTexture;
import dai.android.media.gl.view.texture.GLMultiTexProducerView;
import dai.android.media.gl.view.texture.GLSurfaceTextureProducerView;
import dai.android.media.gl.view.texture.GLTexture;
import dai.android.media.gl.view.texture.gles.EglContextWrapper;

/**
 * @deprecated use {@link MultiTexOffScreenCanvas} instead
 */
public abstract class OffScreenCanvas extends MultiTexOffScreenCanvas {

    private BasicTexture outsideSharedTexture;
    private SurfaceTexture outsideSharedSurfaceTexture;


    public OffScreenCanvas() {
    }

    public OffScreenCanvas(int width, int height) {
        super(width, height);
    }

    public OffScreenCanvas(Object surface) {
        super(surface);
    }

    public OffScreenCanvas(int width, int height, Object surface) {
        super(width, height, surface);
    }

    public OffScreenCanvas(int width, int height, EglContextWrapper sharedEglContext, Object surface) {
        super(width, height, sharedEglContext, surface);
    }

    public OffScreenCanvas(int width, int height, EglContextWrapper sharedEglContext) {
        super(width, height, sharedEglContext);
    }

    /**
     * If it is used, it must be called before start() called.
     */
    public void setOnSurfaceTextureSet(final GLSurfaceTextureProducerView.OnSurfaceTextureSet onSurfaceTextureSet) {
        setSurfaceTextureCreatedListener(new GLMultiTexProducerView.SurfaceTextureCreatedListener() {
            @Override
            public void onCreated(List<GLTexture> glTextureList) {
                GLTexture glTexture = glTextureList.get(0);
                onSurfaceTextureSet.onSet(glTexture.getSurfaceTexture(), glTexture.getRawTexture());
            }
        });
    }

    public void setSharedTexture(RawTexture outsideTexture, @Nullable SurfaceTexture outsideSurfaceTexture) {
        this.outsideSharedTexture = outsideTexture;
        this.outsideSharedSurfaceTexture = outsideSurfaceTexture;
        if (consumedTextures.isEmpty()) {
            consumedTextures.add(new GLTexture(outsideTexture, outsideSurfaceTexture));
        }
    }


    @Override
    protected final void onGLDraw(ICanvasGL canvas, List<GLTexture> producedTextures, List<GLTexture> consumedTextures) {
        GLTexture glTexture = producedTextures.get(0);
        if (!consumedTextures.isEmpty()) {
            GLTexture consumeTexture = consumedTextures.get(0);
            onGLDraw(canvas, glTexture.getSurfaceTexture(), glTexture.getRawTexture(), consumeTexture.getSurfaceTexture(), consumeTexture.getRawTexture());
        } else {
            onGLDraw(canvas, glTexture.getSurfaceTexture(), glTexture.getRawTexture(), null, null);
        }
    }

    protected abstract void onGLDraw(ICanvasGL canvas, SurfaceTexture producedSurfaceTexture, RawTexture producedRawTexture, @Nullable SurfaceTexture outsideSharedSurfaceTexture, @Nullable BasicTexture outsideSharedTexture);
}
