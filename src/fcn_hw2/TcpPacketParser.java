package fcn_hw2;

import java.nio.ByteBuffer;

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
        byte [] arr = {tcpPacketArray[0], tcpPacketArray[1]};
        ByteBuffer wrapped = ByteBuffer.wrap(arr);
        this.sourcePort = wrapped.getShort();
        byte [] arr1 = {tcpPacketArray[2], tcpPacketArray[3]};
        ByteBuffer wrapped1 = ByteBuffer.wrap(arr1);
        this.destinationPort = wrapped1.getShort();
        this.seqNo = tcpPacketArray[0];
        this.ackNo = tcpPacketArray[0];
        this.flags = tcpPacketArray[0];
    }

    public void printPacket() {
        System.out.println("Source:" + sourcePort);
        System.out.println("Destination: " + destinationPort);
    }
}
