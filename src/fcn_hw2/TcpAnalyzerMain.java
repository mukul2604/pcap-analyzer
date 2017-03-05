package fcn_hw2;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class TcpAnalyzerMain {
    private static int tcpCount = 0;
    public static final int SYN = 0x002;
    public static final int ACK = 0x010;

    public static HashMap<Integer, Integer> flowCountHash = new HashMap<>();
    public static HashMap<Integer, TcpFlow> tcpFlowHashMap = new HashMap<>();

    public static void packetFlowInfoDump() {
        for (Integer key: tcpFlowHashMap.keySet()) {
            System.out.println("=============================================================");
//            System.out.println("Source List: " + tcpFlowHashMap.get(key).getSrcList().size());
//            System.out.println("Destination List: " + tcpFlowHashMap.get(key).getDestList().size());
            TcpFlow flow = tcpFlowHashMap.get(key);
            HashMap ackHash = flow.getackHash();
//            System.out.println("AckHash: " + ackHash.size());
            TcpFlowPacket p1 = (TcpFlowPacket) flow.getSrcList().get(2);
            TcpFlowPacket p2 = (TcpFlowPacket) flow.getSrcList().get(3);
            System.out.println("Source Port: " + flow.getSourcePort() + " Destination Port: " +
                                flow.getDestinationPort());
            System.out.println("First Transaction:");
            System.out.println("SeqNo: " + p1.getSeqNo() + " AckNo: " + p1.getAckNo()  + " Window Size: " +
                                p1.getWindowSize());
            System.out.println("Second Transaction:");
            System.out.println("SeqNo: " + p2.getSeqNo() + " AckNo: " + p2.getAckNo()  + " Window Size: " +
                    p2.getWindowSize());
            float lossRate =  (flow.getSrcList().size() * 1.0f)/ ackHash.size();
            System.out.printf("Loss Rate: %.2f\n", lossRate);
        }
    }

    public  static int flowCount() {
        int count = 0;
        for(Integer key: flowCountHash.keySet()) {
            if (flowCountHash.get(key) == ACK) {
                    count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        final String FILE_NAME = "/home/cloudera/workspace/fcn_hw2/src/fcn_hw2/assignment2.pcap";
        StringBuilder errBuf = new StringBuilder(); // For any error msgs

        Pcap pcap = Pcap.openOffline(FILE_NAME, errBuf);

        if (pcap == null) {
            System.err.printf("Error while opening file for capture: "
                    + errBuf.toString());
            return;
        }


        Tcp tcp = new Tcp();
        PcapHeader hdr = new PcapHeader(JMemory.POINTER);
        JBuffer buf = new JBuffer(JMemory.POINTER);
        int id = JRegistry.mapDLTToId(pcap.datalink());


        while (pcap.nextEx(hdr, buf) == Pcap.NEXT_EX_OK) {
            PcapPacket packet = new PcapPacket(hdr, buf);
            packet.scan(id);
            if (packet.hasHeader(tcp)) {
                tcpCount++;
                ByteBuffer frameBuffer = ByteBuffer.allocate(packet.size());
                packet.transferTo(frameBuffer);
                TcpPacketParser tcpPacketParser = new TcpPacketParser(frameBuffer.array());
                tcpPacketParser.ackNo();
            }

        }

        System.out.println("TCP Flow Count: "+flowCount());
        packetFlowInfoDump();
       // System.out.printf("Number of tcp Packets:%d\n", tcpCount);
        pcap.close();
    }
}
