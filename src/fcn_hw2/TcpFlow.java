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
    private List <Long> timeStampList = new ArrayList<>();

    private int sourcePort;
    private int destinationPort;
    private int MSS;
    private int winScale;
    private long RTO = 0;
    private long iRTT = 0;
    private int ACK_FINACK = 2;
    private int initialWindowSize;


    private ConcurrentHashMap<Long, TcpFlowPacket> ackHash = new ConcurrentHashMap<>();
    private HashMap<Long, TcpFlowPacket> dupAckHash = new HashMap<>();
   // private HashMap<Integer, Float> timeStampHash = new HashMap<>();

    protected int FastRetransmit = 0;
    protected int reTransmit = 0;

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    private int removeDuplicates(long[] A) {
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

    private long [] listToArrayLong(List list) {
        long[] ret = new long[list.size()];
        Iterator<Long> iter  = list.iterator();
        for (int i=0; iter.hasNext(); i++) {
            ret[i] = iter.next();
        }
        return ret;
    }

    private long [] deltaArray(long []  arr) {
        int i;
        List<Long> temp = new ArrayList();
        for (i = 0 ; i < arr.length-3; i+=2) {
            temp.add(arr[i+1] - arr[i]);
        }
        return listToArrayLong(temp);
    }

    private float RTT(float  oldRtt, long newSample) {
        return (alpha * oldRtt + (1 - alpha) * newSample);
    }

    private float estimateRTT(long [] deltaArr) {
        float oldRtt = deltaArr[0];
        for (int i = 1; i < deltaArr.length-1; i++) {
            oldRtt = RTT(oldRtt, deltaArr[i]);
        }
        return oldRtt;
    }

    public void push(TcpFlowPacket flowPacket) {
        int TRIPLE_DUP_ACK = 3;
        TcpFlowPacket possibleDup = null;
        
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
                TcpFlowPacket dupPacket = dupAckHash.get(flowPacket.getSeqNo());
                int ackVal = dupPacket.getAckCount();
                if (ackVal == TRIPLE_DUP_ACK) {
                    dupAckHash.remove(flowPacket.getSeqNo());
                    this.FastRetransmit += 1;
                } else {
                    if (this.iRTT == 0) {
                        this.iRTT = srcList.get(1).getTimeStamp() - srcList.get(0).getTimeStamp();
                        this.RTO = 2 * this.iRTT;
                    }

                    if (possibleDup != null) {
                        long diff = flowPacket.getTimeStamp() - possibleDup.getTimeStamp();
                        if (diff > RTO) {
                            this.reTransmit += 1;
                            dupAckHash.remove(flowPacket.getSeqNo());
                        }
                    }
                }
            }
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort() == destinationPort) {
            destList.add(flowPacket);
            if (ackHash.containsKey(flowPacket.getAckNo())) {
                TcpFlowPacket sentPacket = ackHash.get(flowPacket.getAckNo());
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
                    TcpFlowPacket acp = dupAckHash.get(flowPacket.getAckNo());
                    acp.setAckCount(value+1);
                    dupAckHash.put(flowPacket.getAckNo(), acp);
                }
            }
        }
    }

    public void setMSS(int MSS) {
        this.MSS = MSS;
    }

    public int getMSS(int MSS) {
        return MSS;
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


    private void printTransactions(int no) {
        int srcBase = 1;
        int destBase = 0;
        TcpFlowPacket srcP = srcList.get(no+srcBase);
        TcpFlowPacket destP = destList.get(no+destBase);

        System.out.println("Transaction Number: " + no);
        System.out.println("SeqNo: " + srcP.getSeqNo() + " AckNo: " + srcP.getAckNo()  + " Window Size: " +
                (srcP.getWindowSize() << getWinScale()));
        System.out.println("SeqNo: " + destP.getSeqNo() + " AckNo: " + destP.getAckNo()  + " Window Size: " +
                (destP.getWindowSize() << getWinScale()));
    }

    private long[] getDeltaStamps() {
        List timeStampList = getTimeStampList();
        long[] timeStamps = listToArrayLong(timeStampList);
        long[] deltaStamps = deltaArray(timeStamps);
        return deltaStamps;
    }

    private float getEstimatedRtt() {
        // Get Estimated RTT
        return estimateRTT(getDeltaStamps());
    }

    private float empiricalThroughput() {
        int i;
        long totalTime;
        long totalData = 0;

        totalTime = srcList.get(srcList.size()-1).getTimeStamp() - srcList.get(0).getTimeStamp();

        for(i=0; i< srcList.size(); i++) {
            totalData += srcList.get(i).getSegmentLen();
        }

        return  ((((totalData*8.0f) / totalTime) * 1000) / 1024) / 1024;
    }

    private float theoreticalThroughPut(float RTT) {
        double constant = Math.sqrt(3/2);
        float lossrate = ((srcList.size() - ACK_FINACK - ackList.size()) * 1.0f) / srcList.size();
        float rhoroot = (float) Math.sqrt(lossrate);

        if (rhoroot == 0) {
            return ((initialWindowSize * 8000.0f) / RTT)/1024;
        } else {
            return (float) (((MSS * constant * 8000.0f) / (rhoroot * RTT))/1024);
        }
    }

    public void dumpInfo() {
        float rTTE = getEstimatedRtt();

        System.out.println("Source Port: " + sourcePort + " Destination Port: " +
                destinationPort);

        for (int i = 1; i <= 2; i++) {
            printTransactions(i);
        }

        float lossRate = ((srcList.size() - ACK_FINACK - ackList.size()) * 1.0f) / srcList.size();
        System.out.printf("Sender: %d\tReceived: %d\n", srcList.size() - ACK_FINACK, ackList.size());
        System.out.printf("Loss rate: %.3f\n", lossRate);

        System.out.println("Number of fast re-transmissions: " + FastRetransmit);
        System.out.println("Number of re-transmissions: " + reTransmit);
        System.out.println("Estimated rtt: " + rTTE + " msecs.");
        System.out.printf("Empirical Throughput: %.2f Mbps\n", empiricalThroughput());
        System.out.printf("Theoretical Throughput: %.2f Kbps \n", theoreticalThroughPut(rTTE));
    }

    public int getWinScale() {
        return winScale;
    }

    public void setWinScale(int winScale) {
        this.winScale = winScale;
    }

    public int getInitialWindowSize() {
        return initialWindowSize;
    }

    public void setInitialWindowSize(int initialWindowSize) {
        this.initialWindowSize = initialWindowSize;
    }
}
