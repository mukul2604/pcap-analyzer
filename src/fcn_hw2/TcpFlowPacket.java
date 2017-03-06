package fcn_hw2;

/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlowPacket {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private int flags;
    private int windowSize;
    private int dataLen;
    private int timeStamp;

    public TcpFlowPacket(int src, int dest, long seq, long ack,
                         int dlen, int flags, int window, int timestamp) {
        this.sourcePort = src;
        this.destinationPort = dest;
        this.seqNo = seq;
        this.ackNo = ack;
        this.flags = flags;
        this.windowSize = window;
        this.dataLen = dlen;
        this.timeStamp = timestamp;
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

    public int getTimeStamp() {
        return  timeStamp;
    }
}
