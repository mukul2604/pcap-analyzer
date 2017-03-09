package PartC;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mukul on 3/4/17.
 */

public class HttpFlow {

    private int sourcePort;
    private int destinationPort;

    private ConcurrentHashMap<Long, List<HttpFlowPacket>> srcAckHash = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, List<HttpFlowPacket>> destAckHash = new ConcurrentHashMap<>();


    public HttpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
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

    public ConcurrentHashMap getSrcAckHash(){
        return srcAckHash;
    }
    public ConcurrentHashMap getDestAckHash(){ return destAckHash; }
    public int getSourcePort(){
        return sourcePort;
    }
    public int getDestinationPort() {
        return  destinationPort;
    }

    private void printPacketInfo(HttpFlowPacket packet) {
        if (packet.getHttpPayload() == null) {
            System.out.println("Source: " + packet.getSourcePort() + " Destination:" +
                    packet.getDestinationPort());
            System.out.println("SeqNo: " + packet.getSeqNo() + " AckNo: " + packet.getAckNo());
            return;
        }

        String temp[] = packet.getHttpPayload().split("\r\n");
        for (String str : temp) {
            if (str.contains("HTTP")) {
                System.out.println("Source: " + packet.getSourcePort() + " Destination:" +
                        packet.getDestinationPort());
                System.out.println("SeqNo: " + packet.getSeqNo() + " AckNo: " + packet.getAckNo());
                System.out.println("HTTP Request: " + str);
                break;
            } else {
                System.out.println("SeqNo: " + packet.getSeqNo() + " AckNo: " + packet.getAckNo());
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

        for (Long key : srcKeyList) {
            List<HttpFlowPacket> srcList =  srcAckHash.get(key);
            // Collections.sort(srcList,;
            int i = 0;
            while (i < srcList.size()){
                HttpFlowPacket srcPacket = srcList.get(i);
                printPacketInfo(srcPacket);
                i++;
            }
        }

        for (Long key : destKeyList) {
            List<HttpFlowPacket> destList =  destAckHash.get(key);
            // Collections.sort(srcList,;
            int i = 0;
            try {
            while (i < destList.size()){
                HttpFlowPacket destPacket = destList.get(i);
                printPacketInfo(destPacket);
                i++;
            }}  catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    public void dumpInfo() {
        dumpPacketListInfo(srcAckHash, destAckHash);
    }


}
