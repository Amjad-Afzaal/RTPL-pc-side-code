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
package takatuka.tukFormat.dataObjs;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.optimizer.VSS.logic.preCodeTravers.ReduceTheSizeOfLocalVariables;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.DFA.InitializeFirstInstruction;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFFieldInfo extends VerifyMethodInfo implements LFBaseObject {

    private Un address = null;
    private static HashMap cpindexMap = new HashMap();
    private static HashMap nameAndTypeIndexMap = new HashMap();
    private String className = null;
    private int cpRefIndex = -1;
    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();

    public int getReferenceIndex() {
        return cpRefIndex;
    }

    public void setReferanceIndex(int index) {
        cpRefIndex = index;
    }

    public void setAddress(Un address) {
        this.address = address;
    }

    public Un getAddress() {
        return address;
    }

    public LFFieldInfo(ClassFile myClassFile) {
        super(myClassFile);
    }

    public boolean checkNameAndType(Un name) throws Exception {
        Un myNaT = computeNameAndTypeInfo(this);
        if (myNaT != null && myNaT.equals(name)) {
            return true;
        }
        return false;
    }

    public Un getNameAndTypeIndex() throws Exception {
        Un nameAndType = (Un) computeNameAndTypeInfo(this);
        if (nameAndType == null) {
            return factory.createUn();
        }
        return nameAndType;
    }
    //Todo function should not be here

    public static Un computeNameAndTypeInfo(FieldInfo fieldInfo) throws
            Exception {
        if (nameAndTypeIndexMap.get(fieldInfo) != null) {
            return (Un) nameAndTypeIndexMap.get(fieldInfo);
        }

        Un name = fieldInfo.getNameIndex();
        Un desc = fieldInfo.getDescriptorIndex();
        int size = pOne.getCurrentSize(TagValues.CONSTANT_NameAndType);

        NameAndTypeInfo nAndT = null;
        for (int loop = 0; loop < size; loop++) {
            nAndT = (NameAndTypeInfo) pOne.get(loop,
                    TagValues.CONSTANT_NameAndType);
            if (nAndT.getIndex().equals(name)
                    && nAndT.getDescriptorIndex().equals(desc)) {
                nameAndTypeIndexMap.put(fieldInfo,
                        factory.createUn(loop).trim(2));
                break;
            }
        }
        return (Un) nameAndTypeIndexMap.get(fieldInfo);
    }

    public String getName() {
        UTF8Info utf8 = (UTF8Info) GlobalConstantPool.getInstanceOf().
                get(getNameIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8);
        return utf8.convertBytes();
    }

    public String getClassName() throws Exception {
        if (className != null) {
            return className;
        }
        LFClassInfo classInfo = (LFClassInfo) GlobalConstantPool.getInstanceOf().
                get(getClassFile().getThisClass().intValueUnsigned(), TagValues.CONSTANT_Class);

        className = ((UTF8Info) GlobalConstantPool.getInstanceOf().get(
                classInfo.getIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8)).convertBytes();
        return className;
    }

    /**
     * parse signature for number of arguments
     * @param signature String
     * @return int
     */
    private static int getArgsCount(String signature) throws Exception {
        int args = 0, cur = 0;
        if (signature.charAt(cur++) != '(') {
            Miscellaneous.println("MethodInfo.getArgs: Invalid type string");
            Miscellaneous.exit();
        }
        boolean previousWasArray = false;
        while (signature.charAt(cur) != ')') {
            if ((signature.charAt(cur) == 'B') || // byte
                    (signature.charAt(cur) == 'C') || // unicode char
                    (signature.charAt(cur) == 'I') || // integer
                    (signature.charAt(cur) == 'S') || // short
                    (signature.charAt(cur) == 'F') || // float
                    (signature.charAt(cur) == 'Z')) { //boolean
                if (!previousWasArray) {
                    args++;
                }
                previousWasArray = false;
            } else if (signature.charAt(cur) == 'D'
                    || signature.charAt(cur) == 'J') { //double or long
                if (!previousWasArray) {
                    args = args + 2;
                }
                previousWasArray = false;
            } else if (signature.charAt(cur) == 'L') { // instance of class
                while (signature.charAt(cur) != ';') {
                    cur++;
                }
                if (!previousWasArray) {
                    args++;
                }
                previousWasArray = false;

            } else if (signature.charAt(cur) == '[') {
                args++;
                while (signature.charAt(cur + 1) == '[') {
                    cur++;
                }
                previousWasArray = true;
            } else {
                Miscellaneous.printlnErr(
                        "MethodInfo.getArgs: Unexpected char in type: '"
                        + signature.charAt(cur) + "'");
                Miscellaneous.exit();
            }
            cur++;
        }
        return args;

    }

    public String getRawTypeSignature() {
        return ((UTF8Info) pOne.get(getDescriptorIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8)).convertBytes();
    }

    public int getArgsCount() throws Exception {
        //******** Only for testing. **********
        int count = 0;
        String signature = ((UTF8Info) pOne.get(
                getDescriptorIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8)).convertBytes();
        count = getArgsCount(signature);
        AccessFlags flag = this.getAccessFlags();
        // non-static methods have an hidden extra argument: the reference
        if (!flag.isStatic()) {
            count++;
        }
        int newCount = getArgsCountNew();
        if (newCount != count && ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES == 4) {
            String name = Oracle.getInstanceOf().methodOrFieldName(this, pOne);
            String desc = Oracle.getInstanceOf().methodOrFieldDescription(this, pOne);
            String classNameLocal = getClassFile().getFullyQualifiedClassName();
            Miscellaneous.printlnErr("Error # 18620 ");
            Miscellaneous.println(newCount + " abc " + count + " " + name + "---" + desc + "-->" + classNameLocal);
            getArgsCountNew();
            Miscellaneous.exit();
        }
        //************** End of testing code ***************
        return getArgsCountNew();
    }

    public int getArgsCountNew() throws Exception {
        String signature = ((UTF8Info) pOne.get(
                getDescriptorIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8)).convertBytes();
        return InitializeFirstInstruction.getMethodParamertersSize(signature, getAccessFlags().isStatic());

    }

    /**
     * Get all MethodRefInfo or FieldRefInfo given a cooresponding MethodInfo
     * or FieldInfo
     * 1. Get NameAndTypeInfo
     * 2. Compare NameAndTypeInfo and thisClass to get cooresponding ReferenceInfo
     *
     * @return Un
     * It returns the index of MethodRefInfo or FieldRefInfo
     * @throws Exception
     */
    public static int computeRefInfoUsingInfo(LFFieldInfo fieldInfo) throws
            Exception {

        if (cpindexMap.get(fieldInfo) != null) {
            return (Integer) cpindexMap.get(fieldInfo);
        }
        Un nameAndTypeIndex = computeNameAndTypeInfo(fieldInfo);

        //INTERFACES ????
        int tag = -1;
        if (fieldInfo instanceof LFMethodInfo) {
            tag = TagValues.CONSTANT_Methodref;
        } else if (fieldInfo instanceof LFFieldInfo) {
            tag = TagValues.CONSTANT_Fieldref;
        }

        ReferenceInfo refer = null;
        Un name = null;
        Un thisClass = null;
        for (int loop = 0; loop < pOne.getCurrentSize(tag); loop++) {
            //Miscellaneous.println(globRec.getObject());
            Object obj = pOne.get(loop, tag);
            if (obj instanceof ReferenceInfo) {
                refer = (ReferenceInfo) obj;
                name = refer.getNameAndTypeIndex();
                thisClass = refer.getIndex();
                if (name.equals(nameAndTypeIndex)
                        && thisClass.equals(fieldInfo.getClassFile().getThisClass()) /*&& isSubClass(thisClass, fieldInfo.getThisClass())*/) {
                    cpindexMap.put(fieldInfo, loop);
                    return loop;
                }
            }
        }
//        Miscellaneous.println("Cannot load method name=" + nameAndTypeIndex +
//                           " because class not found in memory");
        return -1;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return "\nfields are not printed";
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff);
    }
}
