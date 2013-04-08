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
import takatuka.classreader.logic.factory.*;
import takatuka.optimizer.bytecode.branchSetter.logic.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * 
 * If a field is only put and never get. Then for such field this class remove all
 * the put related code.
 * 
 * The algorithm is as follows:
 * 
 * Note that putfield pop two values from stack and putstatic pop one value 
 * from stack. 
 * Hence the basic logic of algorithm is to remove put<>. Furthermore, remove instruction
 * before it if it is pushing a value on the stack. Such insturctions include (1)
 * 1. <>const<> 
 * 2. <>load<> 
 * 3. <>push
 * 4. getfield
 * In case of putfield we need to replace putfield with a pop. If we remove an instruction before it.
 * otherwise we have to replace putfield with a pop2.
 * In case of putstatic we simply remove it if we had remove an insturction before it. 
 * otherwise we have to replace putstatic with a pop.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class RemoveFieldBytecode {

    private static final RemoveFieldBytecode remFieldBytecode = new RemoveFieldBytecode();
    private Oracle oracle = Oracle.getInstanceOf();
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private boolean isCodeChanged = false;
    private BranchInstructionsHandler branchHandler = BranchInstructionsHandler.getInstanceOf();
    private static final int PREV_INST_NOT_TOUCHED = 0;
    private static final int PREV_INST_CHANGED_1 = 1;
    private static final int PREV_INST_REMOVED = 2;

    private RemoveFieldBytecode() {
    //no one creates me but me.
    }

    public static RemoveFieldBytecode getInstanceOf() {
        return remFieldBytecode;
    }

    public boolean isRemoveBC() {
        return isCodeChanged;
    }

    /**
     * This is the starting point.
     * 
     * Get all the code attributes. Go through instructions
     * if you find a putfield or putstatic then call processPutFS
     */
    public void execute() {
        try {
            isCodeChanged = false;
            Vector<CodeAttCache> codeAttVec = oracle.getAllCodeAtt();
            Iterator<CodeAttCache> codeAttIt = codeAttVec.iterator();
            CodeAtt codeAtt = null;
            while (codeAttIt.hasNext()) {
                CodeAttCache cAttInfo = codeAttIt.next();
                ////Miscellaneous.println("\nbefore code removal =" + cAttInfo);
                codeAtt = (CodeAtt) cAttInfo.getAttribute();
                execute(codeAtt, cAttInfo.getClassFile());
            ////Miscellaneous.println("\nafter code removal =" + cAttInfo);
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void execute(CodeAtt codeAtt, ClassFile codeCFile) throws Exception {
        Instruction inst = null;
        Vector<Instruction> instrVect = codeAtt.getInstructions();
        for (int instIndex = 0; instIndex < instrVect.size(); instIndex++) {
            inst = instrVect.get(instIndex);
            if (inst.getOpCode() == JavaInstructionsOpcodes.PUTFIELD ||
                    inst.getOpCode() == JavaInstructionsOpcodes.PUTSTATIC) {
                int numberOfInstrRemove = processPutFS(instIndex, instrVect, codeCFile);
                instIndex -= numberOfInstrRemove;
            }
        }
    }

    private int processPutFS(int currentIndex, Vector<Instruction> allInstruction,
            ClassFile codeCFile) throws Exception {
        Instruction inst = allInstruction.get(currentIndex);
        int cpIndex = inst.getOperandsData().intValueUnsigned();
        if (!DCFieldInfo.isPutOnlyField(cpIndex, codeCFile.getFullyQualifiedClassName())) {
            return 0;
        }

        int statusPreviousInst = removeOrChangedPrevInstruction(currentIndex - 1, allInstruction);
        return removeOrReplaceCurrentInst(statusPreviousInst, currentIndex,
                allInstruction);
    }

    /**
     * if 1:- if the current inst is putstatic and previous instruction was removed.
     * then:- simply remove current instr
     * ---
     * if 2: if the current inst is putstatic and previous instru was NOT removed.
     * then:- replace current instruc with pop.
     * ---
     * if 3: if the current inst is putfield and previous inst was removed.
     * then:- replace current inst with pop
     * ----
     * if 4: if the current inst is putfield and prev inst was NOT removed.
     * then:- replace current inst with pop2
     * 
     * @param statusPrevInst
     * @param currentIndex
     * @param allInstruction
     * @return
     */
    private int removeOrReplaceCurrentInst(int statusPrevInst,
            int currentIndex, Vector<Instruction> allInstruction) {
        int numberOfInstrRemove = 0;
        if (statusPrevInst == PREV_INST_REMOVED) {
            numberOfInstrRemove++;
            currentIndex--;
        }
        boolean isPrevRemCha = statusPrevInst != PREV_INST_NOT_TOUCHED;
        try {
            Instruction currInst = allInstruction.get(currentIndex);
            if (currInst.getOpCode() != JavaInstructionsOpcodes.PUTFIELD ||
                    currInst.getOpCode() != JavaInstructionsOpcodes.PUTSTATIC) {
                Miscellaneous.println("******** Error: # 619... Exiting....");
                Miscellaneous.exit();
            }
            boolean isPutStatic = currInst.getOpCode() == JavaInstructionsOpcodes.PUTSTATIC;
            // if 1
            if (isPutStatic && isPrevRemCha) {
                if (branchHandler.isBranchTargetInstruction(currInst)) {
                    replaceInstruction(currInst, JavaInstructionsOpcodes.NOP);
                } else {
                    isCodeChanged = true;
                    allInstruction.remove(currentIndex);
                    numberOfInstrRemove++;
                }
            } else if ((isPutStatic && !isPrevRemCha) ||
                    !isPutStatic && isPrevRemCha) { //if 2 & 3
                //Todo fix error
                replaceInstruction(currInst, JavaInstructionsOpcodes.POP);
            } else if (!isPutStatic && !isPrevRemCha) { //if 4
                //Todo fix error
                replaceInstruction(currInst, JavaInstructionsOpcodes.POP2);
            } else {
                Miscellaneous.printlnErr("Error # 490 ");
                Miscellaneous.exit();
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return numberOfInstrRemove;
    }

    private void replaceInstruction(Instruction inst,
            int newOpcode) throws Exception {
        this.isCodeChanged = true;
        inst.setOpCode(newOpcode);
        inst.setMnemonic(Instruction.getMnemonic(newOpcode));
        inst.setOperandsData(factory.createUn());
    }

    /**
     * 
     * return PREV_INST_NOT_TOUCHED if previous instr. not removed or changed.
     * return PREV_INST_REMOVED if previous instr. removed
     * return PREV_INST_CHANGED if previous instr. changed
     * 
     * @param prevInstIndex
     * @param allInstruction
     * @return
     * @throws java.lang.Exception
     */
    private int removeOrChangedPrevInstruction(int prevInstIndex,
            Vector<Instruction> allInstruction) throws Exception {
        if (true) {
        //return false;
        }
        if (prevInstIndex < 0) {
            return PREV_INST_NOT_TOUCHED;
        }
        int ret = PREV_INST_NOT_TOUCHED;
        Instruction prevInst = allInstruction.get(prevInstIndex);
        //todo one can also do this thing with iAdd and others math operators.
        if (allowedInstructionRemoved(prevInst)) {
            if (branchHandler.isBranchTargetInstruction(prevInst)) {
                if (allowedReplacedWithPop(prevInst)) {
                    replaceInstruction(prevInst, JavaInstructionsOpcodes.POP);
                } else {
                    replaceInstruction(prevInst, JavaInstructionsOpcodes.POP2);
                }
                ret = PREV_INST_CHANGED_1;
            } else {
                allInstruction.remove(prevInst);
                ret = PREV_INST_REMOVED;
            }
        }
        return ret;
    }

    /**
     * return true if instruction is in the
     * list of instructions pushing incrementing stack pointer by one.
     * The push could be of any type like doule, long, int or reference etc.
     * @param inst
     * @return
     */
    private boolean allowedInstructionRemoved(Instruction inst) {
        int opcode = inst.getOpCode();
        if ((opcode >= JavaInstructionsOpcodes.ACONST_NULL &&
                opcode <= JavaInstructionsOpcodes.SIPUSH) ||
                (opcode >= JavaInstructionsOpcodes.ILOAD &&
                opcode <= JavaInstructionsOpcodes.SALOAD) ||
                opcode == JavaInstructionsOpcodes.GETFIELD ||
                (opcode >= JavaInstructionsOpcodes.IADD &&
                opcode <= JavaInstructionsOpcodes.DREM) ||
                (opcode >= JavaInstructionsOpcodes.ISHL &&
                opcode <= JavaInstructionsOpcodes.LXOR)) {
            return true;
        }
        return false;
    }

    /**
     * return true if instruction is subset of allowedInstructionRemoved
     * and and push value of category one. If returns false the the value could be
     * replaced by POP2 instead of pop. If instruction is not sub-set of list
     * allowedInstrctuionRemoved then this function throws an Exception
     * @param inst
     * @return
     */
    private boolean allowedReplacedWithPop(Instruction inst) {
        if (!allowedInstructionRemoved(inst)) {
            throw new UnsupportedOperationException();
        }
        int opcode = inst.getOpCode();
        FieldRefInfo f;
        //Todo carefully opcode == JavaInstructionsOpcodes.GETFIELD
        if ((opcode >= JavaInstructionsOpcodes.LCONST_0 &&
                opcode <= JavaInstructionsOpcodes.LCONST_0) ||
                (opcode >= JavaInstructionsOpcodes.DCONST_0 &&
                opcode <= JavaInstructionsOpcodes.DCONST_1) ||
                opcode == JavaInstructionsOpcodes.LLOAD ||
                opcode == JavaInstructionsOpcodes.DLOAD ||
                (opcode >= JavaInstructionsOpcodes.LLOAD_0 &&
                opcode <= JavaInstructionsOpcodes.LLOAD_3) ||
                (opcode >= JavaInstructionsOpcodes.DLOAD_0 &&
                opcode <= JavaInstructionsOpcodes.DLOAD_3) ||
                opcode == JavaInstructionsOpcodes.DALOAD ||
                opcode == JavaInstructionsOpcodes.LALOAD ||
                opcode == JavaInstructionsOpcodes.LADD ||
                opcode == JavaInstructionsOpcodes.DADD ||
                opcode == JavaInstructionsOpcodes.LSUB ||
                opcode == JavaInstructionsOpcodes.DSUB ||
                opcode == JavaInstructionsOpcodes.LMUL ||
                opcode == JavaInstructionsOpcodes.DMUL ||
                opcode == JavaInstructionsOpcodes.LDIV ||
                opcode == JavaInstructionsOpcodes.DDIV ||
                opcode == JavaInstructionsOpcodes.LREM ||
                opcode == JavaInstructionsOpcodes.DREM ||
                opcode == JavaInstructionsOpcodes.LSHL ||
                opcode == JavaInstructionsOpcodes.LSHR ||
                opcode == JavaInstructionsOpcodes.LADD ||
                opcode == JavaInstructionsOpcodes.LXOR) {
            return false;
        } else if (opcode == JavaInstructionsOpcodes.GETFIELD) {
            //Todo not supported....
        }
        return true;
    }
}
