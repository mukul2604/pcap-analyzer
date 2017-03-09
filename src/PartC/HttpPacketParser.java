package PartC;

import java.math.BigInteger;
import java.util.Arrays;

import static PartC.HttpAnalyzerMain.*;


/**
 * Created by mukul on 2/26/17.
 */

public class HttpPacketParser {
    private int sourcePort;
    private int destinationPort;
    private long seqNo;
    private long ackNo;
    private int flags;
    private int windowSize;
    private int dataOffset;
    private int dataLen;
    private int hdrLen;
    private int segmentLen;


    public static int byteArrayToInt(byte[] b) {
        StringBuilder sb = new StringBuilder(2 * b.length);
        for (byte elem : b) {
            sb.append(String.format("%02x", elem));
        }

        BigInteger val = new BigInteger(sb.toString(), 16);
        return val.intValue();
    }

    public static long byteArrayToLong(byte[] b) {
        StringBuilder sb = new StringBuilder(2 * b.length);
        for (byte elem : b) {
            sb.append(String.format("%02x", elem));
        }

        BigInteger val = new BigInteger(sb.toString(), 16);
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


    public HttpPacketParser(byte[] frame, long timeStamp, long frameNumber) {
        byte[] tcpPacketArray = Arrays.copyOfRange(frame, 34, frame.length);
        byte[] subArr;
        tcpSentCount = 0;
        tcpSentTotalData = 0;
        subArr = Arrays.copyOfRange(tcpPacketArray, 0, 2);

        this.sourcePort = byteArrayToInt(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 2, 4);
        this.destinationPort = byteArrayToInt(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 4, 8);
        this.seqNo = byteArrayToLong(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 8, 12);
        this.ackNo = byteArrayToLong(subArr);

        subArr = Arrays.copyOfRange(tcpPacketArray, 12, 14);
        this.flags = extractFlags(subArr);

        this.dataOffset = extractDataOffset(tcpPacketArray[12]);
        this.hdrLen = dataOffset * 4;

        this.dataLen = tcpPacketArray.length - hdrLen;

        subArr = Arrays.copyOfRange(tcpPacketArray, 14, 16);
        this.windowSize = byteArrayToInt(subArr);

        this.segmentLen = tcpPacketArray.length;

        String httpData = null;
        if (dataLen > 0) {
            try {
                subArr = Arrays.copyOfRange(tcpPacketArray, hdrLen, hdrLen + dataLen);
                httpData = new String(subArr, "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        HttpFlowPacket fPacket = new HttpFlowPacket(sourcePort, destinationPort, seqNo,
                ackNo, dataLen, segmentLen, httpData, flags, windowSize, timeStamp, frameNumber);


        int srcDestKey = sourcePort * 27 + destinationPort;
        int destSrcKey = destinationPort * 27 + sourcePort;


        //FLOW COUNT evaluation
        if ((flags & SYN) == SYN && (flags & ACK) != ACK) {
            int state = SYN;
            flowCountHash.remove(srcDestKey);
            flowCountHash.put(srcDestKey, state);
            HttpFlow flow = new HttpFlow(sourcePort, destinationPort, timeStamp);
            httpFlowHashMap.put(srcDestKey, flow);
        }

        if ((flags & SYN) == SYN && (flags & ACK) == ACK) {
            if (flowCountHash.containsKey(destSrcKey)) {
                int state = flowCountHash.get(destSrcKey);
                if (state == SYN) {
                    state = SYN | ACK;
                    flowCountHash.remove(destSrcKey);
                    flowCountHash.put(destSrcKey, state);
                }
            }
        }

        if ((flags & SYN) != SYN && (flags & ACK) == ACK) {
            if (flowCountHash.containsKey(srcDestKey)) {
                int state = flowCountHash.get(srcDestKey);
                if (state == (SYN | ACK)) {
                    state = ACK;
                    flowCountHash.remove(srcDestKey);
                    flowCountHash.put(srcDestKey, state);

                }
            }
        }

        //Don't confuse, it is pushing packets in same flow.

        if (httpFlowHashMap.containsKey(srcDestKey))
            httpFlowHashMap.get(srcDestKey).push(fPacket);

        if (httpFlowHashMap.containsKey(destSrcKey)) {
            httpFlowHashMap.get(destSrcKey).push(fPacket);
            tcpSentCount += 1;
            tcpSentTotalData += fPacket.getSegmentLen();
        }

    }
}
