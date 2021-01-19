
package dai.android.media.gl.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

import dai.android.media.gl.ICanvasGL;


/**
 * Created by Chilling on 2016/10/24.
 */

public abstract class GLContinuousView extends GLView {
    public GLContinuousView(Context context) {
        super(context);
    }

    public GLContinuousView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }


    protected abstract void onGLDraw(ICanvasGL canvas);
}
