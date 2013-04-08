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
package takatuka.tukFormat.verifier.logic;

import java.util.*;
import takatuka.classreader.dataObjs.ClassFile;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.classreader.dataObjs.attribute.Instruction;
import takatuka.classreader.logic.constants.JavaInstructionsOpcodes;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;
import takatuka.optimizer.cpGlobalization.logic.util.CodeAttCache;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * It verifies that if a bytecode instruction uses a CP index then it
 * points to a valid location. 
 * 
 * @author Faisal Aslam
 */
public class CPIndexesInBCVerifier {

    public CPIndexesInBCVerifier() {
    }

    /**
     * 1. Get all methods bytecode
     * 2. For each method check all of its instruction to 
     * find each instruction using CP index.
     * 3. For each instruction using CP index check two things:
     * --- a) If the constant pool index is valid.
     * --- b) If the data at that constant pool index is valid.
     * 
     * The instructions use CP index are following:
     * 
     * 1) anewarray classInfo
     * 2) checkcast classInfo
     * 3) getfield fieldInfo
     * 4) getstatic fieldInfo
     * 5) instanceof classInfo
     * 6) invokespecial methodInfo
     * 7) invokestatic methodInfo
     * 8) invokevirtual methodInfo
     * 9) lcd, lcd_w, lcd2_w ---- not yet verified.
     * 10) multianewarray classInfo, ... 
     * 11) new classInfo
     * 12) putfield field
     * 13) putstatic field
     */
    public void execute() {
        Oracle oracle = Oracle.getInstanceOf();
        Iterator<CodeAttCache> it = oracle.getAllCodeAtt().iterator();
        while (it.hasNext()) {
            CodeAttCache codeAttItem = it.next();
            Vector instrVec = codeAttItem.getMethodInfo().getInstructions();
            Iterator<Instruction> instrIt = instrVec.iterator();
            while (instrIt.hasNext()) {
                Instruction instr = instrIt.next();
                int cpIndex = oracle.getCPIndex(instr);
                if (cpIndex == -1 || instr.getMnemonic().startsWith("LDC")) {
                    continue;
                }
                int type = -1;
                if (instr.getOpCode() == JavaInstructionsOpcodes.ANEWARRAY
                        || instr.getOpCode() == JavaInstructionsOpcodes.CHECKCAST
                        || instr.getOpCode() == JavaInstructionsOpcodes.MULTIANEWARRAY
                        || instr.getOpCode() == JavaInstructionsOpcodes.INSTANCEOF) {
                    type = TagValues.CONSTANT_Class;
                } else if (instr.getOpCode() == JavaInstructionsOpcodes.GETFIELD
                        || instr.getOpCode() == JavaInstructionsOpcodes.GETSTATIC
                        || instr.getOpCode() == JavaInstructionsOpcodes.PUTFIELD
                        || instr.getOpCode() == JavaInstructionsOpcodes.PUTSTATIC) {
                    type = TagValues.CONSTANT_Fieldref;
                } else {
                    type = TagValues.CONSTANT_Methodref;
                }
                checkIndex(cpIndex, instr, codeAttItem.getMethodInfo(),
                        codeAttItem.getClassFile(), type);
            }
        }
    }

    private void checkIndex(int cpIndex, Instruction instr, 
            MethodInfo method, ClassFile cFile, int type) {
        /**
         * first check if the cp index is valid.
         */
        GlobalConstantPool cp = GlobalConstantPool.getInstanceOf();
        String typeStr = null;
        if (cFile.getFullyQualifiedClassName().endsWith("Thread")) {
            return;
        }
        if (type == TagValues.CONSTANT_Class) {
            typeStr = "ClassInfo";
        } else if (type == TagValues.CONSTANT_Methodref) {
            typeStr = "MethodRef";
        } else if (type == TagValues.CONSTANT_Fieldref) {
            typeStr = "FieldRef";
        }
        if (cp.getCurrentSize(type) <= cpIndex) {
            Oracle oracle = Oracle.getInstanceOf();
            String methodStr = oracle.getMethodOrFieldString(method);
            
            TukFileVerifier.addException("CP Index="+cpIndex+
                    " for "+typeStr+" is invalid:"
                    +" class-file="+cFile.getFullyQualifiedClassName()
                    + ", method=" + methodStr + ", InstrId=" + instr.getInstructionId());
        } else {
            //todo 
        }
    }

}
