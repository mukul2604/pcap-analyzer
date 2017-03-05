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
    private HashMap<Long, Long> seqNoHash = new HashMap<>();

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    public void push(TcpFlowPacket flowPacket) {

        // if (seqNoHash.containsKey(flowPacket.getAckNo())) return;

        if (flowPacket.getSourcePort()== sourcePort &&
            flowPacket.getDestinationPort() == destinationPort) {
            srcList.add(flowPacket);
            seqNoHash.put(flowPacket.getAckNo(), flowPacket.getSeqNo());
        } else if (flowPacket.getDestinationPort() == sourcePort &&
                   flowPacket.getSourcePort()== destinationPort) {
            destList.add(flowPacket);
            seqNoHash.put(flowPacket.getAckNo(), flowPacket.getSeqNo());
        }
    }

    public List getSrcList() {
        return  srcList;
    }

    public List getDestList() {
        return destList;
    }
}
