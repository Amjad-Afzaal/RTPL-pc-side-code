/*
 * Copyright 2010 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
 * All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 3 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 3 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Faisal Aslam 
 * (aslam AT informatik.uni-freibug.de or studentresearcher AT gmail.com)
 * if you need additional information or have any questions.
 */
package takatuka.optimizer.cpGlobalization.logic.file;

import takatuka.classreader.dataObjs.ClassFileController;
import java.io.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.StartMeAbstract;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.logAndStats.StatsHolder;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.io.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class OptimClassFileWriter extends ClassFileWriter {

    protected int name = 0;
    protected int totalFile = 0;
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    public long totalBytesWritten = 0;

    public OptimClassFileWriter(String outputDirectory) throws Exception {
        super(outputDirectory);
    }

    public String createDestinationName(boolean isclass) {
        if (isclass) {
            return getOutputDirectory() + "/" + name + ".tuk";
        } else {
            return getOutputDirectory() + "/" + name + ".txt";
        }
    }

    protected String writeClasses(BufferedByteCountingOutputStream buff) throws
            Exception {
        ClassFileController cfcont = ClassFileController.getInstanceOf();
        String ret = "";
        int jump = -1; //5000;
        int size = cfcont.getCurrentSize();
        totalFile = 0;
        //Miscellaneous.println("******************** "+buff.numberOfBytesWritten());
        while (totalFile < size) {

            for (int loop = 0; (jump < 0 || loop < jump) && totalFile < size;
                    loop++) {
                ClassFile.currentClassToWorkOn = (ClassFile) cfcont.get(
                        totalFile);
//                Miscellaneous.println(ClassFile.currentClassToWorkOn.getClassName());
                ret = ret + ClassFile.currentClassToWorkOn.writeSelected(buff);
                if (!ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
                    ret = "";
                }
                totalFile++;
            }
            name++;
            if (totalFile % 5000 == 0) {
                Miscellaneous.println(" Files written = " + totalFile);
            }
        }
        if (ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
        //writeFile(new File(createDestinationName(false)), Creator.print());
        }
        return ret;
    }

    protected String writeGlobalPool(BufferedByteCountingOutputStream buff) throws
            FileNotFoundException,
            IOException,
            Exception {
        Miscellaneous.println("Writing common constant pool");
        String ret = "";
        ret = ret + GlobalConstantPool.getInstanceOf().writeSelected(buff);
//        if (debug.equals("true"))
        //writeFile(new File(createDestinationName(false)),
        //        (new GlobalConstantPool()).print());
        //name++;
        logHolder.addLog("Total number of all constant pool enteries =" +
                GlobalConstantPool.getInstanceOf().
                getTotalEntriesIncludingDuplicates(), true);

        logHolder.addLog("Global constant Pool enteries =" +
                GlobalConstantPool.getInstanceOf().getCurrentSize());
        return ret;
    }

    @Override
    public void writeAll() throws FileNotFoundException,
            IOException, Exception {
        String ret = "";
        logHolder.addLog("Writing class files in Tuk", true);

        FileOutputStream fstream = new FileOutputStream(new File(
                createDestinationName(true)));
        BufferedByteCountingOutputStream buff = new BufferedByteCountingOutputStream(fstream);

        ret = writeGlobalPool(buff);
        if (!ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
            ret = "";
        }
        ret = ret + writeClasses(buff);
        buff.flush();
        fstream.flush();
        fstream.close();
        buff.close();

        if (ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
            writeFile(new File(createDestinationName(false)), ret);
        }
        logHolder.addLog("Total class files written=" + totalFile, true);
        totalBytesWritten = buff.numberOfBytesWritten();
        StatsHolder.getInstanceOf().addStat("Tuk file size", totalBytesWritten);
        double percentReduced = 100.0*(
                ((double)(ClassFileReader.getTotalLengthOfInput()-totalBytesWritten))/
                (double)ClassFileReader.getTotalLengthOfInput());
        StatsHolder.getInstanceOf().addStat("total Java Binary reduction",
                StartMeAbstract.roundDouble(percentReduced,2)+"%");
        logHolder.addLog("*** total Java Binary reduction ="+
                StartMeAbstract.roundDouble(percentReduced, 2)+"%. " +
                "and total Tuk file size (Bytes) ="+ totalBytesWritten +
                " COOL Hah!", true);
  
    }

    public static void main(String args[]) {
        ClassFileController cfcont = ClassFileController.getInstanceOf();
        // String name = null;
        int jump = 500;
        int size = 16470;
        int index = 0;
        while (index < size - 1) {
            Miscellaneous.println(index);
            for (int loop = 0; loop < jump && index < size; loop++) {
                index++;
                Miscellaneous.println("innerloop =" + loop + ", " + index);
            }

        }
    }
}
