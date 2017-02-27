package fcn_hw2;

/**
 * Created by mukul on 2/26/17.
 */

public class TcpPacketParser {
    private int sourcePort;
    private int destinationPort;
    private int seqNo;
    private int ackNo;
    private int flags;

    public TcpPacketParser(byte[] tcpPacketArray){
        this.sourcePort = tcpPacketArray[0];
        this.destinationPort = tcpPacketArray[0];
        this.seqNo = tcpPacketArray[0];
        this.ackNo = tcpPacketArray[0];
        this.flags = tcpPacketArray[0];
    }

    public void printPacket() {
        System.out.println(sourcePort);
    }
}
