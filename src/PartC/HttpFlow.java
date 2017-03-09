package PartC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static PartC.HttpAnalyzerMain.ACK;
import static PartC.HttpAnalyzerMain.FIN;
import static PartC.HttpAnalyzerMain.PUSH;

/**
 * Created by mukul on 3/4/17.
 */

class sComparator implements Comparator<HttpFlowPacket> {
    public int compare(HttpFlowPacket packet1, HttpFlowPacket packet2) {
        return  (int)(packet1.getSeqNo() - packet2.getSeqNo());
    }
}

public class HttpFlow {

    private int sourcePort;
    private int destinationPort;
    private long timeStamp;
    private List<HttpFlowPacket> reassembledFrameList = new ArrayList<>();

    private ConcurrentHashMap<Long, List<HttpFlowPacket>> srcAckHash = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<HttpFlowPacket>> destAckHash = new ConcurrentHashMap<>();


    public HttpFlow (int src, int dest, long timestamp) {
        this.sourcePort = src;
        this.destinationPort =dest;
        this.timeStamp = timestamp;
    }

    public void push(HttpFlowPacket flowPacket) {
        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            int val;
            if (flowPacket.getDataLen() == 0) {
                val = 1;
            } else {
                val = flowPacket.getDataLen();
            }
            
            if (srcAckHash.containsKey(flowPacket.getSeqNo() + val)) {
                srcAckHash.get(flowPacket.getSeqNo() + val).add(flowPacket);
            } else {
                List <HttpFlowPacket> srcList = new ArrayList<>();
                srcList.add(flowPacket);
                srcAckHash.put(flowPacket.getSeqNo() + val, srcList);
            }
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort() == destinationPort) {
             if (srcAckHash.containsKey(flowPacket.getAckNo())) {
                    if(destAckHash.containsKey(flowPacket.getAckNo())) {
                        destAckHash.get(flowPacket.getAckNo()).add(flowPacket);
                    } else {
                        List <HttpFlowPacket> destList = new ArrayList<>();
                        destList.add(flowPacket);
                        destAckHash.put(flowPacket.getAckNo(), destList);
                    }
            }
        }
    }


    private void printPacketInfo(HttpFlowPacket packet) {
        if (packet.getFlags() == (FIN | ACK) && (packet.getSourcePort() == this.sourcePort)
                && (packet.getDestinationPort() == this.destinationPort)) {
            if (reassembledFrameList.size() == 0 ) {
                return;
            }

            for (HttpFlowPacket pkt: reassembledFrameList) {
                System.out.printf("Frame No: %d Source: %d Destination: %d SeqNo: %d AckNo: %d \n", pkt.getFrameNumber(),
                        pkt.getSourcePort(),
                        pkt.getDestinationPort(), pkt.getSeqNo(), pkt.getAckNo());
            }


            String temp1[] = reassembledFrameList.get(0).getHttpPayload().split("\r\n");

            for (String str : temp1) {
                if (str.contains("HTTP")) {
                    System.out.printf("[HTTP %s: %s]\n", "Response", str);
                    if(reassembledFrameList.size() > 1) {
                        System.out.printf("Reassembled frames[First Frame: %d - Last Frame: %d]\n",
                                reassembledFrameList.get(0).getFrameNumber(),
                                reassembledFrameList.get(reassembledFrameList.size()-1).getFrameNumber());

                    }
                }
            }
            return;
        }

        if (packet.getHttpPayload() == null) return;

        String temp[] = packet.getHttpPayload().split("\r\n");
        for (String str : temp) {
            if (str.contains("HTTP")) {
                String msgType;
                if (str.contains("GET") || str.contains("PUT") || str.contains("POST")
                        || str.contains("DELETE") || str.contains("HEAD")) {
                    msgType = "Request";
                } else {
                    msgType = "Response";
                    reassembledFrameList.add(packet);
                }
                if (msgType.equals("Request")) {
                    System.out.printf("[HTTP %s: %s]\n", msgType, str);
                    System.out.printf("Frame No: %d Source: %d Destination: %d SeqNo: %d AckNo: %d\n", packet.getFrameNumber(),
                            packet.getSourcePort(),
                            packet.getDestinationPort(), packet.getSeqNo(), packet.getAckNo());

                }
                break;
            } else {
                if (packet.getHttpPayload().length() > 100) {
                    reassembledFrameList.add(packet);
                }
            }
        }
    }

    private void dumpPacketList(List<Long>  srcKeyList, List <Long> destKeyList) {

        boolean synAck = false;

        if (synAck == false) {
            printPacketInfo(srcAckHash.get(srcKeyList.get(0)).get(0));
            printPacketInfo(destAckHash.get(destKeyList.get(0)).get(0));
            printPacketInfo(srcAckHash.get(srcKeyList.get(1)).get(0));
            srcKeyList.remove(0);
            srcKeyList.remove(0);
            destKeyList.remove(0);
            synAck = true;
        }
        int length =  Math.max(srcKeyList.size(), destKeyList.size());
        for (int i = 0; i < length ; i ++ ) {
            List<HttpFlowPacket> srcPacketList =  srcAckHash.get(srcKeyList.get(i));
            Collections.sort(srcPacketList, new sComparator());
            List<HttpFlowPacket> destPacketList = null;


            if (i < destKeyList.size()) {
                destPacketList = destAckHash.get(destKeyList.get(i));
                Collections.sort(srcPacketList, new sComparator());
            }



            if (destPacketList == null) {
                int j = 0;
                while (j < srcPacketList.size()) {
                    HttpFlowPacket srcPacket = srcPacketList.get(j);
                    printPacketInfo(srcPacket);
                    j++;
                }
            } else {

                int j = 0;
                while (j < Math.min(srcPacketList.size(), destPacketList.size())) {
                    HttpFlowPacket srcPacket = srcPacketList.get(j);
                    HttpFlowPacket destPacket = destPacketList.get(j);
                    printPacketInfo(srcPacket);
                    printPacketInfo(destPacket);
                    j++;
                }

                if (j <  srcPacketList.size()) {
                    while (j < srcPacketList.size()) {
                        HttpFlowPacket srcPacket = srcPacketList.get(j);
                        printPacketInfo(srcPacket);
                        j++;
                    }
                } else {
                    while (j < destPacketList.size()) {
                        HttpFlowPacket destPacket = destPacketList.get(j);
                        printPacketInfo(destPacket);
                        j++;
                    }
                }
            }
        }
    }

    private void dumpPacketListInfo(ConcurrentHashMap<Long, List<HttpFlowPacket>> srcAckHash,
                                    ConcurrentHashMap<Long, List<HttpFlowPacket>> destAckHash) {
        List <Long> srcKeyList = new ArrayList<>();
        List <Long> destKeyList = new ArrayList<>();

        for(Long key: srcAckHash.keySet()) {
            srcKeyList.add(key);
        }

        for(Long key: destAckHash.keySet()) {
            destKeyList.add(key);
        }

        Collections.sort(srcKeyList);
        Collections.sort(destKeyList);

        dumpPacketList(srcKeyList, destKeyList);
        return;
      //  dumpPacketList(srcKeyList, destKeyList);

    }

    public void dumpInfo() {
        if (srcAckHash.size() !=0 ) {
            System.out.println("======================================================================================");
            dumpPacketListInfo(srcAckHash, destAckHash);
        }
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
