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
import takatuka.classreader.dataObjs.attribute.Instruction;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.OGI.threads.FindRefCannotBeFreedDueToThreads;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.logic.factory.NewInstrIdFactory;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *  It is possible that many new instruction id are generated but only few of
 *  them are freed by the offline GC. The goal of this class is to remove new Instruction
 *  ID not used by any free instruction.
 *
 *  Go through all the method instructions and then exactly once and record free instructions'
 *  free ids in a TreeSet. Now Use that set to create new freeIds.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class RemoveUnUsedNewIdInstr {

    private final TreeMap<Integer, Integer> oldToNewFreeIdMap = new TreeMap<Integer, Integer>();
    private final TreeSet<Integer> exisitingFreeIds = new TreeSet<Integer>();
    private int counter = 1;

    public RemoveUnUsedNewIdInstr() {
    }

    public void execute() {
        counter = 1;
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> codeAttInfoVec = oracle.getAllCodeAtt();
        for (int loop = 0; loop < codeAttInfoVec.size(); loop++) {
            CodeAttCache codeAttInfo = codeAttInfoVec.elementAt(loop);
            MethodInfo method = codeAttInfo.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);
            if (methodStr.contains("checkLoadStoreAndMathInstr")) {
                //Miscellaneous.println("stop here baby");
            }
            Iterator it = method.getInstructions().iterator();
            while (it.hasNext()) {
                GCInstruction instr = (GCInstruction) it.next();
                HashSet<TTReference> refSetFreedOnInstr = instr.getReferencesFreedOnMe();
                execute(refSetFreedOnInstr);

            }
        }
        generateOldToNewIdMap();
    }

    private void execute(HashSet<TTReference> refSetFreedOnInstr) {
        Iterator<TTReference> it = refSetFreedOnInstr.iterator();
        while (it.hasNext()) {
            TTReference reference = it.next();

            int newId = reference.getNewId();
            exisitingFreeIds.add(newId);
        }
    }

    /**
     * After knowing list of all the newIds that are freed we fill the
     * empty gaps. For example if newIds freed are 1, 3,5. Then the gaps are filled
     * and new Ids are generated as 1 for 1, 2 for 3, and 3 for 5.
     */
    private void generateOldToNewIdMap() {
        Iterator<Integer> it = exisitingFreeIds.iterator();

        while (it.hasNext()) {
            int newId = it.next();
            int value = -1;
            if (!FindRefCannotBeFreedDueToThreads.getInstanceOf().cannotFreeNewId(newId)) {
                value = counter++;
            }
            oldToNewFreeIdMap.put(newId, value);
        }
    }

    /**
     *
     * @param oldNewId
     * @return given an old id it return the corresponding new Id.
     */
    public int getNew_NewId(int oldNewId) {
        Integer obj = oldToNewFreeIdMap.get(oldNewId);
        if (obj == null) {
            return -1;
        }
        return obj;
    }

    public void LogMe() {
        Set<Integer> keySet = oldToNewFreeIdMap.keySet();
        Iterator<Integer> keyIt = keySet.iterator();
        String oldToNewIdStr = "#old-id to new-Id\n\n";
        String newIdToLineNumber = "#New-Id to line Number \n\n";
        Oracle oracle = Oracle.getInstanceOf();
        while (keyIt.hasNext()) {
            int oldId = keyIt.next();
            Instruction instr = NewInstrIdFactory.getInstanceOf().getInstrANewIdAssignedTo(oldId);
            String methodStr = oracle.getMethodOrFieldString(instr.getMethod());
            int newId = oldToNewFreeIdMap.get(oldId);
            newIdToLineNumber += newId + "= "
                    + instr.getLineNumber() + ", " + methodStr
                    + "\n";
            oldToNewIdStr += oldId + "=" + newId + "\n";
        }
        oldToNewIdStr += "\n\n*****************************************\n\n";
        oldToNewIdStr += newIdToLineNumber;
        LogHolder.getInstanceOf().addLog(oldToNewIdStr, "oldToNewIdMap.txt", false);

    }
}
