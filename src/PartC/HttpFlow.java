package PartC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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

    private ConcurrentHashMap<Long, List<HttpFlowPacket>> srcAckHash = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<HttpFlowPacket>> destAckHash = new ConcurrentHashMap<>();


    public HttpFlow (int src, int dest, long timestamp) {
        this.sourcePort = src;
        this.destinationPort =dest;
        this.timeStamp = timestamp;
    }

    public void push(HttpFlowPacket flowPacket) {
//        if (sourcePort != 56689 && sourcePort != 8092) return;
//        if (destinationPort != 56689 && destinationPort != 8092) return;

        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            int val;
            if (flowPacket.getDataLen() == 0) {
                val = 1;
            } else {
                val = flowPacket.getDataLen();
            }
            
            if (srcAckHash.containsKey(flowPacket.getSeqNo() + val)) {
                srcAckHash.get(flowPacket.getSeqNo()).add(flowPacket);
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
        System.out.println("Source: " + packet.getSourcePort() + " Destination: " +
                packet.getDestinationPort() +
               " SeqNo: " + packet.getSeqNo() + " AckNo: " + packet.getAckNo() + " Timestamp: " + packet.getTimeStamp());

        if (packet.getHttpPayload() == null) return;

        String temp[] = packet.getHttpPayload().split("\r\n");
        for (String str : temp) {
            if (str.contains("HTTP")) {
                System.out.println("HTTP Request: " + str);
                break;
            }
        }
    }

    private void dumpPacketList(List<Long>  srcKeyList, List <Long> destKeyList) {

        boolean synack = false;

        if (synack == false) {
            printPacketInfo(srcAckHash.get(srcKeyList.get(0)).get(0));
            printPacketInfo(destAckHash.get(destKeyList.get(0)).get(0));
            printPacketInfo(srcAckHash.get(srcKeyList.get(1)).get(0));
            srcKeyList.remove(0);
            srcKeyList.remove(0);
            destKeyList.remove(0);
            synack = true;
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
