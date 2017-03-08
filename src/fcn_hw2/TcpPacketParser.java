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
//    private int timeStamp;
    private int dataOffset;
    private int dataLen;
    private int hdrLen;



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

    private int extractDataOffset(byte b) {
        return (b >> 4) & 0xf;
    }

    private int extractMSS(byte[] b) {
        int filter = 0x0204;
        byte[] pat;// = Arrays.copyOfRange(b, 0,2);
        int i;
        for (i = 0; i < b.length - 1; i += 1) {
            pat = Arrays.copyOfRange(b, i, i+2);
            if ((filter &  byteArrayToInt(pat)) == filter) {
                i += 2;
                break;
            }
        }
        pat =  Arrays.copyOfRange(b, i, i+2);
        return byteArrayToInt(pat);

    }

    private int extractWindowScale(byte[] b) {
        int filter = 0x0303;
        byte[] pat;
        int i;
        for (i = 0; i < b.length - 1; i += 1) {
            pat = Arrays.copyOfRange(b, i, i+2);
            if ((filter &  byteArrayToInt(pat)) == filter) {
                i += 2;
                break;
            }
        }
        return b[i];

    }

    public TcpPacketParser(byte [] frame, long timeStamp){
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

        this.dataOffset = extractDataOffset(tcpPacketArray[12]);
        this.hdrLen = dataOffset * 4;

        this.dataLen = tcpPacketArray.length - hdrLen;

        subArr = Arrays.copyOfRange(tcpPacketArray, 14, 16);
        this.windowSize = byteArrayToInt(subArr);

//        subArr = Arrays.copyOfRange(tcpPacketArray, 20, tcpPacketArray.length);
//        this.timeStamp = extractTimeStamp(subArr);


        int segmentLen = tcpPacketArray.length;
        //int framelen = frame.length;

        TcpFlowPacket fPacket = new TcpFlowPacket(sourcePort,destinationPort, seqNo,
                                    ackNo, dataLen, segmentLen, flags, windowSize, timeStamp);


        int srcDestKey = sourcePort*27 + destinationPort;
        int destSrcKey = destinationPort*27 + sourcePort;


        //FLOW COUNT evaluation
        if ((flags & SYN) == SYN && (flags & ACK) != ACK) {
            int state = SYN;
            flowCountHash.remove(srcDestKey);
            flowCountHash.put(srcDestKey, state);
            TcpFlow flow = new TcpFlow(sourcePort, destinationPort);
            tcpFlowHashMap.put(srcDestKey, flow);
            subArr = Arrays.copyOfRange(tcpPacketArray, 20, tcpPacketArray.length);
            int maxSegmentSize =  extractMSS(subArr);
            flow.setMSS(maxSegmentSize);
            int winScale = extractWindowScale(subArr);
            flow.setWinScale(winScale);
            flow.setInitialWindowSize(windowSize);
        }

        if ((flags & SYN) == SYN && (flags & ACK) == ACK) {

            if (flowCountHash.containsKey(destSrcKey)) {
                int state = flowCountHash.get(destSrcKey);
                if (state == SYN) {
                    state = SYN|ACK;
                    flowCountHash.remove(destSrcKey);
                    flowCountHash.put(destSrcKey, state);
                }
            }
        }

        if ((flags & SYN) != SYN && (flags & ACK) == ACK) {
           if (flowCountHash.containsKey(srcDestKey)) {
                int state = flowCountHash.get(srcDestKey);
                if (state == (SYN|ACK)) {
                    state = ACK;
                    flowCountHash.remove(srcDestKey);
                    flowCountHash.put(srcDestKey, state);

                }
            }
        }

        //Don't confuse, it is pushing packets in same flow.

        if (tcpFlowHashMap.containsKey(srcDestKey))
            tcpFlowHashMap.get(srcDestKey).push(fPacket);

        if (tcpFlowHashMap.containsKey(destSrcKey))
            tcpFlowHashMap.get(destSrcKey).push(fPacket);

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
