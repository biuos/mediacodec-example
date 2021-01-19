package dai.android.media.gl.view.texture.gles;

import android.opengl.EGL14;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

public class EglContextWrapper {

    protected EGLContext eglContextOld;
    protected android.opengl.EGLContext eglContext;
    public static EglContextWrapper EGL_NO_CONTEXT_WRAPPER = new EGLNoContextWrapper();

    public EGLContext getEglContextOld() {
        return eglContextOld;
    }

    public void setEglContextOld(EGLContext eglContextOld) {
        this.eglContextOld = eglContextOld;
    }

    public android.opengl.EGLContext getEglContext() {
        return eglContext;
    }

    public void setEglContext(android.opengl.EGLContext eglContext) {
        this.eglContext = eglContext;
    }


    public static class EGLNoContextWrapper extends EglContextWrapper {

        public EGLNoContextWrapper() {
            eglContextOld = EGL10.EGL_NO_CONTEXT;
            eglContext = EGL14.EGL_NO_CONTEXT;
        }

        @Override
        public void setEglContext(android.opengl.EGLContext eglContext) {
        }

        @Override
        public void setEglContextOld(EGLContext eglContextOld) {
        }
    }
}
