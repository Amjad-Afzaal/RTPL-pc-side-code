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
 * if you need additionStarted Working on Intra-loop algorithm.al information or have any questions.
 */
package takatuka.offlineGC.OGI.GraphUtil.FTT;

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.VirtualThread;

/**
 * <p>Title: </p>
 * <p>Description:
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodLevelGraphController {

    private static final MethodLevelGraphController myObj = new MethodLevelGraphController();
    private HashMap<Integer, MethodLevelGraphNode> record = new HashMap<Integer, MethodLevelGraphNode>();
    private HashSet<MethodLevelGraphNode> alreadyPrinted = new HashSet<MethodLevelGraphNode>();
    private HashMap<VirtualThread, MethodLevelGraphNode> firstNodePerThread = new HashMap<VirtualThread, MethodLevelGraphNode>();

    /**
     * 
     */
    private MethodLevelGraphController() {
    }

    /**
     *
     * @return
     */
    public static MethodLevelGraphController getInstanceOf() {
        return myObj;
    }

    public boolean empty() {
        return record.size() == 0;
    }
    /**
     * 
     * @return
     */
    public int size() {
        return record.size();
    }

    /**
     * 
     * @param node
     */
    public void addRecord(MethodLevelGraphNode node, VirtualThread thread) {
        if (node == null || record.get(node.getId()) != null) {
            return; //already added or trying to add a null.
        }
        MethodLevelGraphNode firstNode = firstNodePerThread.get(thread);
        if (firstNode == null) {
            firstNodePerThread.put(thread, node);
        }

        record.put(node.getId(), node);
    }

    /**
     *
     * @param intraMethodId
     * @return
     */
    public MethodLevelGraphNode getRecord(int intraMethodId) {
        return record.get(intraMethodId);
    }

    /**
     *
     * @return
     */
    public Collection<MethodLevelGraphNode> getFirstNodeRecorded() {
        return firstNodePerThread.values();
    }

    public void clearPrintCache() {
        alreadyPrinted.clear();
    }
    @Override
    public String toString() {
        String ret = "\n\nsize ="+record.values().size();
        Iterator<MethodLevelGraphNode> it = record.values().iterator();
        while (it.hasNext()) {
              MethodLevelGraphNode methodLevelGraphNode  = it.next();
              if (alreadyPrinted.contains(methodLevelGraphNode)) {
                  continue;
              }
              alreadyPrinted.add(methodLevelGraphNode);
              ret += ret +"\n{"+methodLevelGraphNode+"}";
        }
        return ret;
    }
}
