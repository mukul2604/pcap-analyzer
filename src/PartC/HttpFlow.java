package PartC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mukul on 3/4/17.
 */

public class HttpFlow {
    private List <HttpFlowPacket> srcList = new ArrayList<>();
    private List <HttpFlowPacket> destList = new ArrayList<>();
    private List <HttpFlowPacket> ackList = new ArrayList<>();
    private List <Long> timeStampList = new ArrayList<>();
    private int sourcePort;
    private int destinationPort;



    private ConcurrentHashMap<Long, HttpFlowPacket> ackHash = new ConcurrentHashMap<>();
    private HashMap<Long, HttpFlowPacket> dupAckHash = new HashMap<>();


    public HttpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    public void push(HttpFlowPacket flowPacket) {
        int TRIPLE_DUP_ACK = 3;
        HttpFlowPacket possibleDup = null;
        
        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            //don't add if contains only  ACK, FIN,ACK
            srcList.add(flowPacket);
            int val;
            if (flowPacket.getDataLen() == 0) {
                val = 1;
            } else {
                val = flowPacket.getDataLen();
            }

            if (ackHash.containsKey(flowPacket.getSeqNo() + val)) {
                possibleDup = ackHash.get(flowPacket.getSeqNo() + val);
                ackHash.remove(flowPacket.getSeqNo() + val);
            }

            ackHash.put(flowPacket.getSeqNo() + val, flowPacket);

           // timeStampHash.put()
            // triple dupAck, if sent packet is found in triAck hash with
            // ackVal = 3 then it means it is fast retransmitted.
            if (dupAckHash.containsKey(flowPacket.getSeqNo())) {
                HttpFlowPacket dupPacket = dupAckHash.get(flowPacket.getSeqNo());
            }
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort() == destinationPort) {
            destList.add(flowPacket);
            if (ackHash.containsKey(flowPacket.getAckNo())) {
                HttpFlowPacket sentPacket = ackHash.get(flowPacket.getAckNo());
                long timeStamp =  sentPacket.getTimeStamp();
                timeStampList.add(timeStamp);
                timeStampList.add(flowPacket.getTimeStamp());
                //remove all acknowledged packets from ackHash and
                //move to ackList.
                for(Long key: ackHash.keySet()) {  //need concurrentHashMap for this
                    if (ackHash.get(key).getSeqNo() < flowPacket.getAckNo()) {
                        ackList.add(ackHash.get(key));
                        ackHash.remove(key);
                    }
                }
                //put this ack into dup ack hash to track fast retransmission
                flowPacket.setAckCount(0);
                dupAckHash.put(flowPacket.getAckNo(), flowPacket);
                ackHash.remove(flowPacket.getAckNo());
            }

            if (dupAckHash.containsKey(flowPacket.getAckNo())) {
                int value = dupAckHash.get(flowPacket.getAckNo()).getAckCount();
                if ( value < TRIPLE_DUP_ACK) {
                    HttpFlowPacket acp = dupAckHash.get(flowPacket.getAckNo());
                    acp.setAckCount(value+1);
                    dupAckHash.put(flowPacket.getAckNo(), acp);
                }
            }
        }
    }


    public List getSrcList() {
        return  srcList;
    }

    public List getDestList() {
        return destList;
    }

    public List ackList() {
        return  ackList;
    }

    public ConcurrentHashMap getackHash(){
        return ackHash;
    }

    public int getSourcePort(){
        return sourcePort;
    }

    public int getDestinationPort() {
        return  destinationPort;
    }

    public List getTimeStampList() {
        return timeStampList;
    }

    private void dumpPacketListInfo(List<HttpFlowPacket> packetList) {
        for(HttpFlowPacket packet: packetList) {
            if (packet.getHttpPayload() == null) continue;
            String temp [] = packet.getHttpPayload().split("\r\n");
            for (String str: temp) {
                if (str.contains("HTTP")){
                    System.out.println("Source: " + packet.getSourcePort() + " Destination:" + packet.getDestinationPort());
                    System.out.println("SeqNo: " + packet.getSeqNo() + " AckNo: " + packet.getAckNo());
                    System.out.println("HTTP Request: " + str);
                    break;
                }
            }
        }

    }

    public void dumpInfo() {
        dumpPacketListInfo(srcList);
        System.out.println("------------------------------------");
        dumpPacketListInfo(destList);
    }


}
