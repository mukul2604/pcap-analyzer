package fcn_hw2;

import java.math.BigInteger;
import java.util.Arrays;

import static fcn_hw2.TcpAnalyzerMain.*;


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

        TcpFlowPacket fPacket = new TcpFlowPacket(sourcePort,destinationPort, seqNo,
                                    ackNo, flags, windowSize);

        int srcDestKey = sourcePort*27 + destinationPort;
        int destSrcKey = destinationPort*27 + sourcePort;

        if (tcpFlowHashMap.containsKey(srcDestKey))
            tcpFlowHashMap.get(srcDestKey).push(fPacket);

        if (tcpFlowHashMap.containsKey(destSrcKey))
            tcpFlowHashMap.get(destSrcKey).push(fPacket);

        //FLOW COUNT evaluation
        if ((flags & SYN) == SYN && (flags & ACK) != ACK) {
            int state = SYN;
            flowHash.remove(srcDestKey);
            flowHash.put(srcDestKey, state);
        }

        if ((flags & SYN) == SYN && (flags & ACK) == ACK) {

            if (flowHash.containsKey(destSrcKey)) {
                int state = flowHash.get(destSrcKey);
                if (state == SYN) {
                    state = SYN|ACK;
                    flowHash.remove(destSrcKey);
                    flowHash.put(destSrcKey, state);
                }
            }
        }

        if ((flags & SYN) != SYN && (flags & ACK) == ACK) {
            int a = sourcePort*27 + destinationPort;
            if (flowHash.containsKey(srcDestKey)) {
                int state = flowHash.get(srcDestKey);
                if (state == (SYN|ACK)) {
                    state = ACK;
                    flowHash.remove(srcDestKey);
                    flowHash.put(srcDestKey, state);
                    TcpFlow flow = new TcpFlow(sourcePort, destinationPort);
                    tcpFlowHashMap.put(srcDestKey, flow);
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
