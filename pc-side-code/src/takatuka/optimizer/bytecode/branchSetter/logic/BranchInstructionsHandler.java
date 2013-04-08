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
package takatuka.optimizer.bytecode.branchSetter.logic;

import takatuka.classreader.dataObjs.attribute.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.tukFormat.dataObjs.LFMethodInfo;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.optimizer.bytecode.changer.logic.comb.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.classreader.logic.util.*;
/**
 * 
 * Description:
 * <p>
 *
 *  This class is responsible for keeping branch addresses intact. 
 *  That is how it works.
 *  1) Before optimization one must call this class saveBranchInformation function
 *     That function will save that what is branch traget and what it is source.
 *  2) During optimization one must call this class function isBranchSrcAndDestInstruction.
 *     If that function returns true then one must not change, optimize or remove that
 *     instruction.
 *     Note: The branch addresses cannot be adjust properly if branch source or traget instructions are changed.
 *  3) After optimization one must call restoreBranchInformation function. Based on the
 *     information saved in step one and current state of Instructions, this 
 *     function will correctly set the branch addresses.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class BranchInstructionsHandler {

    private static final BranchInstructionsHandler brachInstHandler =
            new BranchInstructionsHandler();
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    private static boolean shouldDebug = false;

    public static BranchInstructionsHandler getInstanceOf() {
        return brachInstHandler;
    }

    public void restoreBranchInformation() {
        ClassFile classFile = null;
        MethodInfo method = null;
        try {
            logHolder.addLog("restoring branch information ...");
            Oracle oracle = Oracle.getInstanceOf();
            ClassFileController classFileCont = ClassFileController.getInstanceOf();
            for (int classFileIndex = 0; classFileIndex < classFileCont.getCurrentSize(); classFileIndex++) {
                classFile = (ClassFile) classFileCont.get(classFileIndex);

                MethodInfoController methodInfoController = classFile.getMethodInfoController();
                int methodContSize = methodInfoController.getCurrentSize();
                for (int methodIndex = 0; methodIndex < methodContSize; methodIndex++) {
                    method = (MethodInfo) methodInfoController.get(methodIndex);
                    String methodStr = oracle.getMethodOrFieldString(method);
                    /*if (methodStr.contains("main([Ljava/lang/String;)V")) {
                        System.out.println("stop here");
                        System.out.println(method.getInstructions());
                    }*/
                    BHCodeAtt codeAtt = null;
                    codeAtt = (BHCodeAtt) method.getCodeAtt();
                    if (codeAtt == null || codeAtt.getCodeLength().intValueUnsigned() == 0) {
                        continue;
                    }
                    codeAtt.verifyCodeAttribute();
                    setAllOffsets(codeAtt.getInstructions());
                    restoreBranchInformation(codeAtt);
                }
            }
        } catch (Exception d) {
            Miscellaneous.printlnErr("branch error at class=" +
                    classFile.getFullyQualifiedClassName() + ", method=" +
                    ((LFMethodInfo) method).getName());
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    public boolean isBranchSrcAndDestInstruction(Instruction inst) {
        return ((BHInstruction) inst).isBranchSrcAndDst();
    }

    public boolean isBranchSourceInstruction(Instruction inst) {
        return ((BHInstruction) inst).isBranchSource();
    }

    public boolean isBranchTargetInstruction(Instruction inst) {
        return ((BHInstruction) inst).isBranchTarget();
    }

    /**
     * This function restore the branch addresses after optimization of the bytecode.
     * This function works only if an optimization-code has called saveBranchInformation 
     * before optimization starts and 
     * has use isBranchSrcAndDestInstruction for avoiding optimization of branch instructions.
     * 
     * @param changedCode
     */
    public static void restoreBranchInformation(CodeAtt changedCode) {
        if (changedCode == null || changedCode.getCodeLength().intValueUnsigned() == 0) {
            return; //we are done here.
        }
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(changedCode.getMethod());
        Vector instructions = changedCode.getInstructions();
        Vector branchTargets = null;
        //Miscellaneous.println(" all instructions 1= "+ instructions);
        for (int loop = 0; loop < instructions.size(); loop++) {
            BHInstruction currInstr = (BHInstruction) instructions.elementAt(loop);
            branchTargets = currInstr.getMyTargets();

            changedCode.restorePCInfoInExceptionTables(currInstr);

            if (branchTargets == null || branchTargets.size() == 0) {
                continue; // instruction does not have any target
            }
           /* if (currInstr.getMnemonic().contains("SWITCH")) {
                Miscellaneous.println(" all Instructions = " + instructions + "\n\n");
                Miscellaneous.println(" here here =" + currInstr);
                setAllOffsets(instructions);
            }*/
            //Miscellaneous.println("Targets  ="+branchTargets);
            //Miscellaneous.println("\n\n"+loop+",   "+currInstr+"----------- all instructions -------- "+instructions+"\n\n\n");
            Vector addresses = getInstructionOffset(branchTargets,
                    instructions, currInstr.getOffSet());
            //Miscellaneous.println("addresses  ="+addresses);
            currInstr.setBranchAddresses(addresses);
        }
    // Miscellaneous.println(" all instructions 2= "+ instructions);
    }

    private static Vector getInstructionOffset(Vector branchTargets, Vector allInstr,
            int currentOffSet) {

        Vector ret = new Vector();
        if (branchTargets.size() == 0 || allInstr.size() == 0) {
            return ret;
        }
        BranchTarget bTarget = null;
        for (int loop = 0; loop < branchTargets.size(); loop++) {
            bTarget = (BranchTarget) branchTargets.elementAt(loop);
            BHInstruction instr = getInstructionWithTargetId(allInstr,
                    bTarget.targetId);
            ret.addElement(instr.getOffSet() - currentOffSet);
        }
        return ret;
    }

    private static BHInstruction getInstructionWithTargetId(Vector<BHInstruction> allInstr,
            long targetId) {
        int size = allInstr.size();
        //System.out.println("del me "+allInstr);
        for (int loop = 0; loop < size; loop++) {
            BHInstruction inst = allInstr.elementAt(loop);
            Vector<BHInstruction> targets = inst.getTargetInstructionsInMe();
            for (int innerLoop = 0; innerLoop < targets.size(); innerLoop++) {
                BHInstruction target = targets.elementAt(innerLoop);
                if (target.getInstructionId() == targetId) {
                    return inst;
                }
            }
        }
        return null;
    }

    private static void setAllOffsets(Vector<BHInstruction> allInstrInput) {
        int offset = 0;
        int padding = 0;
        for (int loop = 0; loop < allInstrInput.size(); loop++) {
            padding = 0;
            BHInstruction currInstr = allInstrInput.elementAt(loop);
            if (!(currInstr instanceof InstructionsCombined) && 
                    (currInstr.getMnemonic().equals("TABLESWITCH") ||
                    currInstr.getMnemonic().equals("LOOKUPSWITCH"))) {
              padding = currInstr.getOperandsData().size() % 4;
            }
            currInstr.setOffSet(offset);
            if (shouldDebug) {
                Miscellaneous.println("offSet ="+offset+": instr="+currInstr);
            }
            offset += currInstr.length()-padding;
        }

    }

    private class InstructionsIdComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            long n1 = 0;
            long n2 = 0;
            if (o1 instanceof BHInstruction) {
                n1 = ((BHInstruction) o1).getInstructionId();
            } else {
                n1 = (Long) o1;
            }
            if (o2 instanceof BHInstruction) {
                n2 = ((BHInstruction) o2).getInstructionId();
            } else {
                n1 = (Long) o2;
            }
            return new Long(n1).compareTo(n2);
        }
    }
}
