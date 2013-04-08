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
package takatuka.offlineGC.OGI.threads;

import takatuka.offlineGC.DFA.dataObjs.*;
import java.util.*;
import takatuka.offlineGC.generateInstrs.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * - If a thread is started multiple times then ALL  the references used by it cannot be
 * freed.
 *
 * - If two different threads share any references of any kind then that reference cannot
 * be freed.
 *
 * - Finally a reference on which a new thread is created could also never be freed.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FindRefCannotBeFreedDueToThreads {

    private static final FindRefCannotBeFreedDueToThreads myObj = new FindRefCannotBeFreedDueToThreads();
    private FunctionStateRecorder stateRecorder = FunctionStateRecorder.getInstanceOf();
    /**
     * It keep record of all the references that cannot be freed (ever).
     */
    private HashSet<TTReference> refCannotBeFreed = new HashSet<TTReference>();
    private HashMap<FunctionStateKey, HashSet<TTReference>> cacheForRefUsedByAmethodCall = new HashMap<FunctionStateKey, HashSet<TTReference>>();
    private HashSet<TTReference> refUsedOnce = new HashSet<TTReference>();
    private static boolean shouldDebug = false;

    private FindRefCannotBeFreedDueToThreads() {
    }

    static void printDebug(Object obj1, Object obj2) {
        if (shouldDebug) {
            System.out.println(obj1 + ", " + obj2);
        }
    }

    public static FindRefCannotBeFreedDueToThreads getInstanceOf() {
        return myObj;
    }

    public HashSet<TTReference> getRefThatCannotBeFreed() {
        return refCannotBeFreed;
    }

    public boolean cannotFreeNewId(int newId) {
        Iterator<TTReference> it = refCannotBeFreed.iterator();
        while (it.hasNext()) {
            TTReference reference = it.next();
            if (reference.getNewId() == newId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 1) If a thread is called multiple times then mark all the references used by
     * it as not usable.
     * 2) Go through all the threads and if a reference is found in multiple threads
     * then it is mark unusaable.
     */
    public void execute() {
        /**
         * First mark all threads that are invoked multiple times.
         */
        MarkThreadsInvokedMultipleTimes.getInstanaceOf().execute();
        VirtualThreadController vThreadContr = VirtualThreadController.getInstanceOf();
        Collection<VirtualThread> finishThreadCollection = vThreadContr.getAllFinishedThreads();
        Iterator<VirtualThread> vThreadIt = finishThreadCollection.iterator();
        LogHolder.getInstanceOf().addLogHeading("Dealing with threads", LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        while (vThreadIt.hasNext()) {
            VirtualThread vThread = vThreadIt.next();
            LogHolder.getInstanceOf().addLog("current thread=" + vThread, LogRecord.LOG_FILE_FOR_OFFLINE_GC, shouldDebug);
            HashSet<TTReference> allRefUsedByThread = null;
            GCType refOnWhichAThreadisCreated = vThread.getObjectType();
            if (refOnWhichAThreadisCreated != null) {
                /**
                 * Cannot free the refs on which the thread is created.
                 */
                refCannotBeFreed.addAll(refOnWhichAThreadisCreated.getReferences());
            }

            if (vThread.getThreadStartedMultipleTimes()) {
                allRefUsedByThread = getAllTheReferencesUsedByAThread(vThread);
                LogHolder.getInstanceOf().addLog("\t references =" + allRefUsedByThread, LogRecord.LOG_FILE_FOR_OFFLINE_GC, shouldDebug);

                refCannotBeFreed.addAll(allRefUsedByThread);
            }
            /**
             * The following part check if a same reference is used in multiple threads
             * if so then it cannot be freed.
             */
            if (finishThreadCollection.size() == 1) {
                /**
                 * There is one thread hence do not waste CPU cycles as we are done here.
                 */
                return;
            }
            if (allRefUsedByThread == null) {
                allRefUsedByThread = getAllTheReferencesUsedByAThread(vThread);
                LogHolder.getInstanceOf().addLog("\t references =" + allRefUsedByThread, LogRecord.LOG_FILE_FOR_OFFLINE_GC, shouldDebug);
            }
            Iterator<TTReference> it = allRefUsedByThread.iterator();
            while (it.hasNext()) {
                TTReference reference = it.next();
                if (refUsedOnce.contains(reference)) {
                    refCannotBeFreed.add(reference);
                } else {
                    refUsedOnce.add(reference);
                }
            }
        }
        LogHolder.getInstanceOf().addLog("References Cannot be freed =" + refCannotBeFreed, LogRecord.LOG_FILE_FOR_OFFLINE_GC, shouldDebug);
    }

    /**
     * This function returns all the references used by a thread.
     *
     * Here is how it is done.
     * 1) Get all of the thread method calls.
     * 2) get references for each of those thread calls and save them in a set.
     *
     * @param thread
     * @return
     */
    private HashSet<TTReference> getAllTheReferencesUsedByAThread(VirtualThread thread) {
        HashSet<TTReference> ret = new HashSet<TTReference>();
        HashSet<MethodCallInfo> threadMethods = getAllOfThreadMethods(thread);
        Iterator<MethodCallInfo> it = threadMethods.iterator();
        while (it.hasNext()) {
            MethodCallInfo methodCallInfo = it.next();
            printDebug("method Call Info", methodCallInfo);
            ret.addAll(getAllRefUsedByAMethodCall(methodCallInfo));
        }
        return ret;

    }

    private HashSet<MethodCallInfo> getAllOfThreadMethods(VirtualThread vThread) {
        MethodCallInfo startingMethodCallInfo = vThread.getStartingMethodCallInfo();
        Stack<MethodCallInfo> stack = new Stack<MethodCallInfo>();
        stack.add(startingMethodCallInfo);
        HashSet<MethodCallInfo> alreadyVisited = new HashSet<MethodCallInfo>();
        while (!stack.empty()) {
            MethodCallInfo methodCallInfo = stack.pop();
            if (alreadyVisited.contains(methodCallInfo)) {
                continue;
            }
            stack.addAll(methodCallInfo.getAllChildren());
            alreadyVisited.add(methodCallInfo);
        }
        return alreadyVisited;
    }

    /**
     * Returns all the references used by a method call.
     * @param key
     * @return
     */
    private HashSet<TTReference> getAllRefUsedByAMethodCall(FunctionStateKey key) {
        HashSet<TTReference> ret = cacheForRefUsedByAmethodCall.get(key);
        if (ret != null) {
            return ret;
        } else {
            ret = new HashSet<TTReference>();
            cacheForRefUsedByAmethodCall.put(key, ret);
        }
        HashMap<Long, FunctionStateValueElement> stateOfFunctionMap = stateRecorder.getFunctionState(key);
        Collection<FunctionStateValueElement> valueElmCollection = stateOfFunctionMap.values();
        Iterator<FunctionStateValueElement> it = valueElmCollection.iterator();
        while (it.hasNext()) {
            FunctionStateValueElement stateElm = it.next();
            ret.addAll(stateElm.getAllReferenceUsed());
        }
        return ret;
    }
}
