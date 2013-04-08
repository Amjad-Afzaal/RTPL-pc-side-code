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
package takatuka.optimizer.bytecode.changer.logic;

import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.logic.StartMeOGC;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeBCC extends StartMeOGC {

    public static boolean doOptimization = true;
    private static boolean endWithOptimization = false;
    private static final InputOptionsController engine = InputOptionsController.getInstanceOf();
    private final static StatsHolder statHolder = StatsHolder.getInstanceOf();

    public StartMeBCC() {
        super();
    }

    public static boolean isEndWithOptimization() {
        return endWithOptimization;
    }

    public static double totalReductionPercentage(double reduction) {
        double totalCodeSize = CodeAtt.getCodeTotalLengthInput();
        return ((reduction) / totalCodeSize) * 100.0;
    }

    @Override
    public void init(String args[]) throws Exception {
        String superArgs[] = null;
        StatsHolder.getInstanceOf().addStatArgs(args);
        engine.setDefaults();
        try {
            superArgs = engine.processArgs(args);
        } catch (Exception d) {
            engine.inputMessage();
            //Miscellaneous.printlnErr("************ ERROR Wrong input: " + d.getMessage());
            //d.printStackTrace();
            Miscellaneous.exit();
        }
        //do not forget this. However, at the end as third parameter was set above properly
        super.init(superArgs);
        System.arraycopy(superArgs, 0, args, 0, args.length);
    }

    @Override
    public void validateInput(String args[]) throws Exception {
        if (args.length < 2) {
            engine.inputMessage();
            Miscellaneous.exit();
        }
    }

    @Override
    public void execute(String args[]) throws Exception {
        try {
            statHolder.addStat(StatGroups.BYTECODE_OPTIMIZATION,
                    "Total size of all instructions ",
                    CodeAtt.getCodeTotalLengthInput());
            super.execute(args);

            if (!doOptimization) {
                return;
            }
            InputOptionsController.getInstanceOf().execute();
            endWithOptimization = true;
            Oracle.getInstanceOf().updateAllCodeAttsLength();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    public static void main(String args[]) throws Exception {
        (new StartMeBCC()).start(args);
    }
}
