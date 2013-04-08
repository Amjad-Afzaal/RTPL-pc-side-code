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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool;

import java.util.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * Here we globalized all the Attributes (instances of AttributeInfo) ...
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PhaseFour implements Phase {

    private GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private ClassFile cfile = null;
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private static final PhaseFour phaseFour = new PhaseFour();

    protected PhaseFour() {
        super();
    }

    public static PhaseFour getInstanceOf() {
        return phaseFour;
    }

    public boolean isPhaseTag(int tag) {
        //no body use it here.
        throw new UnsupportedOperationException();
    }

    public boolean isPhaseElm(InfoBase obj) {
        //no body use it here.
        throw new UnsupportedOperationException();
    }

    public void removeAllDuplicates(Phase phase) throws Exception {
        //no body use it here.
        throw new UnsupportedOperationException();
    }

    public void execute() throws Exception {
        ClassFileController classCont = ClassFileController.getInstanceOf();

        for (int loop = 0; loop < classCont.getCurrentSize(); loop++) {
            ClassFile.currentClassToWorkOn = (ClassFile) classCont.get(loop);


            cfile = ClassFile.currentClassToWorkOn;
//            Miscellaneous.println("+++++++++++++++++++++++++++++++++++++ "+KeyUtil.getRegularBaseIndexStr(ClassFile.currentClassToWorkOn.getThisClass()));
//            Miscellaneous.println(cp.oldtoString());
            globalizeAllAttributes();
            gloabizeInterfaceInformation();
        }
        //StatisticGenerator.produceStatsPhaseFour();
    }

    private void gloabizeInterfaceInformation() throws
            Exception {
        int size = cfile.getInterfaceController().getCurrentSize();
        InterfaceController newcontrl = new InterfaceController(size);
        int classinfoIndex;
        for (int loop = 0; loop < size; loop++) {
            classinfoIndex = pOne.getGlobalIndex((Un) cfile.getInterfaceController().get(
                    loop), ClassFile.currentClassToWorkOn,
                    TagValues.CONSTANT_Class);
            newcontrl.add(factory.createUn(classinfoIndex).trim(2));
        }
        //add it back
        cfile.setInterfacesInfos(newcontrl);
    }

    private void globalizeSourceFileAtt(SourceFileAtt srcAtt) throws Exception {
        srcAtt.setSourcefileIndex(pOne.getGlobalIndexUn(
                srcAtt.getSourcefileIndex(), ClassFile.currentClassToWorkOn, TagValues.CONSTANT_Utf8));
    }

    private void globalizeLocalVariableTableAtt(LocalVariableTableAtt lvtAtt) throws
            Exception {
        int size = lvtAtt.getLocalVariableTableLength();
        for (int loop = 0; loop < size; loop++) {
            try {
                //Miscellaneous.println(lvtAtt);
                int descriptorIndex = pOne.getGlobalIndex(lvtAtt.getDescriptorIndex(loop),
                        ClassFile.currentClassToWorkOn, TagValues.CONSTANT_Utf8);
                int nameIndex = pOne.getGlobalIndex(lvtAtt.getNameIndex(loop),
                        ClassFile.currentClassToWorkOn, TagValues.CONSTANT_Utf8);
                lvtAtt.setDescriptorIndex(loop,
                        factory.createUn(descriptorIndex).trim(2));
                lvtAtt.setNameIndex(loop, factory.createUn(nameIndex).trim(2));
            } catch (Exception d) {
                Miscellaneous.println(lvtAtt);
                Miscellaneous.println("name_index="
                        + lvtAtt.getNameIndex(loop).hexValue()
                        + ", descriptor_index"
                        + lvtAtt.getDescriptorIndex(loop).hexValue());
                d.printStackTrace();
                Miscellaneous.exit();
            }
        }

    }

    private void globalizeExceptionAtt(ExceptionsAtt expAtt) throws Exception {
        int size = expAtt.getNumberOfExceptions().intValueUnsigned();
        int classinfoIndex;
        for (int loop = 0; loop < size; loop++) {
            classinfoIndex = pOne.getGlobalIndex(expAtt.getExceptionIndexAt(
                    loop), ClassFile.currentClassToWorkOn,
                    TagValues.CONSTANT_Class);
            expAtt.updateExceptionIndex(loop, factory.createUn(classinfoIndex).trim(2));
        }
    }

    private void globalizeInnerClassAtt(InnerClassesAtt innerClassAtt) throws
            Exception {

        int size = innerClassAtt.getNumberOfClasses();
        for (int loop = 0; loop < size; loop++) {

            //per documentation it could be zero
            if (innerClassAtt.getInnerNameIndex(loop).intValueUnsigned() != 0) {
                innerClassAtt.setInnerClassInfoIndex(loop,
                        pOne.getGlobalIndexUn(innerClassAtt.getInnerClassInfoIndex(loop),
                        ClassFile.currentClassToWorkOn,
                        TagValues.CONSTANT_Class));
            }

            //per documentation it could be zero
            if (innerClassAtt.getOuterClassInfoIndex(loop).intValueUnsigned() != 0) {
                innerClassAtt.setOuterClassInfoIndex(loop,
                        pOne.getGlobalIndexUn(innerClassAtt.getOuterClassInfoIndex(loop),
                        ClassFile.currentClassToWorkOn,
                        TagValues.CONSTANT_Class));
            }

            //per documentation it could be zero
            if (innerClassAtt.getInnerNameIndex(loop).intValueUnsigned() != 0) {
                innerClassAtt.setInnerNameIndex(loop,
                        pOne.getGlobalIndexUn(innerClassAtt.getInnerNameIndex(loop),
                        ClassFile.currentClassToWorkOn,
                        TagValues.CONSTANT_Utf8));
            }
        }
    }

    private void globalizeConstantValueAtt(ConstantValueAtt consValueAtt) throws
            Exception {
        int oldIndex = consValueAtt.getConstantValueIndex().intValueUnsigned();
        int tag = GlobalConstantPool.getConstantValueTag(ClassFile.currentClassToWorkOn.getSourceFileNameWithPath(), oldIndex);

        int globalIndex = pOne.getGlobalIndex(oldIndex,
                ClassFile.currentClassToWorkOn, tag);
        consValueAtt.setConstantValueIndex(factory.createUn(globalIndex).trim(2));
    }

    /*    private int createIndex(byte upperByte, byte lowerByte) {
    return upperByte << 8 + lowerByte;
    }
    private int createIndex(Un u2) throws Exception {
    Un.validateUnSize(2, u2);
    byte[] data = u2.getData();
    //return data[1] << 8 + data[0];
    return u2.intValueUnsigned();
    }
     */
    private int getInstructionOldIndex(Instruction inst) {
        int oldIndex = -1;
        int opCode = inst.getOpCode();
        Un operandsClone = (Un) inst.getOperandsData().clone();
        if (opCode == JavaInstructionsOpcodes.ANEWARRAY
                || opCode == JavaInstructionsOpcodes.CHECKCAST
                || opCode == JavaInstructionsOpcodes.INSTANCEOF
                || opCode == JavaInstructionsOpcodes.MULTIANEWARRAY
                || opCode == JavaInstructionsOpcodes.NEW
                || opCode == JavaInstructionsOpcodes.GETFIELD
                || opCode == JavaInstructionsOpcodes.GETSTATIC
                || opCode == JavaInstructionsOpcodes.PUTFIELD
                || opCode == JavaInstructionsOpcodes.PUTSTATIC
                || opCode == JavaInstructionsOpcodes.INVOKESPECIAL
                || opCode == JavaInstructionsOpcodes.INVOKESTATIC
                || opCode == JavaInstructionsOpcodes.INVOKEVIRTUAL
                || opCode == JavaInstructionsOpcodes.INVOKEINTERFACE
                || opCode == JavaInstructionsOpcodes.LDC_W
                || opCode == JavaInstructionsOpcodes.LDC2_W
                || opCode == JavaInstructionsOpcodes.LDC) {
            try {
                if (opCode != JavaInstructionsOpcodes.LDC) {
                    operandsClone = Un.cutBytes(2, operandsClone);
                }
            } catch (Exception d) {
                Miscellaneous.printlnErr("opcode =" + opCode);
                d.printStackTrace();
                Miscellaneous.exit();
            }
            oldIndex = operandsClone.intValueUnsigned();
        }
        return oldIndex;
    }

    public static int getInstructionCPIndexTag(Instruction inst, int oldIndex) {
        int tag = -1;
        int opCode = inst.getOpCode();
        String className = ClassFile.currentClassToWorkOn.getSourceFileNameWithPath();
        if (opCode == JavaInstructionsOpcodes.ANEWARRAY
                || opCode == JavaInstructionsOpcodes.CHECKCAST
                || opCode == JavaInstructionsOpcodes.INSTANCEOF
                || opCode == JavaInstructionsOpcodes.MULTIANEWARRAY
                || opCode == JavaInstructionsOpcodes.NEW) { //all instructions using classInfo
            tag = TagValues.CONSTANT_Class;
        } else if (opCode == JavaInstructionsOpcodes.GETFIELD
                || opCode == JavaInstructionsOpcodes.GETSTATIC
                || opCode == JavaInstructionsOpcodes.PUTFIELD
                || opCode == JavaInstructionsOpcodes.PUTSTATIC) { //all instructions using FieldRefInfo
            tag = TagValues.CONSTANT_Fieldref;
        } else if (opCode == JavaInstructionsOpcodes.INVOKESPECIAL
                || opCode == JavaInstructionsOpcodes.INVOKESTATIC
                || opCode == JavaInstructionsOpcodes.INVOKEVIRTUAL) { //all instructions using MethodRefInfo
            tag = TagValues.CONSTANT_Methodref;
        } else if (opCode == JavaInstructionsOpcodes.INVOKEINTERFACE) {
            tag = TagValues.CONSTANT_InterfaceMethodref;
        } else if (opCode == JavaInstructionsOpcodes.LDC
                || opCode == JavaInstructionsOpcodes.LDC_W
                || opCode == JavaInstructionsOpcodes.LDC2_W) { //using constants
            tag = GlobalConstantPool.getConstantValueTag(className, oldIndex);
        } else if (opCode == JavaInstructionsOpcodes.LDC_FLOAT
                || opCode == JavaInstructionsOpcodes.LDC_W_FLOAT) {
            tag = TagValues.CONSTANT_Float;
        } else if (opCode == JavaInstructionsOpcodes.LDC2_W_LONG) {
            tag = TagValues.CONSTANT_Long;
        } else if (opCode == JavaInstructionsOpcodes.LDC_W_INT
                || opCode == JavaInstructionsOpcodes.LDC_INT) {
            tag = TagValues.CONSTANT_Integer;
        }
        return tag;
    }

    private void chooseBetweenLDCAndLDCW(int newIndex, Instruction inst) {
        int opCode = inst.getOpCode();
        if (opCode == JavaInstructionsOpcodes.LDC && newIndex > 255) {
            /**
             * change it to LDC_W
             */
            inst.setOpCode(JavaInstructionsOpcodes.LDC_W);
            inst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC_W));
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC_W);

        } else if (opCode == JavaInstructionsOpcodes.LDC_W && newIndex <= 255) {
            /**
             * Change it to LDC
             */
            inst.setOpCode(JavaInstructionsOpcodes.LDC);
            inst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC));
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC);
        }
    }

    /**
     * set the global index to the new instruction.
     *
     * If we have a LDC or LDC_W instruction and
     * If the global index is greater than 255 then the instruction should be changed to
     * LDC_W if it was LDC. Otherwise if the global index is smaller than 255 and instruction
     * was LDC_W then it should be changed to LDC.
     * 
     * @param newIndex
     * @param inst
     * @throws Exception
     */
    private void setInstNewIndex(int newIndex, Instruction inst)
            throws Exception {
        chooseBetweenLDCAndLDCW(newIndex, inst);
        int opCode = inst.getOpCode();
        Un operand = factory.createUn(newIndex).trim(2);
        /**
         * Now if it is LDC then trim the operand to one byte size.
         */
        if (opCode == JavaInstructionsOpcodes.LDC) { //has one byte operand
            //Miscellaneous.println("************************ opcode == "+opCode);
            operand = operand.trim(1);
        } else if (opCode == JavaInstructionsOpcodes.INVOKEINTERFACE
                || opCode == JavaInstructionsOpcodes.MULTIANEWARRAY) { //has more than one byte operand
            inst.getOperandsData().replace(operand, 0);
            operand = inst.getOperandsData();
        }
        inst.setOperandsData(operand);
    }

    private void changeNewArrayBC(Instruction inst) {
        try {
            if (inst.getOpCode() != JavaInstructionsOpcodes.NEWARRAY) {
                return;
            }
            Un operand = inst.getOperandsData();
            int type = operand.intValueUnsigned();
            int newType = -1;
            switch (type) {
                case 4: //boolean
                    newType = FieldTypes.TYPE_JBOOLEAN;
                    break;
                case 5: // char
                    newType = FieldTypes.TYPE_JCHAR;
                    break;
                case 6: //float
                    newType = FieldTypes.TYPE_JFLOAT;
                    break;
                case 7: // double
                    newType = FieldTypes.TYPE_JDOUBLE;
                    break;
                case 8: //byte
                    newType = FieldTypes.TYPE_JBYTE;
                    break;
                case 9: //short
                    newType = FieldTypes.TYPE_JSHORT;
                    break;
                case 10: //int
                    newType = FieldTypes.TYPE_JINT;
                    break;
                case 11: //long
                    newType = FieldTypes.TYPE_JLONG;
                    break;
                default:
                    Miscellaneous.printlnErr("Invalid NewArray instruction operand" + type);
                    Miscellaneous.exit();
                    break;
            }
            inst.setOperandsData(factory.createUn(newType).trim(1));
        } catch (Exception d) {
            d.printStackTrace();
        }
    }

    /**
     * It creates total of five new instructions i.e. 
     *         ldc_float, ldc_int, ldc_w_float, ldc_w_int and ldc2_w_long
     * the opcode of these instructions are 203, 204, 205, 206, and 207 respectively
     * @param ldcInst either ldc, ldc_w or ldc2_w instructions.
     * @param className name of the class which has this instruction. Required for globlization...
     * @param oldCPIndex
     */
    private void createAdditionalLDCInstructions(Instruction ldcInst,
            int type) {
        if (ldcInst.getOpCode() == JavaInstructionsOpcodes.LDC) {

            if (type == TagValues.CONSTANT_Float) {
                ldcInst.setOpCode(JavaInstructionsOpcodes.LDC_FLOAT);
                ldcInst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC_FLOAT));
                BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC_FLOAT);
            } else if (type == TagValues.CONSTANT_Integer) {
                ldcInst.setOpCode(JavaInstructionsOpcodes.LDC_INT);
                ldcInst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC_INT));
                BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC_INT);
            }
        } else if (ldcInst.getOpCode() == JavaInstructionsOpcodes.LDC_W) {
            if (type == TagValues.CONSTANT_Float) {
                ldcInst.setOpCode(JavaInstructionsOpcodes.LDC_W_FLOAT);
                ldcInst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC_W_FLOAT));
                BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC_W_FLOAT);
            } else if (type == TagValues.CONSTANT_Integer) {
                ldcInst.setOpCode(JavaInstructionsOpcodes.LDC_W_INT);
                ldcInst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC_W_INT));
                BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC_W_INT);
            }
        } else if (ldcInst.getOpCode() == JavaInstructionsOpcodes.LDC2_W) {
            if (type == TagValues.CONSTANT_Long) {
                ldcInst.setOpCode(JavaInstructionsOpcodes.LDC2_W_LONG);
                ldcInst.setMnemonic(Instruction.getMnemonic(JavaInstructionsOpcodes.LDC2_W_LONG));
                BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LDC2_W_LONG);
            }
        }
    }

    private void globalizeCodeArray(CodeAtt codeAtt) throws Exception {
        Vector instruction = codeAtt.getInstructions();
        Instruction inst = null;
        int tag = -1;
        ClassFile classFile = ClassFile.currentClassToWorkOn;
        int oldIndex = -1;
        int newIndex = -1;
        for (int loop = 0; loop < instruction.size(); loop++) {
            try {
                inst = (Instruction) instruction.elementAt(loop);

                changeNewArrayBC(inst);
                oldIndex = getInstructionOldIndex(inst);
                if (oldIndex == -1) {//instruction does not point to any constant pool entry 
                    continue;
                }
                tag = getInstructionCPIndexTag(inst, oldIndex);
                //now get instruction new Index
                newIndex = pOne.getGlobalIndex(oldIndex, classFile, tag);

                // now set the new Index
                setInstNewIndex(newIndex, inst);
                createAdditionalLDCInstructions(inst, tag);
            } catch (Exception d) {
                Miscellaneous.printlnErr("Exception in globlizating instruction at instruction "
                        + inst + ", tag=" + tag + ", new Index=" + newIndex);
                d.printStackTrace();
                Miscellaneous.exit();
            }
        }
    }

    private void globalizeCodeAtt(CodeAtt codeAtt) throws Exception {
        int classinfoIndex;
        int size = codeAtt.getExceptionTableLength().intValueUnsigned();
        globalizeCodeArray(codeAtt);
        for (int loop = 0; loop < size; loop++) {
            Un catchType = codeAtt.getCatchType(loop);
            if (catchType.intValueUnsigned() != 0) {
//per documentation the catch_type could be zero. In that case it is supposed to catch all the exceptions.
//note that zero is not a valid index in the constant pool. Hence in that case we should not try to access
//it and no need to globalize it (it is already globalized).
                classinfoIndex = pOne.getGlobalIndex(catchType,
                        ClassFile.currentClassToWorkOn, TagValues.CONSTANT_Class);
                codeAtt.setCatchType(loop, factory.createUn(classinfoIndex).trim(2));
            }
        }
    }

    private void getAllMethodFieldAttributes(ControllerBase cont,
            Vector attritues) throws Exception {
        FieldInfo field = null;
        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            field = (FieldInfo) cont.get(loop);
            addAll(field.getAttributeController().getAll(), attritues);
        }
    }

    private void add(AttributeInfo obj, Vector ret) {
        ret.add(obj);
    }

    private void addAll(Vector objs, Vector ret) {
        for (int loop = 0; loop < objs.size(); loop++) {
            add((AttributeInfo) objs.elementAt(loop), ret);
        }
    }

    private Vector getAllAttributes() throws Exception {
        Vector ret = new Vector();

        ClassFile file = ClassFile.currentClassToWorkOn;

        //all attributes of class
        addAll(file.getAttributeInfoController().getAll(), ret);
        //all attributes of functions and fields

        getAllMethodFieldAttributes(file.getMethodInfoController(), ret);
        getAllMethodFieldAttributes(file.getFieldInfoController(), ret);

        //in case of method also get codeAttribute
        MethodInfoController mCont = file.getMethodInfoController();
        MethodInfo method = null;
        for (int mloop = 0; mloop < mCont.getCurrentSize(); mloop++) {
            method = (MethodInfo) mCont.get(mloop);
            ControllerBase base = method.getAttributeController();
            for (int aloop = 0; aloop < base.getCurrentSize(); aloop++) {
                if (base.get(aloop) instanceof CodeAtt) {
                    addAll(((CodeAtt) base.get(aloop)).getAttributes().getAll(),
                            ret);
                }
            }
        }
        return ret;
    }

    private void globalizeAllAttributes() throws Exception {

        Vector allAttributes = getAllAttributes();
//        OptimizedAttributeInfoController.getAllAttributes();
        AttributeInfo attInfo = null;
        String key = null;
        for (int loop = 0; loop < allAttributes.size(); loop++) {
            attInfo = (AttributeInfo) allAttributes.elementAt(loop);
            key = null;
            if (attInfo instanceof LineNumberTableAtt) {
                key = AttributeNameIndexValues.LINE_NUMBER_TABLE;
            } else if (attInfo instanceof SourceFileAtt) {
                key = AttributeNameIndexValues.SOURCE_FILE;
                globalizeSourceFileAtt((SourceFileAtt) attInfo);
            } else if (attInfo instanceof CodeAtt) {
                key = AttributeNameIndexValues.CODE;
                globalizeCodeAtt((CodeAtt) attInfo);
            } else if (attInfo instanceof LocalVariableTableAtt) {
                key = AttributeNameIndexValues.LOCAL_VARIABLE_TABLE;
                globalizeLocalVariableTableAtt((LocalVariableTableAtt) attInfo);
            } else if (attInfo instanceof ExceptionsAtt) {
                key = AttributeNameIndexValues.EXCEPTION;
                // do not do this for the time being globalizeExceptionAtt((ExceptionsAtt) attInfo);
            } else if (attInfo instanceof InnerClassesAtt) {
                key = AttributeNameIndexValues.INNERCLASSES;
                //do not do this for the time being globalizeInnerClassAtt((InnerClassesAtt) attInfo);
            } else if (attInfo instanceof ConstantValueAtt) {
                key = AttributeNameIndexValues.CONSTANT_VALUE;
                //do not do this for the time being globalizeConstantValueAtt((ConstantValueAtt) attInfo);
            } else if (attInfo instanceof SyntheticAtt) {
                key = AttributeNameIndexValues.SYNTHETIC;
            } else if (attInfo instanceof DeprecatedAtt) {
                key = AttributeNameIndexValues.DEPRECATED;
            } else {
                // other attributes might be VM specific and we are not required to recognized.
            }
            //if (key != null) {
            Un globalIndex = pOne.getGlobalIndexUn(
                    attInfo.getAttributeNameIndex(),
                    ClassFile.currentClassToWorkOn,
                    TagValues.CONSTANT_Utf8);

            //Miscellaneous.println(key+", "+attInfo.getAttributeNameIndex()+" ---->"+globalIndex);
            attInfo.setAttributeNameIndex(globalIndex);
            // }
        }
    }
}
