package fcn_hw2;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import java.nio.ByteBuffer;




public class TcpAnalyzerMain {
    private static int tcpCount = 0;

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
//        JFlowMap map = new JFlowMap();

        int id = JRegistry.mapDLTToId(pcap.datalink());


        while (pcap.nextEx(hdr, buf) == Pcap.NEXT_EX_OK) {

            PcapPacket packet = new PcapPacket(hdr, buf);

            packet.scan(id);

            if (packet.hasHeader(tcp)) {
                tcpCount++;
                ByteBuffer frameBuffer = ByteBuffer.allocate(packet.size());
                packet.transferTo(frameBuffer);
                TcpPacketParser tcpPacketParser = new TcpPacketParser(frameBuffer.array());
                tcpPacketParser.printPacket();
//                break;
            }

        }
        System.out.printf("Number of tcp Packets:%d\n", tcpCount);
        pcap.close();
    }
}
