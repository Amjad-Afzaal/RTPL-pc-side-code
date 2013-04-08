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
import takatuka.classreader.logic.file.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.attribute.VerificationInstruction;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ExceptionHandler {

    private static final ExceptionHandler myObj = new ExceptionHandler();
    private static OperandStack stack = null;
    private static Vector<Long> nextPossibleInstructionsIds = null;
    private static MethodInfo currentMethod = null;
    private static int currentPC = -1;
    private static final String EXCEPTION_INSTR_PROPERTY = "runtimeExceptions.properties";
    private static final Properties exceptionInstrProperty =
            PropertyReader.getInstanceOf().loadProperties(EXCEPTION_INSTR_PROPERTY);
    /**
     * instruction which are already checked for all the possible exceptions
     */
    private HashSet<Long> alreadyHandledInstr = new HashSet<Long>();

    protected ExceptionHandler() {
    }

    public void addHandledInstr(long instrId) {
        this.alreadyHandledInstr.add(instrId);
    }

    public boolean alreadyHandledInstr(long instrId) {
        return this.alreadyHandledInstr.contains(instrId);
    }

    protected static void init(OperandStack stack_,
            Vector<Long> nextPossibleInstructionsIds_, MethodInfo currentMethod_,
            int currentPC_) {
        stack = stack_;
        nextPossibleInstructionsIds = nextPossibleInstructionsIds_;
        currentMethod = currentMethod_;
        currentPC = currentPC_;
    }

    public static void exceptionsThrownByAnInstruction(VerificationInstruction instr,
            MethodInfo currentMethod, ExceptionInfo exceptionRecord) {
        try {
            HashSet<ClassFile> cFileSet = exceptionCouldBeThrownByAnInstruction(instr);
            if (cFileSet.size() == 0) {
                return;
            }
            Iterator<ClassFile> it = cFileSet.iterator();
            Vector<Integer> thisClasses = new Vector<Integer>();
            while (it.hasNext()) {
                ClassFile cFile = it.next();
                thisClasses.add(cFile.getThisClass().intValueUnsigned());
            }
            Vector<Long> jumpTo = new Vector<Long>();
            athrowJumpPC(thisClasses, instr.getOffSet(), currentMethod, exceptionRecord);
        } catch (Exception d) {
            throw new VerifyErrorExt("" + d);
        }
    }

    /**
     *
     * @param instr
     * @return the exception thrown by
     */
    private static HashSet<ClassFile> exceptionCouldBeThrownByAnInstruction(VerificationInstruction instr) {
        HashSet<ClassFile> ret = new HashSet<ClassFile>();
        Oracle oracle = Oracle.getInstanceOf();
        String mnemonic = instr.getMnemonic();
        String classesNamesWithCommas = exceptionInstrProperty.getProperty(mnemonic.toLowerCase());
        if (classesNamesWithCommas == null) {
            return ret;
        }
        String classes[] = classesNamesWithCommas.split(",");
        for (int loop = 0; loop < classes.length; loop++) {
            String className = classes[loop];
            ClassFile cFile = oracle.getClass(className);
            if (cFile != null) {
                ret.add(cFile);
            }
        }
        return ret;
    }

    public static ExceptionHandler getInstanceOf() {
        /**
         * assuming init is already called.
         * One must throw an exception here if init is not already called.
         */
        return myObj;
    }

    public static ExceptionHandler getInstanceOf(OperandStack stack,
            Vector<Long> nextPossibleInstructionsIds, MethodInfo currentMethod,
            int currentPC) {
        init(stack, nextPossibleInstructionsIds, currentMethod, currentPC);
        return myObj;
    }

    /**
     * Go to each bytecode of the method see what exception is generated by it.
     * @param method
     * @return
     */
    public HashMap<VerificationInstruction, Vector<Long>> exceptionToBeThrownByAMethod(MethodInfo method) {
        Vector instrVec = method.getInstructions();
        HashMap<VerificationInstruction, Vector<Long>> ret = new HashMap<VerificationInstruction, Vector<Long>>();
        if (instrVec == null || instrVec.size() == 0) {
            return ret;
        }
        Iterator<VerificationInstruction> it = instrVec.iterator();
        while (it.hasNext()) {
            VerificationInstruction instr = it.next();
            //exceptionsThrownByAnInstruction(instr, method, ret);
        }
        return ret;
    }

    /**
     * Throw exception or error
     * ..., ref ==> [empty], ref
     *
     */
    protected final void athrowInstruction(VerificationInstruction currentInstr) {
        Type ref = stack.pop();
        if (!ref.isReference()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID);
        }
        stack.clear();
        stack.push(ref);
        try {
            nextPossibleInstructionsIds.clear();
            Vector<Integer> inputCatchTypes = new Vector(ref.getRefClassThisPtr());
            ExceptionInfo excepTionInfo = new ExceptionInfo(currentInstr);
            athrowJumpPC(inputCatchTypes, currentPC, currentMethod, excepTionInfo);
            nextPossibleInstructionsIds = excepTionInfo.getTargetCatchInstrIDs();
        } catch (Exception d) {
            throw new VerifyErrorExt("");
        }
    }

    /**
     * As per http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc.html
     * 0) clear the nextPossibleInstructons.
     * 1) Get the exception table of the method.
     * 2) Go through each exception table entry one by one.
     * 3) Stop at the first exception table entry that handles the objectRef.
     * 4) Check if it is currentPC is within the startPC and endPC of that exception-entry.
     *    If so then jump to step 6.
     * 5) Go back to step 3 to find next exception-table entry handling the objectRef.
     *    If all entries are checked then go to step 6.
     * 6) If step 4 has returned an exception-table entry handling the current exception.
     *    Then add exception handler of that entry in nextPossibleInstructionsIds.
     *
     * @param inputCatchTypes
     * @param currentPC
     * @param currentMethod
     * @param nextPossibleInstructionsIds
     * @throws Exception
     */
    private static void athrowJumpPC(Vector<Integer> inputCatchTypes, int currentPC,
            MethodInfo currentMethod, ExceptionInfo exceptInfo) throws Exception {
        Vector<Long> targetCatchIDs = exceptInfo.getTargetCatchInstrIDs();
        Vector<ExceptionTableEntry> exceptTableEntry = currentMethod.getCodeAtt().getExceptions();
        Oracle oracle = Oracle.getInstanceOf();
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        Vector methodInstr = currentMethod.getInstructions();
        for (int catchLoop = 0; catchLoop < inputCatchTypes.size(); catchLoop++) {
            for (int loop = 0; loop < exceptTableEntry.size(); loop++) {
                ExceptionTableEntry expTbl = exceptTableEntry.elementAt(loop);
                int entryCatchType = expTbl.getCatchType().intValueUnsigned();
                ClassFile entryCatchClass = oracle.getClass(entryCatchType, pOne);
                int startPC = MethodInfo.findInstruction(expTbl.getStartPCInstrId(), methodInstr).getOffSet();
                int endPC = MethodInfo.findInstruction(expTbl.getEndPCInstrId(), methodInstr).getOffSet();
                int catchType = inputCatchTypes.get(catchLoop);
                ClassFile catchClass = oracle.getClass(catchType, pOne);
                boolean rightCatchType = (catchType == entryCatchType
                        || entryCatchType == 0 /*ANY: Handles all types */
                        || (catchClass != null && entryCatchClass != null
                        && oracle.isSubClass(catchClass, entryCatchClass)));
                /**
                 * According to documentation start PC is inclusive and end PC is
                 * exclusive.
                 */
                if (rightCatchType && currentPC >= startPC && currentPC < endPC) {
                    targetCatchIDs.add(expTbl.getHandlerPCInstrId());
                    exceptInfo.getExceptionClassesThisPointers().add(catchType);
                    exceptInfo.getExceptionTableEntriedUsed().add(loop);
                    break;
                }
            }
        }

    }
}
