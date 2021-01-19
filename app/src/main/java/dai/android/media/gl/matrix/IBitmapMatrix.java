package dai.android.media.gl.matrix;

/**
 * The output is MVP matrix of OpenGL, which is used to calculate gl_position.
 * gl_position = MVP * [x,y,z,w]
 */
public interface IBitmapMatrix {
    float[] obtainResultMatrix(int viewportW, int viewportH,
                               float x, float y,
                               float drawW, float drawH);
}
