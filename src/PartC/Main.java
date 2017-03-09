package PartC;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static PartC.HttpAnalyzerMain.HTTP1_0;
import static PartC.HttpAnalyzerMain.HTTP1_1;
import static PartC.HttpAnalyzerMain.HTTP2_0;
import static PartC.HttpAnalyzerMain.tcpSentCount;
import static PartC.HttpAnalyzerMain.tcpSentTotalData;

/**
 * Created by mukul on 3/8/17.
 */
class ratioComparator implements Comparator<HttpAnalyzerMain> {
    public int compare(HttpAnalyzerMain a, HttpAnalyzerMain b) {
        return  (int)(a.getApproxDataPerFlow() - b.getApproxDataPerFlow());
    }
}
public class Main {
    public static void compareAndPrint(List<HttpAnalyzerMain> analyzerMainList) {
        Collections.sort(analyzerMainList, new ratioComparator());
        System.out.printf("Port: %s  HTTP Protocol: HTTP%.1f\n", analyzerMainList.get(0).getServerPort(),  HTTP1_0);
        System.out.printf("Port: %s  HTTP Protocol: HTTP%.1f\n", analyzerMainList.get(1).getServerPort(),  HTTP1_1);
        System.out.printf("Port: %s  HTTP Protocol: HTTP%.1f\n", analyzerMainList.get(0).getServerPort(),  HTTP2_0);
        System.out.println("=========================================================");
    }

    public static  void main(String[] args) {
        List<HttpAnalyzerMain> analyzerMainList = new ArrayList<>();
        HttpAnalyzerMain analyzer8092 = new HttpAnalyzerMain();
        analyzer8092.setServerPort("8092");
        analyzer8092.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8092.pcap", true);
        System.out.println("Server Port: 8092");
        System.out.println("Total Time: " + analyzer8092.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData/1024 + " Kbytes");
        System.out.println("=========================================================");
        analyzerMainList.add(analyzer8092);
        tcpSentCount = 0;
        tcpSentTotalData = 0;

        HttpAnalyzerMain analyzer8093 = new HttpAnalyzerMain();
        analyzer8093.setServerPort("8093");
        analyzer8093.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8093.pcap", false);
        System.out.println("Server Port: 8093");
        System.out.println("Total Time:" + analyzer8093.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData/1024 + " Kbytes");
        System.out.println("=========================================================");
        analyzerMainList.add(analyzer8093);
        tcpSentCount = 0;
        tcpSentTotalData = 0;

        HttpAnalyzerMain analyzer8094 = new HttpAnalyzerMain();
        analyzer8094.setServerPort("8094");
        analyzer8094.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8094.pcap", false);
        System.out.println("Server Port: 8094");
        System.out.println("Total Time: " + analyzer8094.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData/1024 + " Kbytes");
        System.out.println("=========================================================");
        analyzerMainList.add(analyzer8094);

        compareAndPrint(analyzerMainList);
    }
}
