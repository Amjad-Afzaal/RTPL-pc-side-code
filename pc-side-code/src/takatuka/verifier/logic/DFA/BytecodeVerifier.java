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
import takatuka.classreader.dataObjs.attribute.BytecodeProcessor;
import takatuka.classreader.dataObjs.attribute.Instruction;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.FactoryFacade;
import takatuka.classreader.logic.factory.FactoryPlaceholder;
import takatuka.optimizer.VSS.logic.preCodeTravers.ChangeCodeForReducedSizedLV;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * This class is called once per method verification.
 *
 * This class process an instruction stack and localVariables as described below.
 *
 * 2. Model the effect of the instruction on the operand stack and local variable array by doing the following:
 * (A) If the instruction uses values from the operand stack, ensure that there are a sufficient number of values
on the stack and that the top values on the stack are of an appropriate type. Otherwise, verification fails.
 * (B) If the instruction uses a local variable, ensure that the specified local variable contains a value of the
appropriate type. Otherwise, verification fails.
 * (C) If the instruction pushes values onto the operand stack, ensure that there is sufficient room on the operand
stack for the new values. Add the indicated types to the top of the modeled operand stack.
 * (D) If the instruction modifies a local variable, record that the local variable now contains the new type.
 *
 * TERMINOLOGY: I will use following terminology in function descriptions.
 * A, B, .. ==> C, D means an instruction pop A, B and push C,D. Here A, B will be types.
 * VGet(i, j, ...) means that an instruction get from local variables at location i, j
 * VSet(X (i),Y (j), ...) meaans that an instruction set local variables at index i, j to types X, Y respectively
 *
 * Types are defined in class Type. In case of reference the classInfo index is used, with a
 * special bit telling that it is a reference (so that we do not mix reference with other normal types)
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class BytecodeVerifier {

    //protected Frame currentFrame = null;
    private int currentPC = 0;
    private Instruction currentInstr = null;
    private HashSet<Type> returnTypes = new HashSet<Type>();
    private InvokeInstrs invokeInstrVerifier = null;
    private MathInstrs mathInstrsVerifier = null;
    private LoadAndStoreInstrs loadAndStoreInstrVerifier = null;
    private IfAndCmpInstrs ifAndCmpOpcodeVerifier = null;
    private ObjCreatorInstrs objCreatorInstVerifier = null;
    private static FieldInstrs fieldInstrsVerifier = null;
    private PureStackInstrs stackRelatedInstr = null;
    private MiscInstrs miscInstr = null;
    private ExceptionHandler exceptHandler = null;
    private Vector<Long> nextPossibleInstructionsIds = new Vector<Long>();
    private MethodInfo currentMethod = null;
    private Frame frame = null;
    private Vector methodCallingParameters = null;

    /**
     * 
     * @param frame
     * @param currentMethod
     * @param methodCallingParamters
     */
    public BytecodeVerifier(Frame frame, MethodInfo currentMethod, Vector methodCallingParamters) {
        this.frame = frame;
        this.currentMethod = currentMethod;
        this.methodCallingParameters = methodCallingParamters;
    }

    /**
     * returns the current program counter.
     * @return 
     */
    public int getCurrentPC() {
        return currentPC;
    }

    /**
     * Return the current instruction under verification.
     */
    public Instruction getCurrentInstr() {
        return currentInstr;
    }

    /**
     * 
     * @return
     */
    public HashSet<Type> getReturnType() {
        return returnTypes;
    }

    /**
     * 
     * @param type
     */
    public void addReturnType(Type type) {
        returnTypes.add(type);
    }

    /**
     *
     * @return
     */
    public MethodInfo getMethodInfo() {
        return currentMethod;
    }

    /**
     *
     * @return
     */
    public Frame getFrame() {
        return frame;
    }

    /**
     * 
     * @return
     */
    public Vector getMethodCallingParameters() {
        return methodCallingParameters;
    }

    public void initilizeHelperClasses() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        OperandStack stack = frame.getOperandStack();
        LocalVariables localVar = frame.getLocalVariables();
        loadAndStoreInstrVerifier = frameFactory.createLoadAndStoreInstrsInterpreter(frame,
                currentMethod, methodCallingParameters);
        invokeInstrVerifier = frameFactory.createInvokeInstrsInterpreter(frame,
                currentMethod, methodCallingParameters, nextPossibleInstructionsIds);
        mathInstrsVerifier = frameFactory.createMathInstrsInterpreter(stack,
                currentMethod, currentPC);
        loadAndStoreInstrVerifier = frameFactory.createLoadAndStoreInstrsInterpreter(frame,
                currentMethod, methodCallingParameters);
        ifAndCmpOpcodeVerifier = frameFactory.createIfAndCmpInstrsInterpreter(nextPossibleInstructionsIds,
                stack, currentMethod);
        objCreatorInstVerifier = frameFactory.createObjCreatorInstrsInterpreter(stack,
                currentMethod);
        fieldInstrsVerifier = frameFactory.createFieldInstrsInterpreter(stack,
                currentMethod, invokeInstrVerifier);
        stackRelatedInstr = frameFactory.createPureStackInstrsInterpreter(stack, currentMethod);
        miscInstr = frameFactory.createMiscInstrsInterpreter(nextPossibleInstructionsIds,
                stack, currentMethod, localVar, currentPC, returnTypes);
        exceptHandler = frameFactory.createExceptionHandler(stack,
                nextPossibleInstructionsIds, currentMethod, currentPC);
    }

    /**
     * 
     * @param inst
     * @param currentPC
     * @param parentFunctionStack
     * @return
     * @throws Exception
     */
    public Vector execute(VerificationInstruction inst,
            int currentPC, OperandStack parentFunctionStack) throws Exception {
        this.currentPC = currentPC;
        this.currentInstr = inst;
        nextPossibleInstructionsIds.clear();
        /**
         * -1 indicates that next instruction will be the subsequent instruction.
         * In case of JUMP instructions this element (-1) is removed.
         */
        nextPossibleInstructionsIds.addElement((long) -1);
        initilizeHelperClasses();
        int opcode = inst.getOpCode();

        //if (opcode == JavaInstructionsOpcodes.ALOAD_BLOCK_SIZE_2) {
        //Miscellaneous.println("Stop here");
        //}
        if (opcode == JavaInstructionsOpcodes.NOP) {
            //do nothing
        } else if (opcode >= JavaInstructionsOpcodes.ACONST_NULL
                && opcode <= JavaInstructionsOpcodes.DCONST_1) {
            miscInstr.constExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.BIPUSH
                || opcode == JavaInstructionsOpcodes.SIPUSH) {
            stackRelatedInstr.pushExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.LDC
                && opcode <= JavaInstructionsOpcodes.LDC2_W
                || opcode == JavaInstructionsOpcodes.LDC_FLOAT
                || opcode == JavaInstructionsOpcodes.LDC_INT
                || opcode == JavaInstructionsOpcodes.LDC_W_FLOAT
                || opcode == JavaInstructionsOpcodes.LDC_W_INT
                || opcode == JavaInstructionsOpcodes.LDC2_W_LONG) {
            objCreatorInstVerifier.ldcExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.LOAD_INT_FLOAT
                || opcode == JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN
                || opcode == JavaInstructionsOpcodes.LOAD_LONG_DOUBLE
                || opcode == JavaInstructionsOpcodes.LOAD_REFERENCE
                || opcode == JavaInstructionsOpcodes.LOAD_SHORT_CHAR) {
            loadAndStoreInstrVerifier.loadVarExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.IALOAD
                && opcode <= JavaInstructionsOpcodes.SALOAD) {
            loadAndStoreInstrVerifier.loadArrayExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.STORE_INT_FLOAT
                || opcode == JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN
                || opcode == JavaInstructionsOpcodes.STORE_LONG_DOUBLE
                || opcode == JavaInstructionsOpcodes.STORE_REFERENCE
                || opcode == JavaInstructionsOpcodes.STORE_SHORT_CHAR) {
            loadAndStoreInstrVerifier.storeVarExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.IASTORE
                && opcode <= JavaInstructionsOpcodes.SASTORE) {
            loadAndStoreInstrVerifier.storeArrayExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.POP
                || opcode == JavaInstructionsOpcodes.POP2) {
            stackRelatedInstr.popExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.DUP
                && opcode <= JavaInstructionsOpcodes.DUP2_X2) {
            stackRelatedInstr.dupExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.SWAP) {
            stackRelatedInstr.swapInstruction();
        } else if ((opcode >= JavaInstructionsOpcodes.IADD
                && opcode <= JavaInstructionsOpcodes.DREM)
                || (opcode >= JavaInstructionsOpcodes.ISHL
                && opcode <= JavaInstructionsOpcodes.LXOR)) {
            mathInstrsVerifier.mathExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.INEG
                && opcode <= JavaInstructionsOpcodes.DNEG) {
            miscInstr.negExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.IINC) {
            miscInstr.iincInstruction(inst, false);
        } else if (opcode >= JavaInstructionsOpcodes.I2L
                && opcode <= JavaInstructionsOpcodes.I2S) {
            miscInstr.A2BExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.LCMP
                && opcode <= JavaInstructionsOpcodes.DCMPG) {
            ifAndCmpOpcodeVerifier.cmpExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.LCMP
                && opcode <= JavaInstructionsOpcodes.DCMPG) {
            ifAndCmpOpcodeVerifier.cmpExecute(inst);
        } else if ((opcode >= JavaInstructionsOpcodes.IFEQ
                && opcode <= JavaInstructionsOpcodes.IFLE)
                || opcode == JavaInstructionsOpcodes.IFNULL
                || opcode == JavaInstructionsOpcodes.IFNONNULL) {
            ifAndCmpOpcodeVerifier.ifExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.IF_ICMPEQ
                && opcode <= JavaInstructionsOpcodes.IF_ACMPNE) {
            ifAndCmpOpcodeVerifier.ifCmdExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.GOTO
                || opcode == JavaInstructionsOpcodes.GOTO_W
                || opcode == JavaInstructionsOpcodes.JSR
                || opcode == JavaInstructionsOpcodes.JSR_W) {
            ifAndCmpOpcodeVerifier.jumpExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.RET) {
            miscInstr.retInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.TABLESWITCH) {
            ifAndCmpOpcodeVerifier.switchInstruction(inst, false);
        } else if (opcode == JavaInstructionsOpcodes.LOOKUPSWITCH) {
            ifAndCmpOpcodeVerifier.switchInstruction(inst, true);
        } else if (opcode >= JavaInstructionsOpcodes.IRETURN
                && opcode <= JavaInstructionsOpcodes.RETURN) {
            miscInstr.returnExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.GETSTATIC
                || opcode == JavaInstructionsOpcodes.GETFIELD) {
            fieldInstrsVerifier.fieldGetExecute(inst);
        } else if (opcode == JavaInstructionsOpcodes.PUTFIELD
                || opcode == JavaInstructionsOpcodes.PUTSTATIC) {
            fieldInstrsVerifier.fieldPutExecute(inst);
        } else if (opcode >= JavaInstructionsOpcodes.INVOKEVIRTUAL
                && opcode <= JavaInstructionsOpcodes.INVOKEINTERFACE) {
            invokeInstrVerifier.invokeInstruction(inst, opcode);
        } else if (opcode == JavaInstructionsOpcodes.NEW) {
            objCreatorInstVerifier.newInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.NEWARRAY) {
            objCreatorInstVerifier.newArrayInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.ANEWARRAY) {
            objCreatorInstVerifier.anewarrayInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.ARRAYLENGTH) {
            miscInstr.arraylengthInstruction();
        } else if (opcode == JavaInstructionsOpcodes.ATHROW) {
            exceptHandler.athrowInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.CHECKCAST) {
            miscInstr.checkCastInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.INSTANCEOF) {
            miscInstr.instanceofInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.MONITORENTER
                || opcode == JavaInstructionsOpcodes.MONITOREXIT) {
            miscInstr.monitorInstruction();
        } else if (opcode == JavaInstructionsOpcodes.MONITORENTER) {
            wideInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.MULTIANEWARRAY) {
            objCreatorInstVerifier.multianewarrayInstruction(inst);
        } else if (opcode == JavaInstructionsOpcodes.CAST_METHOD_STACK_LOCATION
                || opcode == JavaInstructionsOpcodes.CAST_STACK_LOCATION) {
            //do nothing for the time being.
        } else if (opcode == JavaInstructionsOpcodes.WIDE) {
            wideInstruction(inst);
        } else {
            throw new VerifyErrorExt(Messages.INVALID_BYTECODE + inst);
        }
//        for (int loop = 0; loop < nextPossibleInstructionsIds.size(); loop ++)
//            DataFlowAnalyzer.Debug_print(" /////////////// "+nextPossibleInstructionsIds.elementAt(loop));
        return nextPossibleInstructionsIds;
    }

        private void newOperandCreator(Un operand) throws Exception {
            
        Un opcodeWithinUn = Un.cutBytes(1, operand);
        
        int opcodeWithin = opcodeWithinUn.intValueUnsigned();
        if (opcodeWithin == JavaInstructionsOpcodes.ILOAD || opcodeWithin == JavaInstructionsOpcodes.FLOAD) {
            opcodeWithin = JavaInstructionsOpcodes.LOAD_INT_FLOAT;
        }  else if (opcodeWithin == JavaInstructionsOpcodes.LLOAD || opcodeWithin == JavaInstructionsOpcodes.DLOAD) {
            opcodeWithin = JavaInstructionsOpcodes.LOAD_LONG_DOUBLE;
        } else if (opcodeWithin == JavaInstructionsOpcodes.ISTORE || opcodeWithin == JavaInstructionsOpcodes.FSTORE) {
            opcodeWithin = JavaInstructionsOpcodes.STORE_INT_FLOAT;
        }  else if (opcodeWithin == JavaInstructionsOpcodes.LSTORE || opcodeWithin == JavaInstructionsOpcodes.DSTORE) {
            opcodeWithin = JavaInstructionsOpcodes.STORE_LONG_DOUBLE;
        }  else if (opcodeWithin == JavaInstructionsOpcodes.ASTORE) {
            opcodeWithin = JavaInstructionsOpcodes.STORE_REFERENCE;
        } else if (opcodeWithin == JavaInstructionsOpcodes.ALOAD) {
            opcodeWithin = JavaInstructionsOpcodes.LOAD_REFERENCE;
        }
        opcodeWithinUn = new Un(opcodeWithin).trim(1);
        opcodeWithinUn.conCat(operand);
        operand.setData(opcodeWithinUn.getData());
    }
        
    protected void wideInstruction(VerificationInstruction inst) throws Exception {
            newOperandCreator(inst.getOperandsData());
            Un operand = (Un) inst.getOperandsData().clone();
            int nextOpcode = Un.cutBytes(1, operand).intValueUnsigned();
            BytecodeProcessor.removeFromFixUnUsedInstruction(nextOpcode);
            if (nextOpcode != JavaInstructionsOpcodes.IINC) {
               FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
           
               VerificationInstruction newInstr = (VerificationInstruction) factory.createInstruction(nextOpcode, operand, null);
               if (newInstr.getMnemonic().contains("LOAD")) {
                   loadAndStoreInstrVerifier.loadVarExecute(newInstr);
               } else if (newInstr.getMnemonic().contains("STORE")) {
                   
                   loadAndStoreInstrVerifier.storeVarExecute(newInstr);
               }  else if (newInstr.getMnemonic().contains("RET")) {
                   miscInstr.retInstruction(newInstr);
               }
            }  else {
                 BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.WIDE_IINC);
                /*0x0001012c2c Vs 0x840001012c*/ ChangeCodeForReducedSizedLV.changeInstruction(inst, 
                        JavaInstructionsOpcodes.WIDE_IINC, 
                        inst.getOperandsData());
                //inst.setOpCode(JavaInstructionsOpcodes.WIDE_IINC);
                Oracle.getInstanceOf().clearMethodCodeAttAndClassFileCache();
            }
               
    }
    /**
     * OperandStack behave same as the original instruction.
     
    protected void wideInstruction(VerificationInstruction inst) throws Exception {
        //todo should not make new instruction everytime.
        VerificationInstruction newInst = inst.getWideUnderlineInstruction();
        if (newInst.getOpCode() == JavaInstructionsOpcodes.IINC) {
            miscInstr.iincInstruction(newInst, true);
        } else {
            execute(newInst, currentPC, null); //should handle wideness automatically.
        }

    }*/
}
