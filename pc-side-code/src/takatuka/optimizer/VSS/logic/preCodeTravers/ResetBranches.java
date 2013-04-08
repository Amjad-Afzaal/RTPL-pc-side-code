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
package takatuka.optimizer.VSS.logic.preCodeTravers;

import java.util.*;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.BHInstruction;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.BranchTarget;

/**
 * It reset branch target to a newly added instruction
 * if the newly added instruction is before a branch instruction
 * It is used:
 *      1) By VSS after adding casting instructions.
 *      2) By OGC after adding OFFLINE_GC_FREE instructions.
 * 
 * @author Faisal Aslam
 */
public class ResetBranches {
    
    private HashMap<Long, BHInstruction> dataMap = new HashMap<Long, BHInstruction>();
    private MethodInfo method = null;
    
    public ResetBranches(MethodInfo method) {
        this.method = method;
    }
    
    /**
     * Use this method to add instructions that are newly added before an instruction
     * @param possibleBranchInstr
     * @param newlyAddedInstr 
     */
    public void addToRestore(BHInstruction possibleBranchInstr, BHInstruction newlyAddedInstr) {
        if (!possibleBranchInstr.isBranchTarget()
                || dataMap.get(possibleBranchInstr.getInstructionId()) != null) {
            return;
        }
        dataMap.put(possibleBranchInstr.getInstructionId(), newlyAddedInstr);
    }
    /**
     * Use this method to change branch to the newly added instruction 
     * instead of the old instruction.
     */
    public void restore() {
        Vector instrVec = method.getInstructions();
        Iterator<BHInstruction> it = instrVec.iterator();
        while (it.hasNext()) {
            BHInstruction instr = it.next();
            Vector<BranchTarget> targetsIt = instr.getMyTargets();
            if (targetsIt.size() == 1) {
                BranchTarget target = targetsIt.firstElement();
                BHInstruction castInstr = dataMap.get(target.targetId);
                if (castInstr == null) {
                    continue;
                }
                BranchTarget newBranchTarget = new BranchTarget(-1, castInstr.getInstructionId());
                targetsIt.clear();
                targetsIt.addElement(newBranchTarget);
                castInstr.setBranchTarget(true);
            }
        }
    }
}
