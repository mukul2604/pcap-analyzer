package fcn_hw2;

import org.jnetpcap.protocol.tcpip.Tcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlow {
    private List <TcpFlowPacket> srcList = new ArrayList<>();
    private List <TcpFlowPacket> destList = new ArrayList<>();
    private List <TcpFlowPacket> ackList = new ArrayList<>();

    private int sourcePort;
    private int destinationPort;
    private ConcurrentHashMap<Long, TcpFlowPacket> ackHash = new ConcurrentHashMap<>();
    private HashMap<Long, Integer> triAckHash = new HashMap<>();
    protected int FastRetransmit = 0;

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    public void push(TcpFlowPacket flowPacket) {
        int TRIPLE_DUP_ACK = 3;
        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            srcList.add(flowPacket);
            int val;
            if (flowPacket.getDataLen() == 0) {
                val = 1;
            } else {
                val = flowPacket.getDataLen();
            }
            ackHash.put(flowPacket.getSeqNo() + val, flowPacket);
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort()== destinationPort) {

            destList.add(flowPacket);

            if (ackHash.containsKey(flowPacket.getAckNo())) {
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
                    triAckHash.put(flowPacket.getAckNo(), value+1);
                } else {
                    triAckHash.remove(flowPacket.getAckNo());
                    this.FastRetransmit += 1;
                }
            }
        }
    }

    public List getSrcList() {
        return  srcList;
    }

//    public List getDestList() {
//        return destList;
//    }

    public ConcurrentHashMap getackHash(){
        return ackHash;
    }

    public int getSourcePort(){
        return sourcePort;
    }

    public int getDestinationPort() {
        return  destinationPort;
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
}
