/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.OGI.FTT.CreateIntraMethodDAG;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNodeRefsCalc;
import takatuka.offlineGC.OGI.GraphUtils.ReferenceFilter;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.tukFormat.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * The objective of this class is to produce results for offLineGC.
 * It is accomplish by following.
 * 1) After each method call (note that new is also a method call to a constructor)
 *    and after each OFFLINE_GC_FREE instruction, adds following code:
 *
 *    takatuka.vm.VM.printFreeMemory();
 *    
 *    In bytecode it should be like:
 *
 *   0:	invokestatic	#2; //Method takatuka/vm/VM.printFreeMemory:()V
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenerateInstrForResults {

    private static final GenerateInstrForResults myObj = new GenerateInstrForResults();
    private static final String VM_CLASS_NAME = "takatuka/vm/VM";
    private static final String VM_PRINT_FREE_MEMORY_NAME = "printFreeMemory";
    private static final String VM_PRINT_FREE_MEMORY_DESC = "()V";
    private static final String VM_PRINT_PRINTLN_0_NAME = "println0";
    private static final String VM_PRINT_PRINTLN_0_DESC = "()V";
    /**
     * after how many bytecode instruction it should insert a freememory instruction.
     */
    public static int insertInstrAfter = -1;
    private int printMemoryFunctionCPIndex = -1;
    private int println0CPIndex = -1;
    private static int totalInstrGenerater = 0;
    public static String functionAllowed = "-";
    private List<String> functionAllowedList = null;

    /**
     * The constructor is private
     */
    private GenerateInstrForResults() {
    }

    public static final GenerateInstrForResults getInstanceOf() {
        return myObj;
    }


    private boolean methodAllowed(String methodStr) {
        Iterator<String> it = functionAllowedList.iterator();
        while (it.hasNext()) {
            String str = it.next();
            if (methodStr.contains(str)) {
                return true;
            }
        }
        return false;
    }
    public void execute() {
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> codeAttCacheVector = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> it = codeAttCacheVector.iterator();
        LogHolder.getInstanceOf().addLog(" filter for methods =" + functionAllowed, true);
        boolean allowAllFunction = functionAllowed.contains("-");
        String[] functionAllowedArray = functionAllowed.split(",");
        functionAllowedList = Arrays.asList(functionAllowedArray);
        while (it.hasNext()) {
            CodeAttCache codeAttCache = it.next();
            MethodInfo method = codeAttCache.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);

            if (methodStr.startsWith("java.") || methodStr.startsWith("takatuka.vm")
                    || (!allowAllFunction && !methodAllowed(methodStr))) {
                continue;
            }

            CodeAtt codeAtt = (CodeAtt) codeAttCache.getAttribute();
            Vector<Instruction> instrVec = codeAtt.getInstructions();
            Vector<Instruction> instrToInsert = getInstrToInsert(codeAtt);
            Vector<Long> placeToInsertNewInstr = findPlacesToAddInstr(instrVec);
            if (placeToInsertNewInstr.size() != 0) {
                LogHolder.getInstanceOf().addLog("added GC resutls instruction in method = " + methodStr + ", at "
                        + placeToInsertNewInstr.size() + " places", true);
            }
            Vector<Instruction> newInstrSet = new Vector<Instruction>();
            for (int loop = 0; loop < instrVec.size(); loop++) {
                if (loop == 0 && !allowAllFunction) {
                    //newInstrSet.addAll(getInstrToInsertBeforeAMethodStart(codeAtt));
                    //newInstrSet.addAll(getInstrToInsertBeforeAMethodStart(codeAtt));
                }
                Instruction instr = instrVec.elementAt(loop);
                newInstrSet.addElement(instr);
                long instrId = instr.getInstructionId();
                if (placeToInsertNewInstr.contains(instrId)) {
                    newInstrSet.addAll(instrToInsert);
                    totalInstrGenerater++;
                }
            }
            if (placeToInsertNewInstr.size() != 0) {
                codeAtt.setInstructions(newInstrSet);
                //System.out.println(method.getCodeAtt().getMaxStack().intValueUnsigned());
                //System.out.println(method.getInstructions());
            }
        }
        LogHolder.getInstanceOf().addLog("total instrs for offline GC resutls ="
                + totalInstrGenerater, true);
    }

    private int getMethodOrFieldCPIndex(String cFileFQN, String name, String descr, boolean isMethod) {
        Oracle oracle = Oracle.getInstanceOf();
        ClassFile cFile = oracle.getClass(cFileFQN);
        LFFieldInfo field = (LFFieldInfo) oracle.getMethodOrField(cFile, name, descr, isMethod);
        int ret = oracle.existFieldInfoCPIndex(field, isMethod,
                cFile.getThisClass().intValueUnsigned());
        return ret;
    }

    private int getPrintFreeMemoryMethodIndexRunTimeMethodIndex() {
        if (printMemoryFunctionCPIndex != -1) {
            return printMemoryFunctionCPIndex;
        }
        printMemoryFunctionCPIndex = getMethodOrFieldCPIndex(VM_CLASS_NAME,
                VM_PRINT_FREE_MEMORY_NAME, VM_PRINT_FREE_MEMORY_DESC, true);
        return printMemoryFunctionCPIndex;
    }

    private int getprintln0MethodIndexRunTimeMethodIndex() {
        if (println0CPIndex != -1) {
            return println0CPIndex;
        }
        println0CPIndex = getMethodOrFieldCPIndex(VM_CLASS_NAME,
                VM_PRINT_PRINTLN_0_NAME, VM_PRINT_PRINTLN_0_DESC, true);
        return println0CPIndex;
    }

    /**
     * return back instruction id after which the new instructions needs to be 
     * inserted.
     * That is after each
     * - invokevirtual
     * - invokestatic
     * - invokespecial
     * - offline_GC_Free
     *
     * @param instrVec
     * @return
     */
    private Vector<Long> findPlacesToAddInstr(Vector<Instruction> instrVec) {
        Vector<Long> ret = new Vector<Long>();
        MethodInfo method = instrVec.get(0).getMethod();
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        if (methodStr.contains("<")) {
            return ret; //do not add instruction in the init methods of the class or object.
        }

        //System.out.println(instrVec+"......");
        int freeInstrCount = 0;
        boolean addedInstr = false;
        if (false) {
            CreateIntraMethodDAG createDAG = CreateIntraMethodDAG.getInstanceOf();
            DAGNode dagNode = createDAG.createDAG(method, null,
                    DAGNodeRefsCalc.getInstanceOf(), ReferenceFilter.getInstanceOf());
            Stack<DAGNode> stack = new Stack<DAGNode>();
            stack.push(dagNode);
            int checkedInstrcount = 0;
            while (!stack.empty()) {
                DAGNode dAGNode = stack.pop();
                if (dagNode.getGraphNodes().size() == 1) {
                    /*
                     * Can add instruction as the DAGNode does not represent a loop
                     */
                    GCInstruction instr = dagNode.getGraphNodes().iterator().next().getInstruction().getNormalInstrs().firstElement();
                    if (checkedInstrcount % insertInstrAfter == 0) {
                        ret.addElement(instr.getInstructionId());
                        freeInstrCount++;
                        addedInstr = true;
                    }
                }
                checkedInstrcount += dagNode.getGraphNodes().size();
            }
        } else {
            for (int loop = 0; loop < instrVec.size(); loop++) {
                GCInstruction instr = (GCInstruction) instrVec.elementAt(loop);
                if (loop % insertInstrAfter == 0) {
                    ret.addElement(instr.getInstructionId());
                    freeInstrCount++;
                    addedInstr = true;
                }
            }
        }
        return ret;
    }

    private Instruction createInstruction(int opCode, int operand, CodeAtt parentCodeAtt) {
        Instruction ret = null;
        try {
            FactoryFacade factoryFacade = FactoryPlaceholder.getInstanceOf().getFactory();
            Un operandUn = factoryFacade.createUn();
            if (operand != -1) {
                operandUn = factoryFacade.createUn(operand).trim(2);
            }
            ret = factoryFacade.createInstruction(opCode, operandUn, parentCodeAtt);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    private Vector<Instruction> getInstrToInsertBeforeAMethodStart(CodeAtt codeAtt) {
        Vector<Instruction> ret = new Vector<Instruction>();
        try {

            /**
             * 0:	invokestatic	#?; //Method takatuka/vm/VM.printFreeMemory:()V
             */
            Instruction invokeStatic = createInstruction(JavaInstructionsOpcodes.INVOKESTATIC,
                    getprintln0MethodIndexRunTimeMethodIndex(), codeAtt);
            ret.addElement(invokeStatic);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    /**
     * The following instructions are returns in a vector.
     *
     * 0:	invokestatic	#?; //Method takatuka/vm/VM.printFreeMemory:()V
     *
     * @param codeAtt
     * @return
     */
    private Vector<Instruction> getInstrToInsert(CodeAtt codeAtt) {
        Vector<Instruction> ret = new Vector<Instruction>();
        try {

            /**
             * 0:	invokestatic	#?; //Method takatuka/vm/VM.printFreeMemory:()V
             */
            Instruction invokeStatic = createInstruction(JavaInstructionsOpcodes.INVOKESTATIC,
                    getPrintFreeMemoryMethodIndexRunTimeMethodIndex(), codeAtt);
            ret.addElement(invokeStatic);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    /**
     * increase max stack of the given codeAtt by 3 stack slots of size 4 bytes
     * each.
     * 
     * @param codeAtt
     */
    private void increateMaxStack(CodeAtt codeAtt) {
        try {
            if (codeAtt.getInstructions().size() == 0) {
                return;
            }
            int oldMaxStack = codeAtt.getMaxStack().intValueUnsigned();
            oldMaxStack = oldMaxStack + 3;
            FactoryFacade factoryFacade = FactoryPlaceholder.getInstanceOf().getFactory();
            codeAtt.setMaxStack(factoryFacade.createUn(oldMaxStack).trim(2));
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
