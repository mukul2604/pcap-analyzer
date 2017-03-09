package PartC;

/**
 * Created by mukul on 3/8/17.
 */
public class Main {
    public static HttpAnalyzerMain analyzerMain = new HttpAnalyzerMain();

    public static  void main(String[] args) {
        analyzerMain.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8092.pcap");
//        analyzerMain.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8093.pcap");
//        analyzerMain.analyze("/home/cloudera/workspace/fcn_hw2/src/PartC/DumpFile01_8094.pcap");
    }
}
