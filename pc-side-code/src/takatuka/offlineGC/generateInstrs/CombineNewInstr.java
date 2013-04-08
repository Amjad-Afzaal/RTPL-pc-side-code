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
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * In case two new instruction have all of their free instruction
 * appeared in the code at the same locations then we combine such
 * new Instructions and remove duplicate free instructions. 
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CombineNewInstr {

    /**
     * It combines all the newIds that have free instruction at 
     * the same places.
     */
    private HashMap<Integer, Integer> combineNewIds() {
        HashMap<Integer, HashSet<Long>> map = GCInstruction.getRecordOfFreeInstrPerNewId();
        Vector<Integer> keysVector = new Vector(map.keySet());
        HashSet<Integer> toSkip = new HashSet<Integer>();
        HashMap<Integer, HashSet<Integer>> recordEqualNewIds = new HashMap<Integer, HashSet<Integer>>();
        HashMap<Integer, Integer> recordOfMyNewNewId = new HashMap<Integer, Integer>();
        double countSaved = 0;
        for (int outerLoop = 0; outerLoop < keysVector.size(); outerLoop++) {
            Integer outerKey = keysVector.elementAt(outerLoop);
            if (toSkip.contains(outerKey)) {
                continue;
            }
            HashSet<Long> instrsFreeAt = map.get(outerKey);
            for (int innerLoop = 0; innerLoop < keysVector.size(); innerLoop++) {
                if (innerLoop == outerLoop) {
                    continue;
                }
                Integer innerkey = keysVector.get(innerLoop);
                if (map.get(innerkey).equals(instrsFreeAt)) {
                    //System.out.println("********** new Id "+outerKey+" === "+" new Id "+innerkey+", freeInstrSize="+instrsFreeAt.size());
                    countSaved += (1 + instrsFreeAt.size());
                    recordOfMyNewNewId.put(innerkey, outerKey);
                    HashSet<Integer> record = recordEqualNewIds.get(outerKey);
                    if (record == null) {
                        record = new HashSet<Integer>();
                        recordEqualNewIds.put(outerKey, record);
                    }
                    record.add(innerkey);
                }
            }
            if (recordEqualNewIds.get(outerKey) != null) {
                toSkip.addAll(recordEqualNewIds.get(outerKey));
            }
        }
        LogHolder.getInstanceOf().addLog("*** Total saved by combining"
                + " OFFLINE_GC_NEW instructions =" + countSaved + " instructions", true);
        return recordOfMyNewNewId;
    }

    private void checkNewIdsAvailable() {
        //System.out.println(NewInstrIdFactory.getInstanceOf().toString());
    }

    public void execute() {
        checkNewIdsAvailable();
        HashMap<Integer, Integer> recordOfMyNewNewId = combineNewIds();
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> codeAttInfoVec = oracle.getAllCodeAtt();
        for (int loop = 0; loop < codeAttInfoVec.size(); loop++) {
            CodeAttCache codeAttInfo = codeAttInfoVec.elementAt(loop);
            MethodInfo method = codeAttInfo.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);
            Iterator it = method.getInstructions().iterator();
            while (it.hasNext()) {
                GCInstruction instr = (GCInstruction) it.next();

                HashSet<TTReference> refSetFreedOnInstr = instr.getReferencesFreedOnMe();
                if (refSetFreedOnInstr.isEmpty()) {
                    continue;
                }
                //System.out.println("see me ********* " + instr);
                changeFreeInstrs(instr, refSetFreedOnInstr, recordOfMyNewNewId);
            }

        }
        NewInstrIdFactory.getInstanceOf().changeOfflineGCNewInstrs(recordOfMyNewNewId);
        checkNewIdsAvailable();
    }

    private void changeFreeInstrs(GCInstruction instr,
            HashSet<TTReference> refSetFreedOnInstr,
            HashMap<Integer, Integer> recordOfMyNewNewId) {
        //System.out.println(refSetFreedOnInstr.size() + " : " + refSetFreedOnInstr);

        Iterator<TTReference> it = refSetFreedOnInstr.iterator();
        ArrayList<TTReference> refToChange = new ArrayList<TTReference>();
        while (it.hasNext()) {
            TTReference reference = it.next();

            int newId = reference.getNewId();
            Integer newnewId = recordOfMyNewNewId.get(newId);
            if (newnewId != null) {
                refToChange.add(reference);
                reference.setNewId(newnewId);

            }
        }

        removeDuplicates(instr);

    }

    private void removeDuplicates(GCInstruction instr) {
        Oracle oracle = Oracle.getInstanceOf();
        String methodName = oracle.methodOrFieldName(instr.getMethod(), GlobalConstantPool.getInstanceOf());
        if (methodName.contains("main")) {
           // System.out.println("Stop here");
        }
        HashSet<TTReference> refFreedOnMe = instr.getReferencesFreedOnMe();
        //System.out.println("******** see this " + refFreedOnMe);
        HashSet<TTReference> dummy = new HashSet<TTReference>();
        HashSet<Integer> newIdsSet = new HashSet<Integer>();
        Iterator<TTReference> it = refFreedOnMe.iterator();
        while (it.hasNext()) {
            TTReference ref = it.next();
            if (!newIdsSet.contains(ref.getNewId())) {
                dummy.add(ref);
            }
            newIdsSet.add(ref.getNewId());
        }
        refFreedOnMe.clear();
        refFreedOnMe.addAll(dummy);
        //System.out.println(refSetFreedOnInstr.size() + " : " + refSetFreedOnInstr);

    }
}
