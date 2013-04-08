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
package takatuka.classreader.logic.logAndStats;

import java.io.*;
import java.util.*;
import takatuka.classreader.logic.*;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class LogHolder {

    private static final LogHolder logHolder = new LogHolder();
    private boolean echo = false;
    public static boolean verboseAllowed = true;
    private static final String LOG_FILE_NAME = "log.txt";
    private static HashMap<String, Vector> fileToLogMap = new HashMap<String, Vector>();

    private LogHolder() {
    }

    public static LogHolder getInstanceOf() {
        return logHolder;
    }

    private String addLogLocal(String log, String fileName, boolean isHeading, boolean echoIt) {
        if (isHeading) {
            String temp = "\n\n\n\t\t***************************************************** \n";
            temp += "\t\t******* " + log.toUpperCase() + " *********\n";
            temp += "\t\t***************************************************** \n\n\n";
            log = temp;
        } else {
            if (!echoIt) {
                log = log + "\n";                
            } else {
                log = "(at=" + StartMeAbstract.getCurrentProgramTime() + ")  " + log + "\n";
            }
        }
        Vector toWriteIn = fileToLogMap.get(fileName);
        if (toWriteIn == null) {
            toWriteIn = new Vector();
            fileToLogMap.put(fileName, toWriteIn);
        }
        toWriteIn.addElement(log);
        if (echoIt && verboseAllowed) {
            Miscellaneous.println(log);
        }
        return log;
    }

    /**
     * By default all the logs are written in file log.txt. However, using
     * this function a set of log can be written in different file(s).
     * 
     * @param log
     * @param fileName the name of the file where the log should be added in.
     * @param echoIt
     */
    public void addLog(String log, String fileName, boolean echoIt) {
        log = addLogLocal(log, fileName, false, echoIt);
    }

    public void addLogHeading(String log, String fileName, boolean echoIt) {
        log = addLogLocal(log, fileName, true, echoIt);

    }

    public void addLogHeading(String log, boolean echoIt) {
        log = addLogLocal(log, LOG_FILE_NAME, true, echoIt);

    }

    /**
     * changes the default behavour of this class for individual logs. It is possible
     * the default behavior set using setEchoDefault is to echo all the blogs but 
     * few blogs are not echoed because of use of this function.
     * @param log
     * @param echoIt
     */
    public void addLog(String log, boolean echoIt) {
        addLog(log, LOG_FILE_NAME, echoIt);
    }

    /**
     * add the blog and echo it depending upon the default behaviour set by 
     * setEchoDefault()...
     * 
     * @param log
     */
    public void addLog(String log) {
        addLog(log, LOG_FILE_NAME, echo);
    }

    /**
     * if echo is true then logs are printed on the screen when they are added
     * otherwise they only appear in
     * log.txt file
     * @param echo
     */
    public void setEchoDefault(boolean echo) {
        this.echo = echo;
    }

    /**
     * write log in the log.txt file
     * This function is called from StartMeAbstract end() function.
     * 
     * Other classes should not call this function but calling it will not 
     * change anything in output.
     */
    public void writeLogs() {
        try {
            Iterator<String> fileNamesIt = fileToLogMap.keySet().iterator();
            while (fileNamesIt.hasNext()) {
                String key = fileNamesIt.next();
                String LOG_FILE = StartMeAbstract.outputWriter.getOutputDirectory()
                        + "/" + key;
                Vector logVec = fileToLogMap.get(key);
                Iterator<String> it = logVec.iterator();
                StringBuffer stringBuffer = new StringBuffer();
                while (it.hasNext()) {
                    stringBuffer.append(it.next());
                }
                ClassFileWriter.writeFile(new File(LOG_FILE), stringBuffer.toString());

            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
