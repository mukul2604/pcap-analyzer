package fcn_hw2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlow {
    private List <TcpFlowPacket> srcList = new ArrayList<>();
    private List <TcpFlowPacket> destList = new ArrayList<>();
    private int sourcePort;
    private int destinationPort;
    private HashMap<Long, Long> ackHash = new HashMap<>();

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    public void push(TcpFlowPacket flowPacket) {
        //System.out.println("Src:" + flowPacket.getSourcePort());
        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            srcList.add(flowPacket);
            int val;
            if (flowPacket.getDataLen() == 0) {
                val = 1;
            } else {
                val = flowPacket.getDataLen();
            }
            ackHash.put(flowPacket.getSeqNo() + val, flowPacket.getSeqNo());
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort()== destinationPort) {
            if (ackHash.containsKey(flowPacket.getAckNo())) {
                destList.add(flowPacket);
                ackHash.remove(flowPacket.getAckNo());
            }
        }
    }

    public List getSrcList() {
        return  srcList;
    }

    public List getDestList() {
        return destList;
    }
}
