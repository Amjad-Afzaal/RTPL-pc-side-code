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
package takatuka.verifier.logic.DFA;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class InitializeFirstInstruction {

    private static final InitializeFirstInstruction specialInstFactory =
            new InitializeFirstInstruction();
    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();

    private InitializeFirstInstruction() {
        super();
    }

    public static InitializeFirstInstruction getInstanceOf() {
        return specialInstFactory;
    }

    public static Frame createFrame(MethodInfo method, Vector localVariables) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        UTF8Info utf8 = (UTF8Info) pOne.get(method.getDescriptorIndex().
                intValueUnsigned(), TagValues.CONSTANT_Utf8);
        String methodDescriptor = utf8.convertBytes();
        CodeAtt codeAtt = method.getCodeAtt();
        boolean isStatic = method.getAccessFlags().isStatic();

        Vector instructions = method.getInstructions();

        LocalVariables localVar = null;
        //Todo what if visited before?
        boolean changed = true;
        if (localVariables == null) {
            localVar = createLocalVariablesOfFirstInstruction(codeAtt.getMaxLocals().intValueUnsigned(),
                    methodDescriptor,
                    isStatic, null);
        } else {
            localVar = frameFactory.createLocalVariables(codeAtt.getMaxLocals().intValueUnsigned());
            localVar.addVaraibles(localVariables);
        }
        if (instructions.size() == 0) {
            return new Frame(localVar, -1, null);
        }
        byte code[] = null;//method.getCodeAtt().getCode().getData();
        VerificationInstruction specInst = (VerificationInstruction) instructions.elementAt(0);
        int maxStack = specInst.getOperandStack().getMaxSize();
        return new Frame(localVar, maxStack, code);
    }

    /**
     * In case an instruction is visited first time then
     * - for the first instruction of the method, the local variables that
     * represent parameters initially contain values of the typesVec indicated by the
     * method's type descriptor; the operand stack is empty.
     * All other local variables contain an illegal value.
     * - for all other instruction operands stack is empty and local variables unitilized too.
     *
     * @param method MethodInfo
     * @throws Exception
     */
    public static Frame createFrameAndInitFirstInstr(MethodInfo method,
            Vector localVariables) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        UTF8Info utf8 = (UTF8Info) pOne.get(method.getDescriptorIndex().
                intValueUnsigned(), TagValues.CONSTANT_Utf8);
        String methodDescriptor = utf8.convertBytes();
        CodeAtt codeAtt = method.getCodeAtt();
        boolean isStatic = method.getAccessFlags().isStatic();

        Vector instructions = method.getInstructions();

        LocalVariables localVar = null;
        //Todo what if visited before?
        boolean changed = true;
        if (localVariables == null) {
            localVar = createLocalVariablesOfFirstInstruction(codeAtt.getMaxLocals().intValueUnsigned(),
                    methodDescriptor,
                    isStatic, null);
        } else {
            localVar = frameFactory.createLocalVariables(codeAtt.getMaxLocals().intValueUnsigned());
            localVar.addVaraibles(localVariables);
        }
        if (instructions.size() == 0) {
            return new Frame(localVar, -1, null);
        }
        VerificationInstruction specInst = (VerificationInstruction) instructions.elementAt(0);
        specInst.createStackLVForInstr(codeAtt);
        if (specInst.isVisited()) {
            changed = specInst.merge(localVar, null);
        }
        specInst.setLocalVariables(localVar);
        specInst.setChangeBit(changed);

        byte code[] = null;//method.getCodeAtt().getCode().getData();
        return new Frame(localVar, specInst.getOperandStack().getMaxSize(), code);
    }

    /**
     * It set the returnType parameter. One can create a type with empty
     * construction and pass in it.
     * It returns index of next type int the description
     *
     * @param descStr String
     * @param fromIndex int
     * @param returnType Type
     * @return int
     */
    public static int getType(String descStr, int fromIndex, Type returnType) {
//        returnType.isReference = false;
//        returnType.isArray = false;
        char ctype = descStr.charAt(fromIndex);
        int retIndex = fromIndex;
        if (ctype == ';') {
            return getType(descStr, fromIndex + 1, returnType);
        } else if (ctype == 'B') { //byte
            returnType.setType(Type.BYTE, false);
        } else if (ctype == 'C') { //char
            returnType.setType(Type.CHAR, false);
        } else if (ctype == 'I') { //integer
            returnType.setType(Type.INTEGER, false);
        } else if (ctype == 'S') { //short
            returnType.setType(Type.SHORT, false);
        } else if (ctype == 'F') { //float
            returnType.setType(Type.FLOAT, false);
        } else if (ctype == 'Z') { //boolean
            returnType.setType(Type.BOOLEAN, false);
        } else if (ctype == 'D') { //double
            returnType.setType(Type.DOUBLE, false);
        } else if (ctype == 'J') { //long
            returnType.setType(Type.LONG, false);
        } else if (ctype == 'L') { //reference
            //returnType = frameFactory.createType();
            retIndex = returnType.setReferenceType(descStr, fromIndex);
        } else if (ctype == '[') {
            while (descStr.charAt(retIndex) != '[') {
                retIndex++;
            }
            int toRet = getType(descStr, retIndex + 1, returnType);
            returnType.setIsArray();
            return toRet;
        } else if (ctype == 'V') {
            returnType.setType(Type.VOID, false);
        } else {
            throw new VerifyErrorExt("Invalid Local-variable type " + ctype);
        }
        return retIndex + 1;
    }

    public static Vector<Type> getMethodParametersTypes(String methodDesc, boolean isStatic) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Vector<Type> ret = new Vector<Type>();
        if (!isStatic) {
            ret.addElement(frameFactory.createType(true));
        }
        String strParam = methodDesc.substring(methodDesc.indexOf("(") + 1, methodDesc.indexOf(")"));
        int cur = 0;
        while (cur < strParam.length()) {
            Type temp = frameFactory.createType(Type.UNUSED);
            cur = getType(strParam, cur, temp);
            ret.addElement(temp);
            if (temp.isDoubleOrLong()) {
                ret.addElement(frameFactory.createType(Type.SPECIAL_TAIL));
            }

        }
        return ret;
    }

    public static int getMethodParamertersSize(String methodDesc, boolean isStatic) {
        Vector<Type> typesVec = getMethodParametersTypes(methodDesc, isStatic);
        Iterator<Type> it = typesVec.iterator();
        int size = 0;
        while (it.hasNext()) {
            Type type = it.next();
            if (!type.isReference() && type.getType() == Type.SPECIAL_TAIL) {
                continue;
            }
            size += type.getBlocks();
        }
        return size;
    }

    public static LocalVariables createLocalVariablesOfFirstInstruction(int maxLocalSize,
            String descStr, boolean isStatic, Type thisTypeForNonStaticMethods) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        LocalVariables localVar = frameFactory.createLocalVariables(maxLocalSize);

        //as at zero we will have (
        int cur = 1;
        Type toAdd = null;
        for (int loop = 0; loop < maxLocalSize /*|| descStr.charAt(cur) != ')'*/;) {

            toAdd = frameFactory.createType(Type.UNUSED);

            //each virtual method has a hidden variable.
            if (!isStatic && loop == 0) {
                if (thisTypeForNonStaticMethods == null) {
                    int thisPointer = ClassFile.currentClassToWorkOn.getThisClass().intValueUnsigned();
                    thisTypeForNonStaticMethods = frameFactory.createType(thisPointer, true, 0);
                }
                loop += localVar.add(thisTypeForNonStaticMethods);
                continue;
            }

            //check if a function has no more parameters
            if (descStr.charAt(cur) != ')') {
                cur = getType(descStr, cur, toAdd);
            }
            loop += localVar.add(toAdd);
        }
        return localVar;
    }
}
