package fcn_hw2;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mukul on 3/4/17.
 */
public class TcpFlow {
    public List <TcpFlowPacket> srcList = new ArrayList<TcpFlowPacket>();
    public List <TcpFlowPacket> destList = new ArrayList<TcpFlowPacket>();
    private int sourcePort;
    private int destinationPort;

    public TcpFlow (int src, int dest) {
        this.sourcePort = src;
        this.destinationPort =dest;
    }

    public void push(TcpFlowPacket packet) {
        if (packet.getSourcePort()== sourcePort && packet.getDestinationPort() == destinationPort) {
            srcList.add(packet);
        } else if (packet.getDestinationPort() == sourcePort && packet.getSourcePort()== destinationPort) {
            destList.add(packet);
        }
    }
}
