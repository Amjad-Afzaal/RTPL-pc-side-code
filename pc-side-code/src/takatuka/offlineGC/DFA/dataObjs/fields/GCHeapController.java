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
package takatuka.offlineGC.DFA.dataObjs.fields;

import java.util.*;

/**
 * 
 * Description:
 * <p>
 * This class keeps all the heap object.
 * One can get a specific heap given a newId.
 * 
 * Each new instruction create a GC Heap object
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCHeapController {

    private HashMap<Integer, GCHeapInterface> heapsMap = new HashMap<Integer, GCHeapInterface>();
    private static final GCHeapController contr = new GCHeapController();
    
    private GCHeapController() {
    }

    public static GCHeapController getInstanceOf() {
        return contr;
    }
    
    public void addGCHeap(int newInstrId, int classId) {
        if (heapsMap.get(newInstrId) != null) {
            return; //do not overwrite existing object.
        }
        heapsMap.put(newInstrId, new GCHeap(newInstrId, classId));
    }

    public void addGCHeapArray(int newInstrId, int classId) {
        if (heapsMap.get(newInstrId) != null) {
            return; //do not overwrite existing object.
        }
      heapsMap.put(newInstrId, new GCArrayHeap(newInstrId, classId));
    }

    public GCHeapInterface getGCHeap(int newInstrId) {
        return heapsMap.get(newInstrId);
    }
}
