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
package takatuka.optimizer.VSS.logic;

import takatuka.optimizer.VSS.logic.preCodeTravers.*;
import takatuka.classreader.logic.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.logic.*;
import takatuka.optimizer.VSS.logic.factory.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * The VSS (Variable Slot Size) optimization reduces the size of
 * a "slot" of local variable and operand stack.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeVSS extends StartMeVerifier {

    private double totalSizeofStacks = 0;
    private double totalSizeofLV = 0;
    private static final String VSS_GROUP = "Variable-Slot-Size(VSS)";
    private static final String REDUC_IN_BYTES_OPERAND_STACK = "Reduction in Operand Stack Size (Bytes)";
    private static final String REDUC_IN_BYTES_LOCAL_VARIABLES = "Reduction in Local Variables Size (Bytes)";
    private static final String PERCENT_REDUCTION_IN_OPERAND_STACK = "Percentage reduction in Operand Stack";
    private static final String PERCENT_REDUCTION_IN_LOCAL_VARIABLES = "Percentage reduction in Local Variables";
    public static boolean doneWithVSS = false;
    
    @Override
    public void setFactoryFacade() {
        super.setFactoryFacade();
        VerificationPlaceHolder.getInstanceOf().setFactory(SSFrameFactory.getInstanceOf());
    }

    @Override
    public void execute(String args[]) throws Exception {
        super.execute(args);
        /**
         * Calculate existing size of Local variable and operand stack.
         */
        preReductiondata();
        ReduceTheSizeOfLocalVariables.getInstanceOf().reduceSizeWithoutCodeVirtualExecution();
        VerificationFrameFactory verFacOriginal = VerificationPlaceHolder.getInstanceOf().getFactory();
        VerificationPlaceHolder.getInstanceOf().setFactory(SSFrameFactoryForSize.getInstanceOf());
        if (ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES < 4) {
            /**
             * Todo following code has some error. The error comes when reduceSizeWithoutCodeVirtualExecution
             * reduce size of a function and the for the same function the reduceSizeWITHCodeVirtualExecution
             * is applied.
             * The following code will be uncommented after fixing the error at some later stage.
             */
            //ReductionBasedOnBCTravers.getInstanceOf().reduceSizeWITHCodeVirtualExecution();
        }
        DataFlowAnalyzer.clearAllMethodFrames();
        VerificationPlaceHolder.getInstanceOf().setFactory(verFacOriginal);
        super.verificationPasses();
        preReductionStats();
        //BytecodeProcessor.refreshOpcodeUsedOriginally();
        DataFlowAnalyzer.printMaxStackInfoInAFile();
        doneWithVSS = true;
    }

    private void preReductionStats() {
        Oracle oracle = Oracle.getInstanceOf();
        double reduceLVSize = oracle.getTotalLVSizeOfAllMethods()
                * ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES;
        double reduceStackSize = oracle.getTotalStackSizeOfAllMethods()
                * ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES;
        double reductionInBytesLV = totalSizeofLV - reduceLVSize;
        double reductionInBytesStack = totalSizeofStacks - reduceStackSize;
        double percReductionInLVSize = ((reductionInBytesLV) / totalSizeofLV) * 100.0;
        double percReductionInStackSize = ((reductionInBytesStack) / totalSizeofStacks) * 100.0;
        if (percReductionInLVSize != 0 && percReductionInStackSize != 0) {
            LogHolder.getInstanceOf().addLog("VSS: Total % reduction in Local Variables ="
                    + StartMeAbstract.roundDouble(percReductionInLVSize, 2)
                    + "% or " + reductionInBytesLV + " bytes.", true);
            LogHolder.getInstanceOf().addLog("VSS: Total % reduction in Operand Stack ="
                    + StartMeAbstract.roundDouble(percReductionInStackSize, 2)
                    + "% or " + reductionInBytesStack + " bytes.", true);
            StatsHolder.getInstanceOf().addStat(VSS_GROUP, PERCENT_REDUCTION_IN_LOCAL_VARIABLES,
                    StartMeAbstract.roundDouble(percReductionInLVSize, 2)+"%");
            StatsHolder.getInstanceOf().addStat(VSS_GROUP, REDUC_IN_BYTES_LOCAL_VARIABLES,
                    totalSizeofLV - reduceLVSize);
            StatsHolder.getInstanceOf().addStat(VSS_GROUP, PERCENT_REDUCTION_IN_OPERAND_STACK,
                    StartMeAbstract.roundDouble(percReductionInStackSize, 2)+"%");
            StatsHolder.getInstanceOf().addStat(VSS_GROUP, REDUC_IN_BYTES_OPERAND_STACK,
                    totalSizeofStacks - reduceStackSize);
        }
    }

    private void preReductiondata() {
        Oracle oracle = Oracle.getInstanceOf();
        totalSizeofLV = oracle.getTotalLVSizeOfAllMethods() * 4;
        totalSizeofStacks = oracle.getTotalStackSizeOfAllMethods() * 4;
    }

    public static void main(String args[]) throws Exception {
        (new StartMeVSS()).start(args);
    }
}
