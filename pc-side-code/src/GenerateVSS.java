
import java.io.*;
import java.util.*;

/**
 * Go through the file once.
 * --- * means get ready for the data.
 * ----* mean that data is finish.
 * ---- After finishing data we get heading.
 * it has a count that counts that second * encounter.
 * Vector output contains strings output
 * @author aslam
 */
public class GenerateVSS {

    private Vector<AlgoData> output = new Vector<AlgoData>();
    private static String outputFileName = null;

    public static void main(String args[]) {
        if (args.length != 2) {
            System.out.println("Usage: java enerateVSS <inputFileName> <outputFileName>");
            System.exit(1);
        }
        try {
            outputFileName = args[1];
            new GenerateVSS().execute(args[0]);

        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    private int maxDataElm() {
        int max = 0;
        for (int loop = 0; loop < output.size(); loop++) {
            int temp = output.elementAt(loop).data.size();
            if (temp > max) {
                max = temp;
            }
        }
        return max;
    }

    private void execute(String fileName) throws Exception {
        boolean dataStart = false;
        RandomAccessFile input = new RandomAccessFile(fileName, "r");
        AlgoData currentAlgoData = null;
        while (input.getFilePointer() < input.length()) {
            String line = input.readLine().trim();
            //System.out.println(line);

            if (line.length() == 0) {
                continue;
            }
            if (line.equals("*")) {
                dataStart = !dataStart;
                //System.out.println("Start data "+dataStart);
                continue;
            }
            if (!dataStart) {
                //System.out.println("creating a new data list");
                currentAlgoData = new AlgoData(line);
                System.out.println("reading data for =" + line);
                // this should be heading
                output.add(currentAlgoData);
            } else {
                currentAlgoData.data.addElement(line);
            }
        }
        System.out.println("finished reading. Time to write");
        input.close();
        String outString = createStringForFile();
        RandomAccessFile outputFile = new RandomAccessFile(outputFileName, "rw");
        outputFile.writeBytes(outString);
        outputFile.close();
    }

    private String createStringForFile() {
        StringBuffer ret = new StringBuffer();
        boolean endOfData = false;
        int count = 0;
        while (!endOfData) {
            endOfData = true;
            for (int loop = 0; loop < output.size(); loop++) {
                AlgoData algoData = output.elementAt(loop);
                if (count < algoData.data.size()) {
                    endOfData = false;
                    ret.append(algoData.data.elementAt(count) + "\t");
                } else {
                    ret.append("\t");
                }
            }
            count++;
            //System.out.println(count+", "+maxCount);
            ret.append("\n");
        }
        return ret.toString();
    }

    private class AlgoData {

        Vector<String> data = new Vector();

        public AlgoData(String heading) {
            data.addElement(heading);
        }
    }
}
