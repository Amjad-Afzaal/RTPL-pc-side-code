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
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.bytecode.branchSetter.logic.*;

/**
 * 
 * Description:
 * <p>
 *
 * The invokeInterface instruction is a useless hence not implemented by TakaTuka.
 * We replace invokeinterface with invokevirtual. Furthermore we replace also the
 * operand of invokeinterface with any method that implement that interface.
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class InvokeInterfaceReplacer {

    private GlobalConstantPool cp = GlobalConstantPool.getInstanceOf();
    private Oracle oracle = Oracle.getInstanceOf();
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private static final InvokeInterfaceReplacer invokeReplacer =
            new InvokeInterfaceReplacer();
    private boolean allInterfaceReplaced = true;
    private HashSet<NotFoundedInkInterfaceReplacer> notFoundedInterfaceImp = new HashSet();
    private HashSet<String> interfacesRemove = new HashSet();

    private InvokeInterfaceReplacer() {
    }

    public static InvokeInterfaceReplacer getInstanceOf() {
        return invokeReplacer;
    }

    public HashSet<String> getNameOfInterfacesRemove() {
        return interfacesRemove;
    }

    public void execute() {
        Vector<CodeAttCache> codeAttVect = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> it = codeAttVect.iterator();
        while (it.hasNext()) {
            execute(it.next());
        }
        if (allInterfaceReplaced) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.INVOKEINTERFACE);
        } else {
            LogHolder.getInstanceOf().addLog("**** Warning: Cannot replace INVOKEINTERFACE for the"
                    + " following interfaces " + notFoundedInterfaceImp
                    + " as no class implementing them is found. "
                    + "It may produce a runtime error.");
        }
        removeAllInterfaceUselessData();
    }

    /**
     * remove interface method, fields.
     *  
     * if interface extend interface then change the superclass to it. Otherwise keep it same.
     * 
     */
    private void removeAllInterfaceUselessData() {
        ClassFileController cfCont = ClassFileController.getInstanceOf();
        try {
            for (int loop = 0; loop < cfCont.getCurrentSize(); loop++) {
                ClassFile cFile = (ClassFile) cfCont.get(loop);
                if (cFile.getAccessFlags().isInterface()) {
                    //Miscellaneous.println("Found interface "+cFile.getFullyQualifiedClassName());
                    ClassFile interFile = (ClassFile) cfCont.get(loop);
                    //clear the methods
                    interFile.getMethodInfoController().clear();
                    //clear the fields
                    interFile.getFieldInfoController().clear();
                    //change super class
                    //todo ------ changeInterfaceSuperClass(cFile);
                    //todo interfacesRemove.add(interFile.getFullyQualifiedClassName());
                    //loop--;
                }
            }
            LogHolder.getInstanceOf().addLog("Interfaces deleted =" + interfacesRemove);
            StatsHolder.getInstanceOf().addStat("Interfaces deleted ", interfacesRemove);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void changeInterfaceSuperClass(ClassFile cFile) throws Exception {
        InterfaceController intCont = cFile.getInterfaceController();
        if (intCont.getCurrentSize() == 0) {
            return;
        } else if (intCont.getCurrentSize() > 1) {
            Miscellaneous.printlnErr("***************** ERROR # 2341 *****");
            Miscellaneous.exit();
        }
        cFile.setSuperClass((Un) intCont.get(0));

    }

    /**
     * Assumption: The globlization is already done and branch addresses are already saved.
     * That is how this function works.
     * 
     * 1. Find an instruction INVOKEINTERFACE.
     * 
     * 2. Reduce the size of this instruction operands to 2 (from 4).
     * 
     * 3. Get the operand
     *    -- use the operand to index constant pool entry of methodRefInfo (say M-Info).
     *    -- get the class file index (say C-Index) from that entry.
     * 4. Find a class File which implements C-Index. Find M-Info from it.
     * 5. Replace operand with this M-Info constant pool index
     * 
     * 6. remove constant pool entries which has interfacemethodrefinfos
     * 7. from the class file controller remove all the interfaces
     * 
     * @param codeAttInfo
     */
    private void execute(CodeAttCache codeAttInfo) {
        try {
            CodeAtt codeAtt = (CodeAtt) codeAttInfo.getAttribute();
            MethodInfo method = codeAttInfo.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);
            Vector<Instruction> instrsVec = codeAtt.getInstructions();
            boolean isChanged = false;
            for (int loop = 0; loop < instrsVec.size(); loop++) {
                Instruction inst = instrsVec.get(loop);
                //1
                if (inst.getOpCode() == JavaInstructionsOpcodes.INVOKEINTERFACE) {
                    inst.setOpCode(JavaInstructionsOpcodes.INVOKEVIRTUAL);
                    inst.setMnemonic(ReplaceInstrAWithB.getMnemonic(
                            JavaInstructionsOpcodes.INVOKEVIRTUAL));
                    replaceOperand(inst, codeAttInfo.getClassFile()); // implements step 2 - 4
                    isChanged = true;
                }
            }
            if (isChanged) {
                codeAtt.setInstructions(instrsVec);
                BranchInstructionsHandler.restoreBranchInformation(codeAtt);
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void replaceOperand(Instruction inst, ClassFile codeFile) throws Exception {
        //2 reduce the size of instruction operand from 2 to 4
        inst.setOperandsData(Un.cutBytes(2, inst.getOperandsData()));
        int cpIndex = inst.getOperandsData().intValueUnsigned();
        ReferenceInfo mInfo = (ReferenceInfo) cp.get(cpIndex,
                TagValues.CONSTANT_InterfaceMethodref);
        if (mInfo == null) {
            Miscellaneous.printlnErr("Error # 592 ");
            Miscellaneous.exit();
        }
        //System.out.println("the code file = "+codeFile.getFullyQualifiedClassName());
        Un thisPointer = mInfo.getIndex();
        HashSet cFileVector = findClassesImplementsThis(thisPointer, codeFile);
        //***following code is for debugging
        /*
        System.out.println("the classes implementing " + thisPointer + " are :");
        Iterator debugIt = cFileVector.iterator();
        while (debugIt.hasNext()) {
        ClassFile cFile = (ClassFile) debugIt.next();
        System.out.println(" ... " + cFile.getFullyQualifiedClassName());
        }
         *
         */
        //***end of debugging code.

        //ClassFile cFileTest = oracle.getClass(thisPointer);

        Iterator<ClassFile> it = cFileVector.iterator();
        Un newOperand = null;
        ClassFile interfaceImpl = null;
        FieldInfo method = null;
        while (it.hasNext()) {
            interfaceImpl = it.next();
            int methodIndex = oracle.getReferenceFieldFromClassFile_GCP(mInfo, interfaceImpl, true);
            if (methodIndex == -1) {
                continue;
            }
            method = (FieldInfo) interfaceImpl.getMethodInfoController().get(methodIndex);

            int methodCPIndex = oracle.existFieldInfoCPIndex(method, true,
                    interfaceImpl.getThisClass().intValueUnsigned());
            if (methodCPIndex != -1) {
                newOperand = factory.createUn(methodCPIndex).trim(2);
                break;
            }
        }
        if (newOperand == null) {
            allInterfaceReplaced = false;
            notFoundedInterfaceImp.add(new NotFoundedInkInterfaceReplacer(oracle.getClassInfoName(thisPointer), codeFile.getFullyQualifiedClassName()));
            //return;
            newOperand = factory.createUn(Short.MAX_VALUE).trim(2);
        }
///        Miscellaneous.println(" replaced operand for  " + oracle.getClassInfoName(thisPointer));
        inst.setOperandsData(newOperand);
    }

    /**
     * Given the thisPointer of an interface this function should return
     * all the classes implemented that interface or any of its sub interfaces.
     *
     * @param thisPointer
     * @param currentcFile
     * @return
     */
    private HashSet findClassesImplementsThis(Un thisPointer, ClassFile currentcFile) {
        HashSet<ClassFile> ret = new HashSet();
        try {
            ClassFile cFile = oracle.getClass(thisPointer, GlobalConstantPool.getInstanceOf());
            if (cFile == null) {
                //no implementation of that interface is found. Hence return back.
                LogHolder.getInstanceOf().addLog("Waring!!! no implementation of an interface" +
                        " is found in code of class file ="+currentcFile.getFullyQualifiedClassName(), false);
                return ret;
            }
            oracle.getAllImplementations(cFile, ret);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    private class NotFoundedInkInterfaceReplacer implements Comparable<NotFoundedInkInterfaceReplacer> {

        String interfaceName = null;
        String className = null;

        public NotFoundedInkInterfaceReplacer(String interfaceName,
                String className) {
            this.interfaceName = interfaceName;
            this.className = className;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof NotFoundedInkInterfaceReplacer)) {
                return false;
            }
            NotFoundedInkInterfaceReplacer input = (NotFoundedInkInterfaceReplacer) obj;
            if (this.interfaceName.equals(input.interfaceName)) {
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + (this.interfaceName != null ? this.interfaceName.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return className + "->" + interfaceName;
        }

        public int compareTo(InvokeInterfaceReplacer.NotFoundedInkInterfaceReplacer o) {
            return o.interfaceName.compareTo(interfaceName);
        }
    }
}
