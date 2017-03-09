package PartC;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.nio.ByteBuffer;
import java.util.HashMap;


public class HttpAnalyzerMain {
    private static int tcpCount = 0;
    public static final int SYN = 0x002;
    public static final int ACK = 0x010;
//    public static final float alpha = 0.875f;

    public static HashMap<Integer, Integer> flowCountHash = new HashMap<>();
    public static HashMap<Integer, HttpFlow> httpFlowHashMap = new HashMap<>();

    public void packetFlowInfoDump() {
        for (Integer key: httpFlowHashMap.keySet()) {
            System.out.println("=====================================================");
            HttpFlow flow = httpFlowHashMap.get(key);
            flow.dumpInfo();
        }
    }

    public int flowCount() {
        int count = 0;
        for(Integer key: flowCountHash.keySet()) {
            if (flowCountHash.get(key) == ACK) {
                    count++;
            }
        }
        return count;
    }

    public void analyze(String args) {
        final String FILE_NAME = args; //"/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8092.pcap";
        StringBuilder errBuf = new StringBuilder();

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
                long tsmsecs = packet.getCaptureHeader().timestampInMillis();
                HttpPacketParser httpPacketParser = new HttpPacketParser(frameBuffer.array(), tsmsecs);
//                httpPacketParser.ackNo();
            }

        }


        packetFlowInfoDump();
        System.out.printf("Number of tcp Packets:%d\n", tcpCount);
        pcap.close();
    }
}
