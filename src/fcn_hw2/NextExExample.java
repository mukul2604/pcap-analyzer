package fcn_hw2;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;

import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.nio.ByteBuffer;

/**
 * This example opens up a capture file found in jNetPcap's installation
 * directory for of the "source" distribution package and iterates over every
 * packet. The example also demonstrates how to property peer
 * PcapHeader, JBuffer and initialize a new
 * PcapPacket object which will contain a copy of the peered
 * packet and header data. The libpcap provide header and data are stored in
 * libpcap private memory buffer, which will be overriden with each iteration of
 * the loop. Therefore we use the constructor in PcapPacket to
 * allocate new memory to store header and packet buffer data and perform the
 * copy. The we
 *
 * @author Mark Bednarczyk
 * @author Sly Technologies, Inc.
 */
public class NextExExample {

    /**
     * Start of our example.
     *
     * @param args
     *          ignored
     */
    public static void main(String[] args) {
        final String FILE_NAME = "/home/cloudera/assignment2.pcap";
        StringBuilder errbuf = new StringBuilder(); // For any error msgs

        /***************************************************************************
         * First - we open up the selected device
         **************************************************************************/
        Pcap pcap = Pcap.openOffline(FILE_NAME, errbuf);

        if (pcap == null) {
            System.err.printf("Error while opening file for capture: "
                    + errbuf.toString());
            return;
        }

        /***************************************************************************
         * Second - we create our main loop and our application We create some
         * objects we will be using and reusing inside the loop
         **************************************************************************/
        Tcp tcp = new Tcp();
        PcapHeader hdr = new PcapHeader(JMemory.POINTER);
        JBuffer buf = new JBuffer(JMemory.POINTER);

        /***************************************************************************
         * Third - we must map pcap's data-link-type to jNetPcap's protocol IDs.
         * This is needed by the scanner so that it knows what the first header in
         * the packet is.
         **************************************************************************/
        int id = JRegistry.mapDLTToId(pcap.datalink());

        /***************************************************************************
         * Fourth - we peer header and buffer (not copy, think of C pointers)
         **************************************************************************/
        while (pcap.nextEx(hdr, buf) == Pcap.NEXT_EX_OK) {

            /*************************************************************************
             * Fifth - we copy header and buffer data to new packet object
             ************************************************************************/
            PcapPacket packet = new PcapPacket(hdr, buf);


            /*************************************************************************
             * Six- we scan the new packet to discover what headers it contains
             ************************************************************************/
            packet.scan(id);

            if (packet.hasHeader(tcp)) {
                packet.getHeader(tcp);
                ByteBuffer packetBuffer = ByteBuffer.allocate(tcp.size());
                tcp.transferTo(packetBuffer);
                byte[] byteArr = packetBuffer.array();
                System.out.println(byteArr);
//                for (byte b: byteArr) {
//                    System.out.println("%d", b);
//                }
                /* System.out.println("ds\n"); */
            }

        }

        /*************************************************************************
         * Last thing to do is close the pcap handle
         ************************************************************************/
        pcap.close();
    }
}
