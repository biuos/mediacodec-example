
package dai.android.media.gl.canvas;

// This mimics corresponding GL functions.
public interface GLId {
    public int generateTexture();

    public void glGenBuffers(int n, int[] buffers, int offset);

    public void glDeleteTextures(int n, int[] textures, int offset);

    public void glDeleteBuffers(int n, int[] buffers, int offset);

    public void glDeleteFramebuffers(int n, int[] buffers, int offset);
}
