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
package takatuka.optimizer.bytecode.replacer.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.VSS.logic.preCodeTravers.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * 
 * Replaces each ICONST and BIPUSH with SIPUSH.
 * 
 * The reduction in the instruction leads in reduction in the size of JVM
 * and also the Java bytecode (as more bytecode optimizations are possible with the
 * available opcodes).
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ReplaceConstInstructions {

    private static final ReplaceConstInstructions myObj = new ReplaceConstInstructions();

    private ReplaceConstInstructions() {
    }

    public static ReplaceConstInstructions getInstanceOf() {
        return myObj;
    }

    public void execute() {
        Vector<CodeAttCache> codeAttCacheVec = Oracle.getInstanceOf().getAllCodeAtt();
        Iterator<CodeAttCache> it = codeAttCacheVec.iterator();
        while (it.hasNext()) {
            CodeAttCache codeAttCache = it.next();
            execute(codeAttCache.getMethodInfo());
        }
    }
    public void execute(MethodInfo method) {
        try {
            if (method.getCodeAtt() == null || method.getCodeAtt().getInstructions().size() == 0) {
                return;
            }
            Vector instrVec = method.getInstructions();
            boolean isChanged = false;
            String methodStr = Oracle.getInstanceOf().getMethodOrFieldString(method);
            for (int loop = 0; loop < instrVec.size(); loop++) {
                Instruction instr = (Instruction) instrVec.elementAt(loop);
                Instruction oldInstr = (Instruction) instr.clone();
                isChanged = convertInstruction(ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES, instr);
                if (isChanged == true) {
                    //Miscellaneous.println(" .---- " + oldInstr + "\n" + instr);
                    //Miscellaneous.println("Stop here 78 ");

                }
            }
            method.getCodeAtt().setInstructions(instrVec);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private boolean convertInstruction(int slotSize, Instruction instr) throws Exception {
        int opcode = instr.getOpCode();
        boolean changed = false;
        if (slotSize == 2 || slotSize == 4 || slotSize == 1) {
            /* commenting code for the time being. Will uncomment after looking
             * into it in more detail.
             if (opcode == JavaInstructionsOpcodes.BIPUSH) {
                ChangeCodeForReducedSizedLV.changeInstruction(instr, JavaInstructionsOpcodes.SIPUSH,
                        instr.getOperandsData().intValueSigned(), 2, true);
                changed = true;
            } else*/
            if (opcode >= JavaInstructionsOpcodes.ICONST_M1
                    && opcode <= JavaInstructionsOpcodes.ICONST_5) {
                ChangeCodeForReducedSizedLV.changeInstruction(instr, JavaInstructionsOpcodes.SIPUSH,
                        opcode - 3, 2, true);
                changed = true;
            }
        }
        return changed;
    }
}
