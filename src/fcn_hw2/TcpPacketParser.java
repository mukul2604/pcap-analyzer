package fcn_hw2;

import java.math.BigInteger;
import java.util.Arrays;


import static fcn_hw2.TcpAnalyzerMain.SYN;
import static fcn_hw2.TcpAnalyzerMain.ACK;
import static fcn_hw2.TcpAnalyzerMain.flowHash;


/**
 * Created by mukul on 2/26/17.
 */

public class TcpPacketParser {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private int flags;
    private int windowSize;
   // private int state;

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

    private int extractFlags(byte[] b) {
        short _8bits = b[1];
        short _1bit = (short) ((b[0] & 0x1) << 8);

        return (_1bit | _8bits);
    }

    public TcpPacketParser(byte [] frame){
        byte[] tcpPacketArray = Arrays.copyOfRange(frame, 34, frame.length);
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

        //FLOW COUNT evaluation
        if ((flags & SYN) == SYN && (flags & ACK) != ACK) {
            int a = sourcePort*27 + destinationPort;
            int state = SYN;
            flowHash.put(a, state);
        }

        if ((flags & SYN) == SYN && (flags & ACK) == ACK) {
            int  a = destinationPort*27 + sourcePort;
            if (flowHash.containsKey(a)) {
                int state = flowHash.get(a);
                if (state == SYN) {
                    state = SYN|ACK;
                    flowHash.put(a, state);
                }
            }
        }

        if ((flags & SYN) != SYN && (flags & ACK) == ACK) {
            int a = sourcePort*27 + destinationPort;
            if (flowHash.containsKey(a)) {
                int state = flowHash.get(a);
                if (state == (SYN|ACK)) {
                    state = ACK;
                    flowHash.put(a, state);
                }
            }
        }
    }


    public void printPacket() {
        System.out.printf("Source: %5d\tDestination: %5d\tSeqNo: %12d\tAck: %12d\tFlags: %d\tWindow Size: %d\n",
                        sourcePort, destinationPort, seqNo, ackNo, flags, windowSize);
    }

    public int srcPort() {
        return  sourcePort;
    }

    public int destPort() {
        return destinationPort;
    }

    public long seqNo() {
        return seqNo;
    }

    public long ackNo() {
        return ackNo;
    }

    public int tcpFlags() {
        return flags;
    }

    public int windowSize() {
        return windowSize;
    }


}
