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

import java.util.Vector;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.factory.FactoryPlaceholder;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.optimizer.cpGlobalization.logic.*;
import takatuka.optimizer.bytecode.branchSetter.logic.factory.*;
import takatuka.optimizer.bytecode.changer.logic.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeBranchHandler extends StartMeGCP {

    private static BranchInstructionsHandler bIH = BranchInstructionsHandler.getInstanceOf();

    public static void printAllInstructionsDebug1(CodeAtt codeAtt) {

        Vector instrs = codeAtt.getInstructions();

        //Miscellaneous.println("\n\n------------------ New Code Att ------------ " +
        //       BHCodeAtt.codeAttCountDebug+
        //      "\n\n");
        for (int index = 0; index < instrs.size(); index++) {
            BHInstruction inst = (BHInstruction) instrs.elementAt(index);
            
            Miscellaneous.println("instruction ="+ inst);
        }

    }

    private static void printAllInstructionsDebug() {
        Vector<CodeAttCache> codeAttVec = Oracle.getInstanceOf().getAllCodeAtt();
        Miscellaneous.println("\n--------------------------------------------- \n");
        for (int loop = 0; loop < codeAttVec.size(); loop++) {
            printAllInstructionsDebug1((CodeAtt)codeAttVec.elementAt(loop).getAttribute());
        }

    }

    @Override
    public void execute(String args[]) throws Exception {
        super.execute(args);
    }

    @Override
    public void setFactoryFacade() {
        super.setFactoryFacade();
        FactoryPlaceholder.getInstanceOf().setFactory(FactoryFacadeBH.getInstanceOf());
    }

    public static void main(String args[]) throws Exception {
        new StartMeBranchHandler().start(args);
        StartMeBCC startMeBCC = new StartMeBCC();
        //printAllInstructionsDebug();
        //startMeBCC.setDefaults();
        //startMeBCC.myExecute(args);
//        printAllInstructionsDebug();
        bIH.restoreBranchInformation();
    //Miscellaneous.println("\n\n After restoring addresses");
    //printAllInstructionsDebug();
    }
}
