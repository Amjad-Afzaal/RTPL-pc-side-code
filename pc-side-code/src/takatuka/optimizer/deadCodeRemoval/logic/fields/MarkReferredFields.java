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
package takatuka.optimizer.deadCodeRemoval.logic.fields;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam, Christoph Gonsior, Sebastian Wagner, Natascha Widder
 * @version 1.0
 */
class MarkReferredFields {

    private static final MarkReferredFields markRefField = new MarkReferredFields();
    private Oracle oracle = Oracle.getInstanceOf();
    private MarkReferredFields() {
    }

    public static MarkReferredFields getInstanceOf() {
        return markRefField;
    }

    public void markFields() {
        Vector<CodeAttCache> codeAttVect = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> it = codeAttVect.iterator();
        while (it.hasNext()) {
            CodeAttCache codeAttInfo = it.next();
            markFields(codeAttInfo);
        }
    }

    private void markFields(CodeAttCache codeAttInfo) {
        Vector<Instruction> instVector = ((CodeAtt) codeAttInfo.getAttribute()).getInstructions();        
        Iterator<Instruction> it = instVector.iterator();
        int opcode = -1;
        while (it.hasNext()) {
            Instruction inst = it.next();
            opcode = inst.getOpCode();
            if (opcode == JavaInstructionsOpcodes.PUTFIELD ||
                    opcode == JavaInstructionsOpcodes.GETFIELD ||
                    opcode == JavaInstructionsOpcodes.PUTSTATIC ||
                    opcode == JavaInstructionsOpcodes.GETSTATIC) {
                boolean isPut = opcode == JavaInstructionsOpcodes.PUTSTATIC ||
                        opcode == JavaInstructionsOpcodes.PUTFIELD ? true : false;
                markField(inst, isPut, codeAttInfo.getClassFile());
            }
        }
    }

    /**
     * -- get instruction operand. 
     * -- The operand will points to FieldRefInfo
     * -- Use oracle to find that field in the corresponding classfile whose pointer 
     *    is mention in fieldrefinfo.
     * 
     * @param inst
     * @param isPut
     * @param codeClassFile
     */
    private void markField(Instruction inst, boolean isPut, ClassFile codeClassFile) {
        MultiplePoolsFacade cp = codeClassFile.getConstantPool();
        int cpFieldIndex = inst.getOperandsData().intValueUnsigned();
        ClassFile.currentClassToWorkOn = codeClassFile;
        FieldRefInfo fieldRef = (FieldRefInfo) cp.get(cpFieldIndex, TagValues.CONSTANT_Fieldref);
        Un thisPointer = fieldRef.getIndex();
        ClassFile.currentClassToWorkOn = codeClassFile;
        ClassFile cFile = oracle.getClass(thisPointer, cp);

        int fieldIndexController = -1;
        while (fieldIndexController == -1 && cFile != null) {
            ClassFile.currentClassToWorkOn = codeClassFile;
            fieldIndexController = oracle.getReferenceFieldFromClassFile(cp,
                    fieldRef, cFile, false);
            if (fieldIndexController == -1) {
                cFile = oracle.getSuperClass(cFile);
            }
        }
        if (cFile == null) {
            return; //cannot mark a field in not loaded class file
        }
        DCFieldInfo fieldInfo = (DCFieldInfo) cFile.getFieldInfoController().get(fieldIndexController);
        //Miscellaneous.println("mark Field: "+fieldInfo);
        if (fieldInfo != null) {
            fieldInfo.setFieldStatus(isPut, cpFieldIndex, codeClassFile.getFullyQualifiedClassName());
        }
    }
}
