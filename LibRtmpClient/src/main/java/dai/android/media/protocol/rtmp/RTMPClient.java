package dai.android.media.protocol.rtmp;

public class RTMPClient extends RTMPLibLoader {

    /**
     * RTMP read has received an EOF or READ_COMPLETE from the server
     */
    public final static int RTMP_READ_DONE = -1;

    /**
     * No error
     */
    private final static int RTMP_SUCCESS = 0;

    private final static int TIMEOUT_IN_MS = 10000;
    private long handler = 0;

    /**
     * Socket send timeout value in milliseconds
     */
    private int sendTimeoutInMs = TIMEOUT_IN_MS;
    /**
     * Socket receive timeout value in seconds
     */
    private int receiveTimeoutInMs = TIMEOUT_IN_MS;


    /**
     * Sets the socket's send timeout value
     *
     * @param sendTimeoutInMs The send timeout value for the rtmp socket in milliseconds.
     *                        Parameter expects a non-zero positive integer and will reset timeout to the default value
     *                        (10000 ms) if zero or a negative integer is passed.
     */
    public void setSendTimeout(int sendTimeoutInMs) {
        if (sendTimeoutInMs > 0) {
            this.sendTimeoutInMs = sendTimeoutInMs;
        } else {
            this.sendTimeoutInMs = TIMEOUT_IN_MS;
        }
    }

    /**
     * Sets the socket's receive timeout value
     *
     * @param receiveTimeoutInMs The receive timeout value for the rtmp socket in milliseconds.
     *                           Parameter expects a non-zero positive integer and will reset timeout to the default value
     *                           (10000 ms) if zero or a negative integer is passed.
     */
    public void setReceiveTimeout(int receiveTimeoutInMs) {
        if (receiveTimeoutInMs > 0) {
            this.receiveTimeoutInMs = receiveTimeoutInMs;
        } else {
            this.receiveTimeoutInMs = TIMEOUT_IN_MS;
        }
    }

    /**
     * opens the rtmp url
     *
     * @param url           url of the stream
     * @param isPublishMode if this is an publication it is true,
     *                      if connection is for getting stream it is false
     * @throws RTMPIOException if open fails
     */
    public void open(String url, boolean isPublishMode) throws RTMPIOException {
        handler = nativeAlloc();
        if (handler == 0) {
            throw new RTMPIOException(RTMPIOException.OPEN_ALLOC);
        }
        int result = nativeOpen(url, isPublishMode, handler, sendTimeoutInMs / 1000);
        if (result != RTMP_SUCCESS) {
            handler = 0;
            throw new RTMPIOException(result);
        }
    }

    private native long nativeAlloc();

    /**
     * opens the rtmp url
     *
     * @param url         url of the stream
     * @param publishMode if this is an publication it is true,
     *                    if connection is for getting stream it is false
     * @return return a minus value if it fails
     * returns
     * <p>
     * returns {@link #OPEN_SUCCESS} if it is successful, throws RtmpIOException if it is failed
     */
    private native int nativeOpen(String url, boolean publishMode, long handler, int timeout_in_seconds);

    /**
     * read data from rtmp connection
     *
     * @param data   buffer that will be filled
     * @param offset offset to read data
     * @param size   size of the data to be read
     * @return number of bytes to be read
     * <p>
     * if it returns {@link #RTMP_READ_DONE}, it means stream is complete
     * and close function can be called.
     * @throws RTMPIOException       if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     *                               missing
     */
    public int read(byte[] data, int offset, int size) throws RTMPIOException, IllegalStateException {
        int ret = nativeRead(data, offset, size, handler);
        if (ret < RTMP_SUCCESS && ret != RTMP_READ_DONE) {
            throw new RTMPIOException(ret);
        }
        return ret;
    }

    private native int nativeRead(byte[] data, int offset, int size, long handler) throws IllegalStateException;

    /**
     * Sends data to server
     *
     * @param data The data to write to server
     * @return number of bytes written
     * @throws RTMPIOException       if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     *                               missing
     */
    public int write(byte[] data) throws RTMPIOException, IllegalStateException {
        return write(data, 0, data.length);
    }

    /**
     * Sends data to server
     *
     * @param data   data to write to server
     * @param offset The offset from where data will be accessed to write to server
     * @param size   The number of bytes to write to server
     * @return number of bytes written
     * @throws RTMPIOException       if connection to server is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     *                               missing
     */
    public int write(byte[] data, int offset, int size) throws RTMPIOException, IllegalStateException {
        int ret = nativeWrite(data, offset, size, handler);
        if (ret < RTMP_SUCCESS) {
            throw new RTMPIOException(ret);
        }
        return ret;
    }

    private native int nativeWrite(byte[] data, int offset, int size, long handler) throws IllegalStateException;


    /**
     * @param pause if pause is true then stream is going to be paused
     *              <p>
     *              If pause is false, it unpauses the stream and it is ready to to play again
     * @return true if it is successful else returns false
     * @throws RTMPIOException       if connection is lost
     * @throws IllegalStateException if call to {@link #open(String, boolean)} was unsuccessful or
     *                               missing
     */
    public boolean pause(boolean pause) throws RTMPIOException, IllegalStateException {
        int ret = nativePause(pause, handler);
        if (ret != RTMP_SUCCESS) {
            throw new RTMPIOException(ret);
        }
        return true;
    }

    private native int nativePause(boolean pause, long handler) throws IllegalStateException;


    /**
     * @return true if it is connected
     * false if it is not connected
     */
    public boolean isConnected() {
        return nativeIsConnected(handler);
    }

    private native boolean nativeIsConnected(long handler);

    /**
     * closes the connection. Don't forget to call
     */
    public void close() {
        nativeClose(handler);
        handler = 0;
    }

    private native void nativeClose(long handler);

}
