package fcn_hw2;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by mukul on 2/26/17.
 */

public class TcpPacketParser {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private short flags;
    private int windowSize;

    public static int byteArrayToInt(byte [] b) {
        StringBuilder sb = new StringBuilder(2* b.length);
        for(byte elem: b) {
            sb.append(String.format("%02x",elem));
        }

        BigInteger val = new BigInteger(sb.toString(),16);
        return val.intValue();
    }

    public static long byteArrayToLong(byte [] b) {
        StringBuilder sb = new StringBuilder(2* b.length);
        for(byte elem: b) {
            sb.append(String.format("%02x",elem));
        }

        BigInteger val = new BigInteger(sb.toString(),16);
        return val.longValue();
    }

    private short extractFlags(byte[] b) {
        short _8bits = b[1];
        short _1bit = (short) ((b[0] & 0x1) << 8);

        return (short) (_1bit | _8bits);
    }

    public TcpPacketParser(byte[] tcpPacketArray){
        byte[] subArr;
        subArr = Arrays.copyOfRange(tcpPacketArray,0,2);

        this.sourcePort = byteArrayToInt(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,2,4);
        this.destinationPort = byteArrayToInt(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,4,8);
        this.seqNo = byteArrayToLong(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray,8,12);
        this.ackNo = byteArrayToLong(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 12,14);
        this.flags = extractFlags(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 14, 16);
        this.windowSize = byteArrayToInt(subArr);
    }


    public void printPacket() {
        System.out.println("////////////////////////////////");
        System.out.println("Source:" + sourcePort);
        System.out.println("Destination: " + destinationPort);
        System.out.println("SeqNo: " + seqNo);
        System.out.println("Ack: " + ackNo);
        System.out.println("Flags: " + flags);
        System.out.println("Window Size: " + windowSize);
    }
}