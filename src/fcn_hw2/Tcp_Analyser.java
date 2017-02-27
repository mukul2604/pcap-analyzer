package fcn_hw2;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;
import java.nio.ByteBuffer;


public class Tcp_Analyser {

     public static void main(String[] args) {
        final String FILE_NAME = "/home/cloudera/assignment2.pcap";
        StringBuilder errbuf = new StringBuilder(); // For any error msgs

        Pcap pcap = Pcap.openOffline(FILE_NAME, errbuf);

        if (pcap == null) {
            System.err.printf("Error while opening file for capture: "
                    + errbuf.toString());
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
                packet.getHeader(tcp);
                ByteBuffer packetBuffer = ByteBuffer.allocate(tcp.size());
                tcp.transferTo(packetBuffer);
                byte[] byteArr = packetBuffer.array();
                System.out.println(byteArr);
            }

        }

        /*************************************************************************
         * Last thing to do is close the pcap handle
         ************************************************************************/
        pcap.close();
    }
}
