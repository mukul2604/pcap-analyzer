package PartC;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapHeader;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.nio.JMemory;
import org.jnetpcap.packet.JRegistry;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.protocol.tcpip.Tcp;

import java.nio.ByteBuffer;
import java.util.*;


class timeComparator implements Comparator<HttpFlow> {
    public int compare(HttpFlow flow1, HttpFlow flow2) {
        return  (int)(flow1.getTimeStamp() - flow2.getTimeStamp());
    }
}

public class HttpAnalyzerMain {
    private long intialTimeStamp = 0;
    private long finalTimeStamp = 0;
    private long approxDataPerFlow = 0;
    private  String serverPort = null;

 //   private float protocol = 0;

    public static final int SYN = 0x002;
    public static final int ACK = 0x010;
    public static final int PUSH = 0x008;
    public static final int FIN = 0x001;
    public static final float HTTP1_0 = 1.0f;
    public static final float HTTP1_1 = 1.1f;
    public static final float HTTP2_0 = 2.0f;
    //public static final int SINGLE_FLOW = 1;

    public static long tcpSentCount = 0;
    public static long tcpSentTotalData = 0;
    public static HashMap<Integer, Integer> flowCountHash = new HashMap<>();
    public static HashMap<Integer, HttpFlow> httpFlowHashMap = new HashMap<>();

    public void packetFlowInfoDump() {
        List<HttpFlow>  flowList = new ArrayList<>();
        for (Integer key: httpFlowHashMap.keySet()) {
            HttpFlow flow = httpFlowHashMap.get(key);
            flowList.add(flow);
        }
        Collections.sort(flowList, new timeComparator());
        for (HttpFlow flow: flowList) {
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

    public void analyze(String args, boolean dumpDetailedInfo) {
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
                ByteBuffer frameBuffer = ByteBuffer.allocate(packet.size());
                packet.transferTo(frameBuffer);
                long frameNumber = packet.getFrameNumber();
                long tsmsecs = packet.getCaptureHeader().timestampInMillis();
                if(this.intialTimeStamp == 0) {
                    this.intialTimeStamp = tsmsecs;
                }
                HttpPacketParser httpPacketParser = new HttpPacketParser(frameBuffer.array(), tsmsecs, frameNumber);
                finalTimeStamp = tsmsecs;
            }
        }

        if (dumpDetailedInfo)
            packetFlowInfoDump();

        this.approxDataPerFlow = (tcpSentTotalData / httpFlowHashMap.size());
        flush();
        pcap.close();
    }

    public static void flush() {
        httpFlowHashMap.clear();
        flowCountHash.clear();
    }
    public long timeDeltaMsec() {
        return (finalTimeStamp - intialTimeStamp);
    }




    public long getApproxDataPerFlow() {
        return approxDataPerFlow;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
}
