package dai.android.media.protocol.rtmp;

public class RTMPMuxer extends RTMPLibLoader {

    public native int open(String url, int video_width, int video_height);

    /**
     * Write H264 NAL units
     *
     * @param data
     * @param offset
     * @param length
     * @param timestamp
     * @return 0 if write network successfully
     * -1 if it could not write
     */
    public native int writeVideo(byte[] data, int offset, int length, long timestamp);

    /**
     * Write raw aac data
     *
     * @param data
     * @param offset
     * @param length
     * @param timestamp
     * @return 0 if write network successfully
     * 1 if it could not write
     */
    public native int writeAudio(byte[] data, int offset, int length, long timestamp);

    public native int read(byte[] data, int offset, int size);

    public native int close();

    public native void writeFLVHeader(boolean is_have_audio, boolean is_have_video);

    public native void fileOpen(String filename);

    public native void fileClose();

    public native boolean isConnected();
}
