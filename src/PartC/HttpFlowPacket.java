package PartC;

/**
 * Created by mukul on 3/4/17.
 */
public class HttpFlowPacket {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private int flags;
    private int windowSize;
    private int dataLen;
    private int ackCount;
    private long timeStamp;
    private int segmentLen;
    private String httpPayload;
    private long frameNumber;

    public HttpFlowPacket(int src, int dest, long seq, long ack,
                          int dlen, int segmentlen, String data, int flags, int window, long timestamp, long framenumber) {
        this.sourcePort = src;
        this.destinationPort = dest;
        this.seqNo = seq;
        this.ackNo = ack;
        this.flags = flags;
        this.windowSize = window;
        this.dataLen = dlen;
        this.timeStamp = timestamp;
        this.segmentLen = segmentlen;
        this.httpPayload = data;
        this.frameNumber = framenumber;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestinationPort() {
        return  destinationPort;
    }

    public long getSeqNo() {
        return  seqNo;
    }

    public long getAckNo() {
        return ackNo;
    }

    public int getFlags() {
        return flags;
    }

    public int getWindowSize() {
        return windowSize;
    }

    public int getDataLen() {
        return dataLen;
    }

    public long getTimeStamp() {
        return  timeStamp;
    }

    public int getAckCount() {
        return ackCount;
    }

    public void setAckCount(int ackCount) {
        this.ackCount = ackCount;
    }

    public int getSegmentLen() {
        return segmentLen;
    }

    public String getHttpPayload() {
        return httpPayload;
    }

    public void setHttpPayload(String httpPayload) {
        this.httpPayload = httpPayload;
    }

    public long getFrameNumber() {
        return frameNumber;
    }

    public void setFrameNumber(long frameNumber) {
        this.frameNumber = frameNumber;
    }
}


