package dai.android.media.protocol.rtmp;

abstract class RTMPLibLoader {
    private static final String Libs = "rtmp-jni";

    static {
        System.loadLibrary(Libs);
    }
}

