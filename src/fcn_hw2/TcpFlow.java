package fcn_hw2;



import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static fcn_hw2.TcpAnalyzerMain.*;


/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlow {
    private List <TcpFlowPacket> srcList = new ArrayList<>();
    private List <TcpFlowPacket> destList = new ArrayList<>();
    private List <TcpFlowPacket> ackList = new ArrayList<>();
    private List <Integer> timeStampList = new ArrayList<>();

    private int sourcePort;
    private int destinationPort;
    private ConcurrentHashMap<Long, TcpFlowPacket> ackHash = new ConcurrentHashMap<>();
    private HashMap<Long, Integer> triAckHash = new HashMap<>();
   // private HashMap<Integer, Float> timeStampHash = new HashMap<>();

    protected int FastRetransmit = 0;

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    private int removeDuplicates(int[] A) {
        if (A.length < 2)
            return A.length;
        int j = 0;
        int i = 1;
        while (i < A.length) {
            if (A[i] == A[j]) {
                i++;
            } else {
                j++;
                A[j] = A[i];
                i++;
            }
        }
        return j + 1;
    }

    private int [] listToArrayInt(List list) {
        int[] ret = new int[list.size()];
        Iterator<Integer> iter  = list.iterator();
        for (int i=0; iter.hasNext(); i++) {
            ret[i] = iter.next();
        }
        return ret;
    }

    private void deltaArray(int []  arr) {
        int i;
        for (i = 0 ; i < arr.length-1; i++) {
            arr[i] = arr[i+1] - arr[i];
        }
    }

    public void push(TcpFlowPacket flowPacket) {
        int TRIPLE_DUP_ACK = 3;
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

            ackHash.put(flowPacket.getSeqNo() + val, flowPacket);
           // timeStampHash.put()
            // triple dupAck, if sent packet is found in triAck hash with
            // ackVal = 3 then it means it is fast retransmitted.
            if (triAckHash.containsKey(flowPacket.getSeqNo())) {
                int ackVal = triAckHash.get(flowPacket.getSeqNo());
                if (ackVal == TRIPLE_DUP_ACK) {
                    triAckHash.remove(flowPacket.getSeqNo());
                    this.FastRetransmit += 1;
                }
            }
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort() == destinationPort) {
            destList.add(flowPacket);
            if (ackHash.containsKey(flowPacket.getAckNo())) {
                TcpFlowPacket sentPacket = ackHash.get(flowPacket.getAckNo());
                int timeStamp =  sentPacket.getTimeStamp();
                timeStampList.add(timeStamp);
                //remove all acknowledged packets from ackHash and
                //move to ackList.
                for(Long key: ackHash.keySet()) {  //need concurrentHashMap for this
                    if (ackHash.get(key).getSeqNo() < flowPacket.getAckNo()) {
                        ackList.add(ackHash.get(key));
                        ackHash.remove(key);
                    }
                }
                //put this ack into triple ack hash to track fast retransmission
                triAckHash.put(flowPacket.getAckNo(), 0);
                ackHash.remove(flowPacket.getAckNo());
            }

            if (triAckHash.containsKey(flowPacket.getAckNo())) {
                int value = triAckHash.get(flowPacket.getAckNo());
                if ( value < TRIPLE_DUP_ACK) {
                    triAckHash.remove(flowPacket.getAckNo());
                    triAckHash.put(flowPacket.getAckNo(), value + 1);
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

    public List gettimeStampList() {
        return timeStampList;
    }


    public void printTransactions(int no) {
        int srcBase = 1;
        int destBase = 0;
        TcpFlowPacket srcP = srcList.get(no+srcBase);
        TcpFlowPacket destP = destList.get(no+destBase);

        System.out.println("Transaction Number: " + no);
        System.out.println("SeqNo: " + srcP.getSeqNo() + " AckNo: " + srcP.getAckNo()  + " Window Size: " +
                srcP.getWindowSize());
        System.out.println("SeqNo: " + destP.getSeqNo() + " AckNo: " + destP.getAckNo()  + " Window Size: " +
                destP.getWindowSize());
    }


    public float getEstimatedRtt() {
        // Get Estimated RTT
        List timeStampList = gettimeStampList();
        Collections.sort(timeStampList);
        int [] timeStamps =  listToArrayInt(timeStampList);
        int [] uniqueTimeStamps;
        int len = removeDuplicates(timeStamps);
        uniqueTimeStamps = new int[len];
        System.arraycopy(timeStamps, 0, uniqueTimeStamps, 0 , len);
        deltaArray(uniqueTimeStamps);
        return estimateRTT(uniqueTimeStamps);
    }
}
