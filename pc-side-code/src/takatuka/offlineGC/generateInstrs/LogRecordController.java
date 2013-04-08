/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.generateInstrs;

import java.util.*;
import takatuka.classreader.logic.logAndStats.LogHolder;

/**
 * <p>Title: </p>
 * <p>Description:
 * Offline GC log recorder.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LogRecordController {

    private static final LogRecordController myObj = new LogRecordController();
    /**
     * All the freed instructions.
     */
    private TreeSet<LogRecord> freeInstrRecordTree = new TreeSet<LogRecord>();
    /**
     * New instructions that are also freed.
     */
    private TreeSet<LogRecord> newInstrRecordTree = new TreeSet<LogRecord>();
    /**
     * New instructions that are not freed.
     */
    private TreeSet<LogRecord> newInstrNotFreedRecordTree = new TreeSet<LogRecord>();

    private LogRecordController() {
    }

    /**
     *
     * @return
     */
    public static LogRecordController getInstanceOf() {
        return myObj;
    }

    public void addFreeInstrRecord(LogRecord logRecord) {
        freeInstrRecordTree.add(logRecord);
    }

    public void addNewInstrRecord(LogRecord logRecord) {
        newInstrRecordTree.add(logRecord);
    }

    public void addNewInstrNotFreedRecord(LogRecord logRecord) {
        newInstrNotFreedRecordTree.add(logRecord);
    }

    private void generateFreeInstrLogPerMethod() {
        HashMap<String, Integer> methodToNoOfInstFreeRecord = GenerateInstrsForOfflineGC.getInstrFreePerMethod();
        Set<String> keys = methodToNoOfInstFreeRecord.keySet();
        Iterator<String> it = keys.iterator();
        LogHolder.getInstanceOf().addLogHeading("Number of Free GC Instr Per Method",
                LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        while (it.hasNext()) {
            String key = it.next();
            int numberOfInstrFreed = methodToNoOfInstFreeRecord.get(key);
            if (numberOfInstrFreed > 0) {
                LogHolder.getInstanceOf().addLog(key + " = " + numberOfInstrFreed,
                        LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
            }
        }
    }

    public void generateLog() {

        LogHolder.getInstanceOf().addLogHeading("General", LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        generateFreeInstrLogPerMethod();
        LogHolder.getInstanceOf().addLogHeading("Instruction Freeing Information",
                LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        Iterator<LogRecord> freeInstrIt = freeInstrRecordTree.iterator();
        while (freeInstrIt.hasNext()) {
            LogRecord freeInstrLogRecord = freeInstrIt.next();
            freeInstrLogRecord.addLog();
        }
        LogHolder.getInstanceOf().addLogHeading("New ID Generation Log",
                LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        Iterator<LogRecord> newInstrIt = newInstrRecordTree.iterator();
        while (newInstrIt.hasNext()) {
            LogRecord newInstrLogRecord = newInstrIt.next();
            newInstrLogRecord.addLog();
        }
        LogHolder.getInstanceOf().addLogHeading("New IDs Not Freed Log",
                LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        Iterator<LogRecord> newInstrNotFreedIt = newInstrNotFreedRecordTree.iterator();
        while (newInstrNotFreedIt.hasNext()) {
            LogRecord newInstrLogRecord = newInstrNotFreedIt.next();
            newInstrLogRecord.addLog();
        }

    }
}
