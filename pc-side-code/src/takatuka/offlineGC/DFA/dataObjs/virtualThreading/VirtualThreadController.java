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
package takatuka.offlineGC.DFA.dataObjs.virtualThreading;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.DFA.dataObjs.GCType;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.VerificationInstruction;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class VirtualThreadController {

    private HashMap<Type, VirtualThread> virtualThreadMap = new HashMap<Type, VirtualThread>();
    private HashMap<Type, VirtualThread> virtualFinishedRunning = new HashMap<Type, VirtualThread>();
    private static final VirtualThreadController myObj = new VirtualThreadController();
    private VirtualThread currentThread = null;

    /**
     * constructor is private
     */
    private VirtualThreadController() {
    }

    /**
     *
     * @return
     */
    public static VirtualThreadController getInstanceOf() {
        return myObj;
    }

    /**
     * 
     * @return
     */
    public Collection<VirtualThread> getAllFinishedThreads() {
        return virtualFinishedRunning.values();
    }

    /**
     * set done status of current thread
     */
    public void doneWithThreadExecution() {
        if (currentThread != null) {
            virtualFinishedRunning.put(currentThread.getObjectType(), currentThread);
        }
        currentThread = null;
    }

    /**
     *
     * @return
     */
    public VirtualThread getNextThreadToRun() {
        if (currentThread != null) {
            Miscellaneous.printlnErr("ERROR: cannot get the new thread util current thread is done running");
            Miscellaneous.exit();
        }
        if (virtualThreadMap.values().size() != 0) {
            currentThread = (VirtualThread) virtualThreadMap.values().toArray()[0];
            virtualThreadMap.remove(currentThread.getObjectType());
        }
        return currentThread;
    }

    /**
     * it creates a virtual thread, if that thread does not already exist.
     *
     * @param type
     * @param method
     * @param lastInstrExecuted
     */
    public void createVirtualThread(GCType type, MethodInfo method,
            GCInstruction lastInstrExecuted) {
        VirtualThread existingVThread = virtualThreadMap.get(type);
        if (existingVThread == null) {
            existingVThread = new VirtualThread(type, method, lastInstrExecuted);
            virtualThreadMap.put(type, existingVThread);
        } else {
            LogHolder.getInstanceOf().addLog("Thread is started multiple times "+existingVThread);
            /**
             * thread already exist
             */
            existingVThread.setThreadStartedMultipleTimes(true);
        }
    }

    private String toStringHelper(HashMap map) {
        String ret = "";
        Collection<VirtualThread> waitingThreadsSet = map.values();
        Iterator<VirtualThread> it = waitingThreadsSet.iterator();
        while (it.hasNext()) {
            VirtualThread vthread = it.next();
            ret = ret + "\n\t" + vthread;
        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "Threads waiting to be run=" + toStringHelper(virtualThreadMap);
        ret += "\nThread finished running=" + toStringHelper(virtualFinishedRunning);
        ret +="\nCurrent thread running="+currentThread;
        return ret;

    }
}
