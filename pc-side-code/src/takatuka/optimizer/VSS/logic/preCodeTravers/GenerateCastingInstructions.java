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
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.file.PropertyReader;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.dataObjs.Type;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenerateCastingInstructions {

    private static final GenerateCastingInstructions myObj = new GenerateCastingInstructions();
    protected FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private MethodInfo currentMethod = null;

    /**
     * constructor is private
     */
    private GenerateCastingInstructions() {
    }

    /**
     * 
     * @return
     */
    public static GenerateCastingInstructions getInstanceOf() {
        return myObj;
    }

    /**
     * Go to all the classfile and each method.
     * See if the method is recored by the controller.
     * If so then remove the method and add the casting instructions in the method.
     */
    public void generateCastingInstrs() {
        ClassFileController classContr = ClassFileController.getInstanceOf();
        int size = classContr.getCurrentSize();
        for (int classFileIndex = 0; classFileIndex < size; classFileIndex++) {
            ClassFile cFile = (ClassFile) classContr.get(classFileIndex);
            MethodInfoController mContr = cFile.getMethodInfoController();
            for (int methodIndex = 0; methodIndex < mContr.getCurrentSize(); methodIndex++) {
                MethodInfo method = (MethodInfo) mContr.get(methodIndex);
                currentMethod = method;
                generateCastingInstrs(method);
            }
        }

    }

    private void generateCastingInstrs(MethodInfo method) {

        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        if (methodStr.equals("java.lang.StringBuffer.append(Ljava/lang/String;)Ljava/lang/StringBuffer;")) {
            //Miscellaneous.println("Stop here");
            //System.out.println(method.getInstructions());
        }
        CastInstructionController castInstContr = CastInstructionController.getInstanceOf();
        TreeSet<CastInfo> castInfoSet = castInstContr.get(method);
        if (castInfoSet == null) {
            return;
        }
        Iterator<CastInfo> it = castInfoSet.iterator();
        int startLoc = 0;
        Vector instrVec = method.getInstructions();
        while (it.hasNext()) {
            CastInfo castInfo = it.next();
            long instrId = castInfo.getInstrId();
            generateCastingInstrs(instrVec, method.getCodeAtt(),
                    0, instrId, castInfo.getStackCurrentType(),
                    castInfo.getTypeToConvertInto(),
                    castInfo.getStackLocation(),
                    castInfo.isForMethodDescription(),
                    oracle.getMethodOrFieldString(method));
        }
        method.getCodeAtt().setInstructions(instrVec);

    }

    private void generateCastingInstrs(Vector instrVec, CodeAtt codeAtt,
            int startLoc, long instrId, int stackCurrentType,
            int toNewType, int stackLoc, boolean isForMethodDesc,
            String methodStr) {
        int loop = 0;
        boolean found = false;
        ResetBranches resetBranches = new ResetBranches(currentMethod);
        for (loop = startLoc; loop < instrVec.size(); loop++) {
            BHInstruction instr = (BHInstruction) instrVec.elementAt(loop);
            if (instr.getInstructionId() == instrId) {
                BHInstruction castingInst = insertCastingInstruction(instrVec, codeAtt, loop, stackCurrentType,
                        toNewType, stackLoc, isForMethodDesc);
                resetBranches.addToRestore(instr, castingInst);
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("instr-Id= "+instrId+" method-Str=" +methodStr);
            Miscellaneous.printlnErr("Error 8923 ");
            Miscellaneous.exit();
        }
        resetBranches.restore();
        
    }

    private BHInstruction insertCastingInstruction(Vector<Instruction> instrVec,
            CodeAtt codeAtt,
            int insertAtIndex, int stackCurrentType, int toNewType,
            int stackLoc, boolean isForMethodDesc) {
        Instruction instr = null;
        try {
            if (stackLoc > 65535) {
                Miscellaneous.printlnErr("The method ("
                        + Oracle.getInstanceOf().getMethodOrFieldString(currentMethod)
                        + ") is too long for using "
                        + ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES
                        + " byte slot sizes\n"
                        + "Either recompile your code using bigger slot size "
                        + "(wasting RAM) or rewrite the method");
                System.exit(1);
            }

            if (!isForMethodDesc) {
                Un operand = factory.createUn(convertType(stackCurrentType)).trim(1);
                operand.conCat(factory.createUn(stackLoc).trim(1));
                instr = factory.createInstruction(JavaInstructionsOpcodes.CAST_STACK_LOCATION,
                        operand, codeAtt);
            } else {
                Un operand = factory.createUn(convertType(stackCurrentType)).trim(1);
                operand.conCat(factory.createUn(convertType(toNewType)).trim(1));
                int stackLocationSize = BytecodeProcessor.getAllParameterSizes(JavaInstructionsOpcodes.CAST_METHOD_STACK_LOCATION).firstElement();
                operand.conCat(factory.createUn(stackLoc).trim(stackLocationSize));
                instr = factory.createInstruction(JavaInstructionsOpcodes.CAST_METHOD_STACK_LOCATION,
                        operand, codeAtt);
            }
            //Miscellaneous.println("------------ "+instr);
            instrVec.add(insertAtIndex, instr);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return (BHInstruction) instr;
    }

    private byte convertType(int type) {
        if (type == Type.INTEGER) {
            return FieldTypes.TYPE_JINT;
        } else if (type == Type.CHAR) {
            return FieldTypes.TYPE_JCHAR;
        } else if (type == Type.SHORT) {
            return FieldTypes.TYPE_JSHORT;
        } else if (type == Type.BOOLEAN) {
            return FieldTypes.TYPE_JBOOLEAN;
        } else if (type == Type.BYTE) {
            return FieldTypes.TYPE_JBYTE;
        } else if (type == Type.BYTE_BOOLEAN) {
            return FieldTypes.TYPE_JBYTE;
        } else if (type == Integer.MIN_VALUE) {
            return FieldTypes.TYPE_JREF;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * 1. Go though all the castInfo and check if the instruction 
     * a cast has to insert before is branch instruction or not. 
     * If not the continue with next instruction otherwise go to step 2.
     * 2. find the first instruction in the castInfo for each branch instruction
     * @param castInfoSet 
     */
    private void resetTheBranches(TreeSet<CastInfo> castInfoSet) {
    }
}
