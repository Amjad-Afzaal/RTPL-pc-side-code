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
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 *
 * Description:
 * <p>
 *
 * The class geneates code for STATIC functions having synchronized keyword in their
 * signatures.
 *
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class GenCodeForSynInSignStaticMethods {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private static final Oracle oracle = Oracle.getInstanceOf();
    private static final String SPECIAL_FIELD_NAME = "<syncSpecial>";
    private static final String SPECIAL_FIELD_DESC = "Ljava/lang/Object;";
    private static final String CLINIT_METHOD_NAME = "<clinit>";
    private static final String VOID_METHOD_DESC = "()V";
    private static final String INIT_METHOD_NAME = "<init>";
    private static final String OBJECT_CLASS_NAME = "java/lang/Object";
    private static final String CODE_ATT_NAME = "Code";
    private static int indexOfObjectClassInit = -1;
    

    

    /**
     * Step 1: add the special field if not already added
     * Step 2: the code is same as of non static method but
     * only in the monitor enter block it uses getfield instead
     * of aload_0 (this pointer).
     * 
     * @param syncStaticMethod
     * @param cFile
     */
    public void execute(MethodInfo syncStaticMethod, ClassFile cFile) {
        try {
            int constantPoolFieldIndex = checkAndAddSpecialField(cFile);
            new GenCodeForSyncInSignature().execute(syncStaticMethod, cFile, constantPoolFieldIndex);
            oracle.clearMethodCodeAttAndClassFileCache();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    

    /**
     *
     * Step 1: Add a field in the class where the static sync method exist  i.e.
     *          static Object <syncSpecial>;
     * Note that in case field with this name already exist then do not readd field and go to step 4:
     *
     * Step 1 a: Add field in the CP if not already there. Record the index of that added field or
     * already existing field.
     *
     * Step 2: Add method <clinit> if the method already does not exist
     *
     * Step 3: In the <clinit> method add following code
     * - NEW CP-index of Object class
     * - DUP
     * - INVOKESPECIAL CP-index of <init> of Object class
     * - PUTSTATIC CP-index of Step 1 a
     * - RETURN NOTE: Do not add return if the it already exist.
     * 
     * 
     * @param cFile
     * @return
     * the cp index of the field.
     */
    private int checkAndAddSpecialField(ClassFile cFile) throws Exception {
        int fieldIndex = oracle.getFieldCPIndex(SPECIAL_FIELD_NAME,
                SPECIAL_FIELD_DESC, cFile, false);
        if (fieldIndex != -1) {
            return fieldIndex;
        }
        ClassFile objectClass = oracle.getClass(OBJECT_CLASS_NAME);

        fieldIndex = oracle.addFieldInfoInClassFileAndCP(SPECIAL_FIELD_NAME,
                SPECIAL_FIELD_DESC, cFile, false,
                AccessFlags.createAccessFlagsValue(true, false, false));
        /* Miscellaneous.println("see me 1 "+cFile.getMethodInfoController().getCurrentSize()); */
        oracle.addFieldInfoInClassFileAndCP(CLINIT_METHOD_NAME,
                VOID_METHOD_DESC, cFile, true,
                AccessFlags.createAccessFlagsValue(true, false, true));
        /*Miscellaneous.println("see me 1 "+cFile.getMethodInfoController().getCurrentSize());*/
        MethodInfo clinitMethod = (MethodInfo) oracle.getMethodOrField(cFile,
                CLINIT_METHOD_NAME, VOID_METHOD_DESC, true);

        CodeAtt codeAtt = clinitMethod.getCodeAtt();
        if (codeAtt == null) {
            codeAtt = addCodeAttribute(clinitMethod);
        }
        int maxStack = codeAtt.getMaxStack().intValueUnsigned();
        //todo: make it batter later
        codeAtt.setMaxStack(factory.createUn(maxStack + 2).trim(2));
        Vector instr = codeAtt.getInstructions();
        addInstructionsInClinit(instr, objectClass, fieldIndex, codeAtt);
        codeAtt.setInstructions(instr);
        return fieldIndex;
    }

    private CodeAtt addCodeAttribute(MethodInfo method) throws Exception {
        int codeAttName = oracle.getUTF8InfoIndex(CODE_ATT_NAME);
        Un codeAttNameUn = factory.createUn(codeAttName).trim(2);
        Un codeAttlength = factory.createUn(13).trim(4);
        Un maxStack = factory.createUn(0).trim(2);
        Un maxLocal = factory.createUn(0).trim(2);
        Un codeLength = factory.createUn(1).trim(4);
        Un exceptionTableLength = factory.createUn(0).trim(2);
        Un attributeCount = factory.createUn(0).trim(2);

        AttributeInfoController attInfoCont = method.getAttributeController();
        attInfoCont.setMaxSize(attInfoCont.getMaxSize() + 1);
        CodeAtt codeAtt = factory.createCodeAttribute(codeAttNameUn, codeAttlength, maxStack, maxLocal, codeLength);
        codeAtt.setExceptionTableLength(exceptionTableLength);
        codeAtt.setAttributeCount(attributeCount);
        attInfoCont.add(codeAtt);
        //RETURN instr
        Instruction instr = factory.createInstruction(JavaInstructionsOpcodes.RETURN,
                factory.createUn(), codeAtt);
        Vector methodInstr = new Vector();
        methodInstr.addElement(instr);
        codeAtt.setInstructions(methodInstr);
        return codeAtt;
    }
    /*
     * Step 3: In the <clinit> method add following code
     * - NEW CP-index of Object class
     * - DUP
     * - INVOKESPECIAL CP-index of <init> of Object class
     * - PUTSTATIC CP-index of Step 1 a
     * - RETURN NOTE: Do not add return if the it already exist.
     * 
     *
     * @param methodInstr
     * @param objectClass
     * @param fieldCPIndex
     * @param codeAtt
     * @throws Exception
     */
    private void addInstructionsInClinit(Vector<Instruction> methodInstr,
            ClassFile objectClass, int fieldCPIndex, CodeAtt codeAtt) throws Exception {
        if (objectClass == null) {
            LogHolder.getInstanceOf().addLog("Object Class is null ", true);
            return;
        }
        int objectCPIndex = objectClass.getThisClass().intValueUnsigned();
        Instruction instr = null;

        //PUTSTATIC instr
        instr = factory.createInstruction(JavaInstructionsOpcodes.PUTSTATIC,
                factory.createUn(fieldCPIndex).trim(2), codeAtt);
        methodInstr.insertElementAt(instr, 0);

        //INVOKESPECIAL instr
        instr = factory.createInstruction(JavaInstructionsOpcodes.INVOKESPECIAL,
                factory.createUn(findObjectClassInitMethod(objectCPIndex)).trim(2),
                codeAtt);
        methodInstr.insertElementAt(instr, 0);

        //DUP instr
        instr = factory.createInstruction(JavaInstructionsOpcodes.DUP,
                factory.createUn(), codeAtt);
        methodInstr.insertElementAt(instr, 0);

        //NEW instr
        instr = factory.createInstruction(JavaInstructionsOpcodes.NEW,
                factory.createUn(objectCPIndex).trim(2), codeAtt);
        methodInstr.insertElementAt(instr, 0);

    }

    private int findObjectClassInitMethod(int objectCPIndex) {
        if (indexOfObjectClassInit != -1) {
            return indexOfObjectClassInit;
        }
        GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
        int poolSize = gcp.getCurrentSize(TagValues.CONSTANT_Methodref);
        for (int loop = 0; loop < poolSize; loop++) {
            ReferenceInfo mRefInfo = (ReferenceInfo) gcp.get(loop, TagValues.CONSTANT_Methodref);
            String name = oracle.methodOrFieldName(mRefInfo, gcp);
            String desc = oracle.methodOrFieldDescription(mRefInfo, gcp);
            int classThisPointer = mRefInfo.getIndex().intValueUnsigned();
            if (name.equals(INIT_METHOD_NAME) && desc.equals(VOID_METHOD_DESC)
                    && classThisPointer == objectCPIndex) {
                indexOfObjectClassInit = loop;
                break;
            }
        }
        if (indexOfObjectClassInit == -1) {
            Miscellaneous.printlnErr("Error # 1232");
            Miscellaneous.exit();
        }
        return indexOfObjectClassInit;
    }
}
