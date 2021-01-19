
package dai.android.media.gl.view.texture;

import android.content.Context;
import android.util.AttributeSet;

import dai.android.media.gl.view.texture.gles.GLThread;

public abstract class GLContinuousTextureView extends GLTextureView {
    public GLContinuousTextureView(Context context) {
        super(context);
    }

    public GLContinuousTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GLContinuousTextureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getRenderMode() {
        return GLThread.RENDERMODE_CONTINUOUSLY;
    }
}
