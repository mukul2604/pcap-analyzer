package PartC;

import static PartC.HttpAnalyzerMain.tcpSentCount;
import static PartC.HttpAnalyzerMain.tcpSentTotalData;

/**
 * Created by mukul on 3/8/17.
 */
public class Main {

    public static  void main(String[] args) {
        HttpAnalyzerMain analyzer8092 = new HttpAnalyzerMain();
        analyzer8092.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8092.pcap");
        System.out.println("Port: 8092");
        System.out.println("Total Time: " + analyzer8092.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData + " bytes");


        HttpAnalyzerMain analyzer8093 = new HttpAnalyzerMain();
        analyzer8093.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8093.pcap");
        System.out.println("Port: 8093");
        System.out.println("Total Time:" + analyzer8093.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData + " bytes");

        HttpAnalyzerMain analyzer8094 = new HttpAnalyzerMain();
        analyzer8094.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8094.pcap");
        System.out.println("Port: 8094");
        System.out.println("Total Time: " + analyzer8094.timeDeltaMsec() + " mSecs");
        System.out.println("No. of total Packets Sent from server: " + tcpSentCount);
        System.out.println("Size of total sent data: " + tcpSentTotalData + " bytes");

//        analyzerMain.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8093.pcap");
//        analyzerMain.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8094.pcap");
    }
}
