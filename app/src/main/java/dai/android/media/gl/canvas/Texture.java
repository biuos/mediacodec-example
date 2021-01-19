package dai.android.media.gl.canvas;


// Texture is a rectangular image which can be drawn on GLCanvas.
// The isOpaque() function gives a hint about whether the secondBitmap is opaque,
// so the drawing can be done faster.
//
// This is the current secondBitmap hierarchy:
//
// Texture
// -- ColorTexture
// -- FadeInTexture
// -- BasicTexture
//    -- UploadedTexture
//       -- BitmapTexture
//       -- Tile
//       -- ResourceTexture
//          -- NinePatchTexture
//       -- CanvasTexture
//          -- StringTexture
//
public interface Texture {
    public int getWidth();

    public int getHeight();

    public void draw(GLCanvas canvas, int x, int y);

    public void draw(GLCanvas canvas, int x, int y, int w, int h);

    public boolean isOpaque();
}
