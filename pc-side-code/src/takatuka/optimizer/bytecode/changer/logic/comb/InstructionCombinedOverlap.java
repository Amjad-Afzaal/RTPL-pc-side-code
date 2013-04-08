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
package takatuka.optimizer.bytecode.changer.logic.comb;

import takatuka.classreader.dataObjs.attribute.Instruction;

/**
 * 
 * Description:
 * <p>
 *  
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class InstructionCombinedOverlap {

    private static final InstructionCombinedOverlap instOverlap = new InstructionCombinedOverlap();
    
    private InstructionCombinedOverlap() {
    }
    
    public static InstructionCombinedOverlap getInstanceOf() {
        return instOverlap;
    }
    
    /**
     * returns true if comb1 overlap comb1 or vica versa
     * @param comb1
     * @param comb2
     * @return
     */
    public boolean isOverlap(InstructionsCombined comb1, InstructionsCombined comb2) {
        //Miscellaneous.println("input combinations = "+comb1.getMnemonic()+"\n, "+comb2.getMnemonic());
        if (isSubSet(comb1, comb2) || isOverlapFromBeginning(comb1, comb2) ||
                isOverlapFromBeginning(comb2, comb1)) {
            return true;
        }
        return false;
    }
    
    /**
     * returns true if either input combination is a subset of other
     * @param comb1
     * @param comb2
     * @return
     */
    private boolean isSubSet(InstructionsCombined comb1, InstructionsCombined comb2) {
        if (comb1.getMnemonic().contains(comb2.getMnemonic()) || 
                comb2.getMnemonic().contains(comb1.getMnemonic())) {
            //Miscellaneous.println("Sub-Set ");
            return true;
        }
        return false;
    }
    
    /**
     * returns true if comb1 overlap comb2 at the beginning of comb2
     * @param comb1
     * @param comb2
     * @return
     */
    private boolean isOverlapFromBeginning(InstructionsCombined comb1, InstructionsCombined comb2) {
        boolean isOverlap = true;
        for (int outerLoop = 1; outerLoop < comb1.getInstructions().size(); outerLoop ++) {
            isOverlap = true;
            for (int innerLoop = outerLoop; innerLoop < comb1.getInstructions().size(); innerLoop ++) {
                Instruction inst1 = (Instruction) comb1.getInstructions().elementAt(innerLoop);
                Instruction inst2 = (Instruction) comb2.getInstructions().elementAt(innerLoop-outerLoop);
                if(!inst1.getMnemonic().equals(inst2.getMnemonic())) {
                    isOverlap = false;
                    break;
                }
            }
            if (isOverlap) {
              //  Miscellaneous.println("********** Overlap at the beginning ********* ");
                return isOverlap;
            }
        }
        return isOverlap;
    }


}
