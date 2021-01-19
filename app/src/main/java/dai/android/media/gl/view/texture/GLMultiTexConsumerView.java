
package dai.android.media.gl.view.texture;

import android.content.Context;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dai.android.media.gl.ICanvasGL;
import dai.android.media.gl.view.texture.gles.EglContextWrapper;

/**
 * This class is used to accept eglContext and textures from outside. Then it can use them to draw.
 * The {@link #setSharedEglContext} must be called as the precondition to consume outside texture.
 */
public abstract class GLMultiTexConsumerView extends BaseGLCanvasTextureView {

    protected List<GLTexture> consumedTextures = new ArrayList<>();

    protected EglContextWrapper mSharedEglContext;

    public GLMultiTexConsumerView(Context context) {
        super(context);
    }

    public GLMultiTexConsumerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLMultiTexConsumerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * @param sharedEglContext The openGL context from other or {@link EglContextWrapper#EGL_NO_CONTEXT_WRAPPER}
     */
    public void setSharedEglContext(EglContextWrapper sharedEglContext) {
        mSharedEglContext = sharedEglContext;
        glThreadBuilder.setSharedEglContext(sharedEglContext);
        createGLThread();
    }

    @Override
    protected void createGLThread() {
        if (mSharedEglContext != null) {
            super.createGLThread();
        }
    }

    /**
     * This must be called for a GLMultiTexConsumerView.
     * @param glTexture texture from outSide.
     */
    public void addConsumeGLTexture(GLTexture glTexture) {
        consumedTextures.add(glTexture);
    }

    /**
     *
     * Will not call until @param surfaceTexture not null
     */
    protected abstract void onGLDraw(ICanvasGL canvas, List<GLTexture> consumedTextures);

    @Override
    protected final void onGLDraw(ICanvasGL canvas) {
        Iterator<GLTexture> iterator = consumedTextures.iterator();
        while (iterator.hasNext()) {
            GLTexture next =  iterator.next();
            if (next.getRawTexture().isRecycled()) {
                iterator.remove();
            }
        }
        onGLDraw(canvas, consumedTextures);
    }

    @Override
    protected void surfaceDestroyed() {
        super.surfaceDestroyed();
        mSharedEglContext = null;
        consumedTextures.clear();
    }
}
