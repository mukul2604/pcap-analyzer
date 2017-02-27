package fcn_hw2;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Created by mukul on 2/26/17.
 */

public class TcpPacketParser {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private int flags;
    private int  temp;

    private int byteArrayToUnsignedShort(byte[] b) {
        return ((b[0] << 8 | b[1]) & 0xffff);

    }

    private long byteArrayToUnsignedInt(byte[] b) {
        return ((b[0] << 24 | b[1] << 16 | b[2] << 8 | b[3]) & 0xffffffff);
    }
    
    public TcpPacketParser(byte[] tcpPacketArray){
        byte[] subArr;
        subArr = Arrays.copyOfRange(tcpPacketArray,0,2);
        this.sourcePort = byteArrayToUnsignedShort(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,2,4);
        this.destinationPort = byteArrayToUnsignedShort(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,4,8);
        this.seqNo = byteArrayToUnsignedInt(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,8,12);
        this.ackNo = byteArrayToUnsignedInt(subArr);
        this.flags = tcpPacketArray[0];
    }


    public void printPacket() {
        System.out.println("////////////////////////////////");
        System.out.println("Source:" + sourcePort);
        System.out.println("Destination: " + destinationPort);
        System.out.println("Seqno: " + seqNo);
        System.out.println("Ack: " + ackNo);
        System.out.println("////////////////////////////////");
    }
}
