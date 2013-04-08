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

import takatuka.optimizer.cpGlobalization.logic.util.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.VSS.logic.preCodeTravers.ResetBranches;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;

/**
 *
 * Description:
 * <p>
 *
 * The class generates code for functions having synchronized keyword in their
 * signatures.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenCodeForSyncInSignature {

    private static long startPCInstrId = -1;
    protected long endPCInstrId = -1;
    protected long handlerPCInstrId = -1;
    protected static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private ResetBranches resetBranches = null;

    /**
     * Generate code for methods having synchronized keyword in their signatures.
     * 1) Find all the method with synchronized keyword in their signatures.
     * 2) In case a function is native then throws Exception. 
     *    We do not support native synch functions. 
     * 3) Otherwise, for each synch function add code in it and add an exception in
     * the exception table of it too.
     */
    public void generateCode() {
        try {
            Oracle oracle = Oracle.getInstanceOf();
            Vector<MethodInfoCache> methodsVec = oracle.getAllMethodInfo();
            Iterator<MethodInfoCache> methodInfoCacheit = methodsVec.iterator();
            while (methodInfoCacheit.hasNext()) {
                MethodInfoCache mCache = methodInfoCacheit.next();
                MethodInfo method = mCache.getMethod();
                if (method.getAccessFlags().isSync()) {
                    multiplexer(method, mCache.getClassFile());
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void multiplexer(MethodInfo syncMethod, ClassFile methodClassFile) throws Exception {
        if (syncMethod.getAccessFlags().isNative()) {
            Miscellaneous.printlnErr("ERROR: TakaTuka does not support native synchronized methods.");
            Miscellaneous.exit();
        } else if (syncMethod.getAccessFlags().isStatic()
                && syncMethod.getCodeAtt().getInstructions().size() > 1) {
            new GenCodeForSynInSignStaticMethods().execute(syncMethod,
                    methodClassFile);
            return;
        }
        execute(syncMethod, methodClassFile, -1);
    }

    public void execute(MethodInfo syncMethod,
            ClassFile methodClassFile, int constantPoolFieldIndex) throws Exception {
        CodeAtt codeAtt = syncMethod.getCodeAtt();
        MethodInfo method = codeAtt.getMethod();
        /**
         * to make sure that monitorexit instruction
         * cannot be jumped over. 
         */
        resetBranches = new ResetBranches(method);
        int maxStack = codeAtt.getMaxStack().intValueUnsigned();
        codeAtt.setMaxStack(factory.createUn(maxStack + 1).trim(2));
        Vector<Instruction> instr = codeAtt.getInstructions();
        if (instr.size() == 1) {
            return;
        }
        insertMonitor(instr, JavaInstructionsOpcodes.ALOAD_0, factory.createUn(), codeAtt,
                constantPoolFieldIndex);
        codeAtt.setInstructions(instr);
        resetBranches.restore();
    }
    /*
     * start the function code by inserting following:
     *          aload_0 // this could be changed to getfield if the function is static
     *          dup
     *          astore x-1
     *          monitorenter
     * 
     * @param instrVec
     * @param codeAtt
     * @throws Exception 
     */

    protected void insertMonitorEnterInstrs(Vector<Instruction> instrVec,
            CodeAtt codeAtt, int constantPoolFieldIndex) throws Exception {
        int maxLocals = codeAtt.getMaxLocals().intValueSigned();
        startPCInstrId = ((BHInstruction) instrVec.elementAt(0)).getInstructionId();
        //instructions are inserted in reverse order
        //insert MONITORENTER
        Instruction instr = factory.createInstruction(JavaInstructionsOpcodes.MONITORENTER, factory.createUn(), codeAtt);
        instrVec.insertElementAt(instr, 0);
        //insert astore x-1
        instr = factory.createInstruction(JavaInstructionsOpcodes.ASTORE, factory.createUn(maxLocals - 2).trim(1), codeAtt);
        instrVec.insertElementAt(instr, 0);
        //insert dup
        instr = factory.createInstruction(JavaInstructionsOpcodes.DUP, factory.createUn(), codeAtt);
        instrVec.insertElementAt(instr, 0);
        if (constantPoolFieldIndex == -1) {
            //insert aload_0
            instr = factory.createInstruction(JavaInstructionsOpcodes.ALOAD_0, factory.createUn(), codeAtt);
            instrVec.insertElementAt(instr, 0);
        } else {
            //insert getstatic constantPoolFieldIndex
            instr = factory.createInstruction(JavaInstructionsOpcodes.GETSTATIC,
                    factory.createUn(constantPoolFieldIndex).trim(2), codeAtt);
            instrVec.insertElementAt(instr, 0);
        }
    }

    /*
     * Before each return insert following:
     *          aload x-1
     *          monitorexit
     * 
     */
    protected void insertMonitorExitBeforeReturn(Vector instrVec, int returnInstrIndex, CodeAtt codeAtt) throws Exception {
        int maxLocals = codeAtt.getMaxLocals().intValueSigned();
        BHInstruction retInstr = (BHInstruction) instrVec.elementAt(returnInstrIndex);
        //instructions are inserted in reverse order
        //insert MONITOREXIT
        Instruction instr = factory.createInstruction(JavaInstructionsOpcodes.MONITOREXIT, factory.createUn(), codeAtt);
        instrVec.insertElementAt(instr, returnInstrIndex);
        //insert aload x-1
        instr = factory.createInstruction(JavaInstructionsOpcodes.ALOAD, factory.createUn(maxLocals - 2).trim(1), codeAtt);
        instrVec.insertElementAt(instr, returnInstrIndex);

        /**
         * To make sure nothing can jump directly to return avoiding newly 
         * added monitorexit instructions.
         */
        resetBranches.addToRestore(retInstr, (BHInstruction) instr);
    }

    /**
     * Step 6 & 7:
     * 
     *  At the end of function add following:
     *          astore x
     *          aload x-1
     *          monitorexit
     *          aload x
     *          athrow
     * @param instrVec
     * @param codeAtt
     * @throws Exception 
     */
    protected void insertMonitorExitBlockAtTheEnd(Vector instrVec, CodeAtt codeAtt) throws Exception {
        int maxLocals = codeAtt.getMaxLocals().intValueSigned();

        //insert astore x
        Instruction instr = factory.createInstruction(JavaInstructionsOpcodes.ASTORE,
                factory.createUn(maxLocals - 1).trim(1), codeAtt);
        instrVec.insertElementAt(instr, instrVec.size());
        //this will be the handler pc instruction id
        handlerPCInstrId = instr.getInstructionId();

        //insert aload x-1
        instr = factory.createInstruction(JavaInstructionsOpcodes.ALOAD,
                factory.createUn(maxLocals - 2).trim(1), codeAtt);
        instrVec.insertElementAt(instr, instrVec.size());
        //insert Monitorexit
        instr = factory.createInstruction(JavaInstructionsOpcodes.MONITOREXIT,
                factory.createUn(), codeAtt);
        instrVec.insertElementAt(instr, instrVec.size());
        //insert aload x
        instr = factory.createInstruction(JavaInstructionsOpcodes.ALOAD,
                factory.createUn(maxLocals - 1).trim(1), codeAtt);
        instrVec.insertElementAt(instr, instrVec.size());
        //this is the end PC.
        endPCInstrId = instr.getInstructionId();
        //finally athrow instruction is added
        instr = factory.createInstruction(JavaInstructionsOpcodes.ATHROW,
                factory.createUn(), codeAtt);
        instrVec.insertElementAt(instr, instrVec.size());
    }

    /**
     * todo fix existing catch blocks missing.
     * 
     * The function should work as follows:
     * 
     * 1. First increment the number of local variables by 2. The
     *    last two newly added local variables will be type references and will 
     *    be used for maintaining monitors. Let x be the total number of local variables 
     *    after adding two new variables. The maxstack should be at least one.
     * 2.  start the function code by inserting following:
     *          aload 0 // this could be changed to getfield if the function is static
     *          dup
     *          astore x-1
     *          monitorenter
     * 
     * 3. Search for each return (i.e. areturn, ireturn, or any other kind of return)
     *    In case a catch block ends at return then now it should end at one 
     *    statement before return.
     * 4. Before each return insert following:
     *          aload x-1
     *          monitorexit
     * 5. In case a jump was targeted to a return then it should be now targetting to
     * the aload x-1 inserted above.
     * 6. At the end of function add following:
     *          astore x
     *          aload x-1
     *          monitorexit
     *          aload x
     *          athrow
     * 7. Add a new catch block:
     *      startpc: original first instruction of the function before  block added in #2.
     *      endpc: second last instruction of the block added in #6
     *      handler: first instruction of the block added in #6
     *      catch_type: 0000
     * 
     * @param instrVec
     * @param getObjectOpcode
     * @param getObjectOperand
     * @param codeAtt
     * @param constantPoolFieldIndex
     * @throws Exception 
     */
    protected void insertMonitor(Vector<Instruction> instrVec, int getObjectOpcode,
            Un getObjectOperand, CodeAtt codeAtt, int constantPoolFieldIndex) throws Exception {
        /**
         * step 1: increment local variables by 2
         */
        int maxLocals = codeAtt.getMaxLocals().intValueSigned();
        maxLocals = maxLocals + 2;
        codeAtt.setMaxLocals(factory.createUn(maxLocals).trim(2));
        /**
         * size of stack should be at least one. If it is already greater
         * than one then change nothing.
         */
        int maxStack = codeAtt.getMaxStack().intValueSigned();
        if (maxStack < 1) {
            codeAtt.setMaxStack(factory.createUn(1).trim(2));
        }

        /**
         * Step 2: 
         *  start the function code by inserting following:
         *          aload 0 // this could be changed to getfield if the function is static
         *          dup
         *          astore x-1
         *          monitorenter
         */
        insertMonitorEnterInstrs(instrVec, codeAtt, constantPoolFieldIndex);

        /**
         * Step 3 & 4 & 5: 
         * Before each return insert following:
         *          aload x-1
         *          monitorexit
         */
        for (int index = 0; index < instrVec.size(); index++) {
            Instruction instr = instrVec.elementAt(index);
            /**
             * If this is one of the return instruction
             */
            if (instr.getMnemonic().endsWith("RETURN")) {
                insertMonitorExitBeforeReturn(instrVec, index, codeAtt);
                // to make sure that same return is not encounter again.
                index = index + 2;
            }
        }
        /**
         * Step 6 & 7:
         * 
         *  At the end of function add following:
         *          astore x
         *          aload x-1
         *          monitorexit
         *          aload x
         *          athrow
         */
        insertMonitorExitBlockAtTheEnd(instrVec, codeAtt);
        codeAtt.addExceptionTable(startPCInstrId, endPCInstrId, handlerPCInstrId, 0);
    }
}
