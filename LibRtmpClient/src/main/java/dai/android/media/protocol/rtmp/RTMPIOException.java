package dai.android.media.protocol.rtmp;

import java.io.IOException;

public class RTMPIOException extends IOException {

    /**
     * RTMP client could not allocate memory for rtmp context structure
     */
    public final static int OPEN_ALLOC = -2;

    /**
     * RTMP client could not open the stream on server
     */
    public final static int OPEN_CONNECT_STREAM = -3;

    /**
     * Received an unknown option from the RTMP server
     */
    public final static int UNKNOWN_RTMP_OPTION = -4;

    /**
     * RTMP server sent a packet with unknown AMF type
     */
    public final static int UNKNOWN_RTMP_AMF_TYPE = -5;

    /**
     * DNS server is not reachable
     */
    public final static int DNS_NOT_REACHABLE = -6;

    /**
     * Could not establish a socket connection to the server
     */
    public final static int SOCKET_CONNECT_FAIL = -7;

    /**
     * SOCKS negotiation failed
     */
    public final static int SOCKS_NEGOTIATION_FAIL = -8;

    /**
     * Could not create a socket to connect to RTMP server
     */
    public final static int SOCKET_CREATE_FAIL = -9;

    /**
     * SSL connection requested but not supported by the client
     */
    public final static int NO_SSL_TLS_SUPP = -10;

    /**
     * Could not connect to the server for handshake
     */
    public final static int HANDSHAKE_CONNECT_FAIL = -11;

    /**
     * Handshake with the server failed
     */
    public final static int HANDSHAKE_FAIL = -12;

    /**
     * RTMP server connection failed
     */
    public final static int RTMP_CONNECT_FAIL = -13;

    /**
     * Connection to the server lost
     */
    public final static int CONNECTION_LOST = -14;

    /**
     * Received an unexpected timestamp from the server
     */
    public final static int RTMP_KEYFRAME_TS_MISMATCH = -15;

    /**
     * The RTMP stream received is corrupted
     */
    public final static int RTMP_READ_CORRUPT_STREAM = -16;

    /**
     * Memory allocation failed
     */
    public final static int RTMP_MEM_ALLOC_FAIL = -17;

    /**
     * Stream indicated a bad datasize, could be corrupted
     */
    public final static int RTMP_STREAM_BAD_DATASIZE = -18;

    /**
     * RTMP packet received is too small
     */
    public final static int RTMP_PACKET_TOO_SMALL = -19;

    /**
     * Could not send packet to RTMP server
     */
    public final static int RTMP_SEND_PACKET_FAIL = -20;

    /**
     * AMF Encode failed while preparing a packet
     */
    public final static int RTMP_AMF_ENCODE_FAIL = -21;

    /**
     * Missing a :// in the URL
     */
    public final static int URL_MISSING_PROTOCOL = -22;

    /**
     * Hostname is missing in the URL
     */
    public final static int URL_MISSING_HOSTNAME = -23;

    /**
     * The port number indicated in the URL is wrong
     */
    public final static int URL_INCORRECT_PORT = -24;

    /**
     * Error code used by JNI to return after throwing an exception
     */
    public final static int RTMP_IGNORED = -25;

    /**
     * RTMP client has encountered an unexpected error
     */
    public final static int RTMP_GENERIC_ERROR = -26;

    /**
     * A sanity check failed in the RTMP client
     */
    public final static int RTMP_SANITY_FAIL = -27;

    public final int errorCode;

    public RTMPIOException(int errorCode) {
        super("RTMP error: " + errorCode);
        this.errorCode = errorCode;
    }

}
