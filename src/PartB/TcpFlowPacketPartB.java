package PartB;

/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlowPacketPartB {
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

    public TcpFlowPacketPartB(int src, int dest, long seq, long ack,
                              int dlen, int segmentlen, int flags, int window, long timestamp) {
        this.sourcePort = src;
        this.destinationPort = dest;
        this.seqNo = seq;
        this.ackNo = ack;
        this.flags = flags;
        this.windowSize = window;
        this.dataLen = dlen;
        this.timeStamp = timestamp;
        this.segmentLen = segmentlen;
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
}


