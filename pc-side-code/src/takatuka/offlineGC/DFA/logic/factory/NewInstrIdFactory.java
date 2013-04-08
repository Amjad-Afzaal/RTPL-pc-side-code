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
package takatuka.offlineGC.DFA.logic.factory;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.Type;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.factory.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class NewInstrIdFactory {

    private HashMap<NewInstrKey, Integer> idMap = new HashMap<NewInstrKey, Integer>();
    private HashMap<Integer, VerificationInstruction> newIdToInstrMap = new HashMap<Integer, VerificationInstruction>();
    private HashMap<Long, Integer> instrTonewIdMap = new HashMap<Long, Integer>();
    private HashMap<String, HashSet<Integer>> idMapPerMethod = new HashMap<String, HashSet<Integer>>();
    private static int idCount = 1;
    private static final NewInstrIdFactory newInstIdFact = new NewInstrIdFactory();
    private HashSet<Integer> primitiveArraysNewIds = new HashSet<Integer>();
    private HashSet<Integer> refArraysNewIds = new HashSet<Integer>();
    private HashMap<Integer, MethodInfo> newIdToMethodMap = new HashMap<Integer, MethodInfo>();

    public static NewInstrIdFactory getInstanceOf() {
        return newInstIdFact;
    }

    public Set<Integer> getAllNewIds() {
        return newIdToMethodMap.keySet();
    }

    /**
     *
     * @param newId
     * @return
     */
    public boolean isPrimitiveArrayNewId(int newId) {
        return primitiveArraysNewIds.contains(newId);
    }

    /**
     * 
     * @param newId
     * @return
     */
    public boolean isRefArrayNewId(int newId) {
        return refArraysNewIds.contains(newId);
    }

    /**
     * 
     * @param methodStr
     * @return
     */
    public HashSet<Integer> getNewIdOfTheMethod(String methodStr) {
        return idMapPerMethod.get(methodStr);
    }

    /**
     * Given a newId return the corresponding instruction.
     * @param newId
     * @return
     */
    public VerificationInstruction getInstrANewIdAssignedTo(int newId) {
        return newIdToInstrMap.get(newId);
    }

    public int getNewIdGivenInstruction(VerificationInstruction instr) {
        Object obj = instrTonewIdMap.get(instr.getInstructionId());
        if (obj == null) {
            return -1;
        } else {
            return (Integer) obj;
        }
    }

    public MethodInfo getMethodOfNewId(int newId) {
        return newIdToMethodMap.get(newId);
    }

    public void removeNewOfMethod(String methodStrStart) {
        Oracle oracle = Oracle.getInstanceOf();
       Iterator<NewInstrKey> it = idMap.keySet().iterator();
       ArrayList<NewInstrKey> toRemove = new ArrayList<NewInstrKey>();
       while (it.hasNext()) {
           NewInstrKey key = it.next();
           //int newId = idMap.get(key);
           if (oracle.getMethodOrFieldString(key.getMethod()).startsWith(methodStrStart)) {
               toRemove.add(key);
           }
       }
       it = toRemove.iterator();
       while (it.hasNext()) {
          NewInstrKey key = it.next();
          idMap.remove(key);
       }
    }
    public void changeOfflineGCNewInstrs(
            HashMap<Integer, Integer> recordOfMyNewNewId) {
        HashMap<NewInstrKey, Integer> newIdsRecordCopy = (HashMap<NewInstrKey, Integer>) idMap.clone();
        Set keySet = idMap.keySet();
        Iterator<NewInstrKey> it = keySet.iterator();
        while (it.hasNext()) {
            NewInstrKey key = it.next();
            int newId = idMap.get(key);
            Integer newNewId = recordOfMyNewNewId.get(newId);
            if (newId == -1 || newNewId == null) {
                continue;
            }
            //System.out.println(newIdsRecordCopy + "\n\n");
            newIdsRecordCopy.remove(key);
            newIdsRecordCopy.put(key, newNewId);
            //System.out.println(newIdsRecordCopy + "\n\n");

        }
        idMap.clear();

        idMap.putAll(newIdsRecordCopy);
    }

    /**
     * creates a unique id per New statement
     * 
     * @param method
     * @param instr
     * @param isPrimitiveArray
     * @param isReferenceArray
     * @return
     */
    public int createNewInstId(MethodInfo method, VerificationInstruction instr,
            boolean isPrimitiveArray, boolean isReferenceArray) {
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);

        /**
         * Do not do offline GC on clinit method. As we cannot 
         * guranteed the order.
        
        if (oracle.isClassInterfaceInitMethod(method)) {
        return -1;
        }
         */
        NewInstrKey key = new NewInstrKey(method, instr);
        Integer id = idMap.get(key);
        if (id == null || id == 0) {
            id = idCount++;
            /**
             * ID must be put in the map so that subsequent calls to a same function
             * always return same id.
             */
            idMap.put(key, id);
            newIdToMethodMap.put(id, method);
            /**
             * For debugging we keep track of ID to the instr they are corresponding
             * to.
             */
            newIdToInstrMap.put(id, instr);
            /**
             * The following map is used in one of the free gc instruction algorithm.
             */
            instrTonewIdMap.put(instr.getInstructionId(), id);

            /**
             * Saves ID generated per method. This is useful for one of the
             * free GC algorithm.
             */
            HashSet<Integer> alreadySavedId = idMapPerMethod.get(methodStr);
            if (alreadySavedId == null) {
                alreadySavedId = new HashSet<Integer>();
                idMapPerMethod.put(methodStr, alreadySavedId);
            }
            alreadySavedId.add(id);
            //Miscellaneous.println("\n\n"+this+"\n\n");
        }
        if (isPrimitiveArray) {
            primitiveArraysNewIds.add(id);
        } else if (isReferenceArray) {
            refArraysNewIds.add(id);
        }
        return id;
    }

    public int getNewInstrGCId(NewInstrKey key) {
        //Miscellaneous.println("abc == ["+key+"] ******  "+idMap);
        Integer ret = idMap.get(key);
        if (ret == null) {
            return -1;
        }
        return ret;
    }

    public Set<NewInstrKey> getNewAndLDCInstrs() {
        return idMap.keySet();
    }

    public Type createNewIdForString(VerificationInstruction inst, MethodInfo currentMethod) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Oracle oracle = Oracle.getInstanceOf();
        ClassFile stringClass = oracle.getClass("java/lang/String");
        if (stringClass == null) {
            Miscellaneous.printlnErr("String class used but not found. Error #98 Exiting...");
            Miscellaneous.exit();
        }
        int newId = createNewInstId(currentMethod, inst, false, false);
        Type type = frameFactory.createType(stringClass.getThisClass().intValueUnsigned(),
                true, newId);
        return type;
    }

    public String toString() {
        String ret = "";
        Set keys = idMap.keySet();
        Iterator it = keys.iterator();
        ret += "size = " + keys.size() + "\n*****\n";
        while (it.hasNext()) {
            Object key = it.next();
            ret += key + " \t" + idMap.get(key);
            ret += "\n*******\n";
        }
        return ret;
    }
}
