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
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class TcpAnalyzerMain {
    private static int tcpCount = 0;
    public static final int SYN = 0x002;
    public static final int ACK = 0x010;
    public static final float alpha = 0.875f;

    public static HashMap<Integer, Integer> flowCountHash = new HashMap<>();
    public static HashMap<Integer, TcpFlow> tcpFlowHashMap = new HashMap<>();

    public static int removeDuplicatesNaive(int[] A) {
        if (A.length < 2)
            return A.length;
        int j = 0;
        int i = 1;
        while (i < A.length) {
            if (A[i] == A[j]) {
                i++;
            } else {
                j++;
                A[j] = A[i];
                i++;
            }
        }
        return j + 1;
    }

    public static int [] listToArrayInt(List list) {
        int[] ret = new int[list.size()];
        Iterator<Integer> iter = list.iterator();
        for (int i=0; iter.hasNext(); i++) {
            ret[i] = iter.next();
        }
        return ret;
    }

    public static void packetFlowInfoDump() {
        for (Integer key: tcpFlowHashMap.keySet()) {
            System.out.println("=====================================================");
            TcpFlow flow = tcpFlowHashMap.get(key);
            ConcurrentHashMap ackHash = flow.getackHash();
            List timeStampList = flow.gettimeStampList();
            int [] timeStamps =  listToArrayInt(timeStampList);

            int len = removeDuplicatesNaive(timeStamps);
            int [] uniqueStamps = new int[len];
            System.arraycopy(timeStamps, 0, uniqueStamps, 0 , len);


            System.out.println("Source Port: " + flow.getSourcePort() + " Destination Port: " +
                                flow.getDestinationPort());

            for (int i = 1; i <= 2; i++) {
                flow.printTransactions(i);
            }

            int lossRate =   (ackHash.size() + flow.FastRetransmit) ;// flow.getSrcList().size();
            System.out.printf("Loss: %d\n", lossRate);//flow.getSrcList().size() - flow.ackList().size());

            System.out.println("Number of fast re-transmission: " + flow.FastRetransmit);
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

    public static float RTTE(float  oldRtt, int newSample) {
        return (alpha * oldRtt + (1 - alpha) * newSample);
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
        System.out.printf("Number of tcp Packets:%d\n", tcpCount);
        pcap.close();
    }
}
