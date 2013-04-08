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
package takatuka.verifier.logic;

import takatuka.optimizer.VSS.logic.preCodeTravers.*;
import takatuka.optimizer.bytecode.replacer.logic.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * The code of verifier is written keeping globalization of constant pool are prereq.
 * Hence it will not work without first constant pool globalization.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeVerifier extends StartMeBCIR {

    public static boolean shouldVerify = false;


    public StartMeVerifier() {
        super();
    }

    @Override
    public void setFactoryFacade() {
        super.setFactoryFacade();
        FactoryPlaceholder.getInstanceOf().setFactory(VerificationFactoryFacade.getInstanceOf());
        //default is good VerificationPlaceHolder.getInstanceOf().setFactory();
    }


    public void verificationPasses() throws Exception {
        if (!shouldVerify) {
            return;
        }
        LogHolder.getInstanceOf().addLog("Doing verification of class files (include pass 1,2,3, and 4).", true);
        //now its time for verification.
        Pass1.getInstanceOf().execute();
        Pass2.getInstanceOf().execute();
        Pass3.getInstanceOf().execute();
        Pass4.getInstanceOf().execute();
        postVerificationTasks();

    }
    @Override
    public void execute(String args[]) throws Exception {
        super.execute(args); //do first globalization
        //verificationPasses();
    }

    public static void main(String args[]) throws Exception {
        (new StartMeVerifier()).start(args);
    }

    /**
     * after verification we generate casting instructions and also produce some stats.
     */
    private void postVerificationTasks() {
        //StatsHolder.getInstanceOf().addStat("Stack_reduction", "% reduction", StartMeAbstract.roundDouble(DataFlowAnalyzer.percentageReductionInStack(), 2));
        //StatsHolder.getInstanceOf().addStat("Stack_reduction", "reduction in bytes",
        //        DataFlowAnalyzer.getTotalReductionInBytes());
        /*        LogHolder.getInstanceOf().addLog("aggregated % reduction in methods' operand stacks="+
        StartMeAbstract.roundDouble(DataFlowAnalyzer.percentageReductionInStack(), 2)+
        "% ("+DataFlowAnalyzer.getTotalReductionInBytes()+" bytes)", true);
         */
        //LogHolder.getInstanceOf().addLog("aggregated % reduction in methods' operand stacks=" +
        //        StartMeAbstract.roundDouble(DataFlowAnalyzer.percentageReductionInStack(), 2) +
        //        "%", true);
        GenerateCastingInstructions.getInstanceOf().generateCastingInstrs();
        //Miscellaneous.println("******************* " + CastInstructionController.getInstanceOf().getSize());
        Oracle.getInstanceOf().clearMethodCodeAttAndClassFileCache();
    }
}
