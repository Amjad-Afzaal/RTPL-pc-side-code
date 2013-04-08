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
package takatuka.classreader.logic;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Fasial Aslam
 * @version 1.0
 */
public abstract class StartMeAbstract {

    private static long StartTime = 0;
    private static final String ENDTIME = "time_taken";
    private static int currentStage = 0;
    //Keep following always ordered. Such that a state comes first has smaller number
    public static final int STATE_FACTORY_SET = 1;
    public static final int STATE_INPUT_VALIDATED = 2;
    public static final int STATE_INIT = 3;
    public static final int STATE_READ_FILES = 4;
    public static final int STATE_EXECUTE = 5;
    public static final int STATE_WRITE = 6;
    private static final StatsHolder statHolder = StatsHolder.getInstanceOf();
    private static final LogHolder logHolder = LogHolder.getInstanceOf();

    //todo should not be static and public
    public static ClassFileWriter outputWriter = null;

    /**
     * any initilization is done here
     * @param args
     * @throws java.lang.Exception
     */
    public abstract void init(String args[]) throws Exception;

    /**
     * we write back files in this function
     * 
     * @throws java.lang.Exception
     */
    public final void write() throws Exception {
        if (ClassFileController.getInstanceOf().getCurrentSize() == 0) {
            Miscellaneous.println("No classfile is found. Program exiting ...");
            Miscellaneous.exit();
        }
        if (outputWriter != null) {
            outputWriter.writeAll();
        } else {
            Miscellaneous.println("not writing file as output write is null");
        }
    }

    /**
     * Anything to do after writing in the output files.
     */
    public void workAfterWrite() {
        //nothing by default
    }
    
    /**
     * we read files in this function
     * @param args
     * @throws java.lang.Exception
     */
    public abstract void read(String args[]) throws Exception;

    /**
     * Here we do the processing on the files we have read. It is basically 
     * the function which perform the computations others are just supporting functions
     * @param args
     * @throws java.lang.Exception
     */
    public abstract void execute(String args[]) throws Exception;

    /**
     * return millisecond since the program has started running
     * @return
     */
    public static final long getCurrentProgramTime() {
        return System.currentTimeMillis() - StartTime;
    }

    /**
     * Just like as we start with start(...). We end with this function named 
     * end ();
     * The default code of this function just print the time it takes to run the 
     * whole program 
     * @param args
     * @throws java.lang.Exception
     */
    public final void end(String args[]) throws Exception { //Todo final at the moment
        long timeDifference = getCurrentProgramTime();
        double seconds = (double) (timeDifference / 1000.0);
        int mins = 0;
        if (seconds > 60) {
            mins = (int) (seconds / 60.0);
            seconds = seconds % 60.0;
        }
        Miscellaneous.println("******* See detail logs and progress in log.txt and stats in stat.properties\n");
        statHolder.addStat(ENDTIME, timeDifference + "");
        logHolder.addLog("PROGRAM ENDs SUCCESSFULLY: total time " + mins + " min, " +
                roundDouble(seconds, 3) + " seconds (or " + timeDifference + " milliseconds)", true);
        logHolder.writeLogs();
        statHolder.writeProperties();
    }

    /**
     * as the name implies you check here if the argument to the program are valid or not
     * @param args
     * @throws java.lang.Exception
     */
    public abstract void validateInput(String args[]) throws Exception;

    public void setFactoryFacade() {
    //do nothing as by default FactoryFacade will be set as a factory
    }

    public abstract void setOutputWriter(String args[]) throws Exception;

    public final ClassFileWriter getOutputWriter() {
        return outputWriter;
    }

    /**
     * It tells what is the point of start function at the moment. It could be
     * STATE_INPUT_VALIDATED, STATE_READ_FILES, STATE_EXECUTE, STATE_WRITE
     * STATE_FACTORY_SET, STATE_INIT
     * 
     * @return
     */
    public static int getCurrentState() {
        return currentStage;
    }



    /**
     * This is from where we start. It is kind of main and should be called from main.
     * This function is final and should not (cannot) be overwirtten. 
     * It calls other function is the following order
     * 
     * setFactoryFacade()
     * validateInput(args);
     * init(args);
     * read(args);
     * execute(args);
     * setOutputWriter(args);
     * write(args);
     * end(args);
     * 
     * @param args
     * @throws java.lang.Exception
     */
    public final void start(String args[]) throws Exception {
        StartTime = System.currentTimeMillis();
        setFactoryFacade();
        StartMeAbstract.currentStage = STATE_FACTORY_SET;
        validateInput(args);
        StartMeAbstract.currentStage = STATE_INPUT_VALIDATED;
        init(args);
        StartMeAbstract.currentStage = STATE_INIT;
        read(args);
        StartMeAbstract.currentStage = STATE_READ_FILES;
        statHolder.addStat("Total size of input", ClassFileReader.getTotalLengthOfInput());
        statHolder.addStat("Total number of files read", ClassFileReader.getTotalFilesRead());
        execute(args);
        StartMeAbstract.currentStage = STATE_EXECUTE;
        setOutputWriter(args);
        write();
        StartMeAbstract.currentStage = STATE_WRITE;
        workAfterWrite();
        end(args);
    }

    public static final double roundDouble(double d, int places) {
        return Math.round(d * Math.pow(10, (double) places)) / Math.pow(10,
                (double) places);
    }
}
