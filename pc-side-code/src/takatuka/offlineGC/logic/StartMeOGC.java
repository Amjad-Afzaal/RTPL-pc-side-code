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
package takatuka.offlineGC.logic;

import takatuka.offlineGC.OGI.FTT.FTTAlgoBase;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.offlineGC.DFA.logic.partialInstrOrder.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.offlineGC.OGI.DAU.*;
import takatuka.offlineGC.OGI.PNR.*;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.offlineGC.DFA.logic.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.logic.DFA.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.offlineGC.generateInstrs.*;
import takatuka.optimizer.deadCodeRemoval.logic.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.offlineGC.OGI.threads.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.factory.*;
import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.VSS.logic.*;
import takatuka.verifier.dataObjs.*;
import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;

/**
 * 
 * Description:
 * <p>
 * This class is the starting point for the offline GC.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeOGC extends StartMeVSS {

    public static boolean useOffLineGC = false;
    public static boolean useOGDFA = false;
    public static boolean insertFreeMemoryInstrForOffLineGCResults = false;
    private static final String MAIN_METHOD_NAME = "main";
    private static final String MAIN_METHOD_DESC = "([Ljava/lang/String;)V";
    public static final int PNR = 1;
    public static final int FTT = 2;
    public static final int DAU = 3;
    public static final int SELECT_ALL_ALGO = -1;
    private static boolean selectAllAlgo = true;
    private static boolean selectInterMethodDAGAlgo = false;
    private static boolean selectLocalVariableAlgo = false;
    private static boolean selectInsideLoopAlgo = false;

    @Override
    public void setFactoryFacade() {
        super.setFactoryFacade();
        FactoryPlaceholder.getInstanceOf().setFactory(GCFactoryFacade.getInstanceOf());
        //VerificationPlaceHolder.getInstanceOf().setFactory(GCFrameFactory.getInstanceOf());
    }

    public static void setGCAlgorithmSelector(int selector) {
        if (selector != PNR
                && selector != FTT
                && selector != DAU) {
            Miscellaneous.printlnErr("invalid GC algorithm selected ");
            Miscellaneous.exit();
        }
        if (selector != SELECT_ALL_ALGO) {
            selectAllAlgo = false;
        }
        if (selector == DAU) {
            selectInsideLoopAlgo = true;
        } else if (selector == PNR) {
            selectInterMethodDAGAlgo = true;
        } else if (selector == FTT) {
            selectLocalVariableAlgo = true;
        }
    }

    @Override
    public void execute(String args[]) throws Exception {
        super.execute(args);
        if (useOffLineGC || useOGDFA) {
            LogHolder.getInstanceOf().addLog("Starting offLine GC. "
                    + "NOTE IT COULD TAKE SOME TIME ...", true);
            //DataFlowAnalyzer.shouldDebugPrint = true;
            LogHolder.getInstanceOf().addLog("OGC-DFA is started ....");
            long time = System.currentTimeMillis();
            
            /**
             * This basically executes OGCDFA
             */
            executeOGCDFA();

            /**
             * Create super instruction for performance
             */
            CreateIntraMethodSuperInstrs.getInstanceOf().execute();

            /**
             * We start by finding references that cannot be free due to threads.
             */
            FindRefCannotBeFreedDueToThreads.getInstanceOf().execute();

            /**
             * There are three algorithms and by default all of them are used however
             * a user can select one of them or two if he wants.
             */
            time = System.currentTimeMillis() - time;
            LogHolder.getInstanceOf().addLog("Time taken by OGC-DFA =" + time, true);
            if (useOffLineGC) {
                int totalFreeInstrs = 0;
                if (selectAllAlgo || selectInterMethodDAGAlgo) {
                    //LogHolder.getInstanceOf().addLog("Running inter-method Offline GC algorithm", true);

                    LogHolder.getInstanceOf().addLogHeading("Running inter-method GC algorithm", false);
                    LogHolder.getInstanceOf().addLog("PNR is statrted ...", true);
                    totalFreeInstrs = 0;
                    time = System.currentTimeMillis();
                    //IntraMethodAlgorithm.shouldDebugPrint = true;
                    PNRBase.getInstanceOf().execute();
                    time = System.currentTimeMillis() - time;
                    totalFreeInstrs = GCInstruction.numberOfRefFreeByAlgo(GCInstruction.PNR);
                    LogHolder.getInstanceOf().addLog("Time taken by PNR =" + time + ", "
                            + "# of free instruction " + totalFreeInstrs, true);
                }
                if (selectAllAlgo || selectLocalVariableAlgo) {
                    //LogHolder.getInstanceOf().addLog("Running method exit Offline GC algorithm", true);
                    LogHolder.getInstanceOf().addLogHeading("Running method exit GC algorithm", false);
                    LogHolder.getInstanceOf().addLog("FTT is started ...", true);
                    totalFreeInstrs = 0;
                    time = System.currentTimeMillis();
                    FTTAlgoBase.getInstanceOf().execute();
                    time = System.currentTimeMillis() - time;
                    totalFreeInstrs = GCInstruction.numberOfRefFreeByAlgo(GCInstruction.FTT)
                            + GCInstruction.numberOfRefFreeByAlgo(GCInstruction.FTT_PNR);
                    LogHolder.getInstanceOf().addLog("Time taken by FTT =" + time
                            + ", # of free instructions=" + totalFreeInstrs, true);
                }
                if (selectAllAlgo || selectInsideLoopAlgo) {
                    //LogHolder.getInstanceOf().addLog("Running inside Loop Offline GC algorithm", true);

                    LogHolder.getInstanceOf().addLogHeading("Running inside Loop Offline GC algorithm", false);
                    LogHolder.getInstanceOf().addLog("DAU is started ... ", true);
                    totalFreeInstrs = 0;
                    time = System.currentTimeMillis();
                    DAUAlgo.getInstanceOf().execute();
                    time = System.currentTimeMillis() - time;
                    totalFreeInstrs = GCInstruction.numberOfRefFreeByAlgo(GCInstruction.DAU)
                            + GCInstruction.numberOfRefFreeByAlgo(GCInstruction.DAU_PNR);
                    LogHolder.getInstanceOf().addLog("Time taken by DAU =" + time
                            + ", # of free instructions=" + totalFreeInstrs, true);
                }
            }
        }
        if (insertFreeMemoryInstrForOffLineGCResults) {
            System.out.println("\n\n\n adding instructions for offLineGC results. ");
            GenerateInstrForResults.getInstanceOf().execute();
        }
        if (useOffLineGC) {
            /**
             * The following inserts offline GC instructions in the bytecode.
             */
            LogHolder.getInstanceOf().addLog("Generating OGC instructions ... ", true);
            long time = System.currentTimeMillis();
            GenerateInstrsForOfflineGC.getInstanceOf().execute();
            time = System.currentTimeMillis() - time;
            LogHolder.getInstanceOf().addLog("Time taken by OGC instruction generation =" + time, true);
            int newIdGenerated = GenerateInstrsForOfflineGC.getInstanceOf().getTotalNewIdGenerated();
            LogHolder.getInstanceOf().addLog("Number Of New GC Instr Gernated ="
                    + newIdGenerated,
                    LogRecord.LOG_FILE_FOR_OFFLINE_GC, true);
            LogHolder.getInstanceOf().addLog("Number Of Free GC Instr Gernated ="
                    + GenerateInstrsForOfflineGC.getInstanceOf().getNumberOfFreeInstrGenerated(),
                    LogRecord.LOG_FILE_FOR_OFFLINE_GC, true);

        }

    }

    private void executeThread(VirtualThread vThread) throws Exception {
        Oracle oracle = Oracle.getInstanceOf();
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        MethodInfo method = vThread.getStartingMethod();
        if (method.getInstructions().size() == 0) {
            return;
        }
        //Miscellaneous.println("going to run new thread ="+vThread);
        DataFlowAnalyzer.Debug_print("going to run new thread =", vThread);
        ClassFile.currentClassToWorkOn = method.getClassFile();
        int maxLocalSize = method.getCodeAtt().getMaxLocals().intValueUnsigned();
        String methodDesc = oracle.methodOrFieldDescription(method, GlobalConstantPool.getInstanceOf());
        LocalVariables lc = InitializeFirstInstruction.createLocalVariablesOfFirstInstruction(maxLocalSize,
                methodDesc, method.getAccessFlags().isStatic(), vThread.getObjectType());
        Vector parameters = null;
        if (lc != null) {
            parameters = lc.getAll();
        }
        frameFactory.createDataFlowAnalyzer().execute(method, parameters, null);
        FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();
        flowRecorder.addMainMethod(method, parameters);
    }

    /**
     * We start only from the main method of application and then traverse the
     * code.
     * We look at the right function by using proper method lookup.
     * We create threads as we traverse the code. A new thread is executed
     * only after previous thread has finished.
     *
     * @throws Exception
     */
    public void executeOGCDFA() throws Exception {
        //DataFlowAnalyzer.shouldDebugPrint = true;
        Oracle oracle = Oracle.getInstanceOf();
        String mainClassName = StartMeDCR.getMainClassName();
        GCDataFlowAnalyzer.clearMethodExecuted();
        ClassFile mainClassFile = oracle.getClass(mainClassName);
        VerificationPlaceHolder.getInstanceOf().setFactory(GCFrameFactory.getInstanceOf());
        MethodInfo mainMethod = null;
        try {
            mainMethod = (MethodInfo) oracle.getMethodOrField(mainClassFile, MAIN_METHOD_NAME,
                    MAIN_METHOD_DESC, true);
        } catch (Exception d) {
            Miscellaneous.printlnErr("Cannot find the main method.");
            Miscellaneous.exit();
        }
        VirtualThreadController vContr = VirtualThreadController.getInstanceOf();
        vContr.createVirtualThread(null, mainMethod, null);
        VirtualThread vThread = vContr.getNextThreadToRun();
        while (vThread != null) {
            executeThread(vThread);
            vContr.doneWithThreadExecution();
            vThread = vContr.getNextThreadToRun();
        }

        /**
         * recalled method due to fields.
         */
        RecallAMethod.getInstanceOf().execute();
        //methodsToBeDeleted();
    }

    private void methodsToBeDeleted() throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> allCodeAttCache = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> codeAttIt = allCodeAttCache.iterator();
        Vector<DCClassFile> checkClassFileFordeletion = new Vector<DCClassFile>();
        while (codeAttIt.hasNext()) {
            CodeAttCache codeAttCache = codeAttIt.next();
            DCMethodInfo method = (DCMethodInfo) codeAttCache.getMethodInfo();
            if (!GCDataFlowAnalyzer.methodAlreadyExecuted(method)) {
                if (method == null || method.getInstructions().size() == 0
                        || method.isFMKeepPerUserRequest()) {
                    continue;
                }
                DCClassFile cFile = (DCClassFile) codeAttCache.getClassFile();
                if (cFile.isKeepPerUserRequest() && oracle.isEmptyContructor(method)) {
                    continue;
                }
                Miscellaneous.println("Removing method ----> "
                        + oracle.getMethodOrFieldString(method)
                        + "--->" + ClassFile.currentClassToWorkOn.getFullyQualifiedClassName());
                int index = oracle.getMethodInfoIndexFromContr(cFile, method);
                //Miscellaneous.println(" size before removal = " + cFile.getMethodInfoController().getCurrentSize());
                cFile.getMethodInfoController().remove(index);
                //Miscellaneous.println(" size after removal = " + cFile.getMethodInfoController().getCurrentSize());
                checkClassFileFordeletion.addElement(cFile);
                //BytecodeVerifier.clearStaticInfo();
                //frameFactory.createDataFlowAnalyzer().execute(method, null, null);
                //FunctionsFlowRecorder.getInstanceOf().addMainMethod(method, null);
            }

        }
        removeClassFiles(checkClassFileFordeletion);
    }

    private void removeClassFiles(Vector<DCClassFile> cFilesVec) {
        Iterator<DCClassFile> it = cFilesVec.iterator();
        while (it.hasNext()) {
            DCClassFile cFile = it.next();
            ClassFileController cFileContr = ClassFileController.getInstanceOf();
            int numberOfMethods = cFileContr.getCurrentSize();
            /*
            Miscellaneous.println(" 1 " +cFile.getFullyQualifiedClassName());
            MethodInfoController mContr = cFile.getMethodInfoController();
            for (int loop = 0; loop < mContr.getCurrentSize(); loop++) {
            MethodInfo method = (MethodInfo) mContr.get(loop);
            Miscellaneous.println("\t\t 2 "+Oracle.getInstanceOf().getMethodString(method));
            }*/
            boolean userRequestedToKeep = cFile.isKeepPerUserRequest();
            if (numberOfMethods == 0 && !userRequestedToKeep) {
                int sizeBeforeRemoval = cFileContr.getCurrentSize();
                ClassFileController.getInstanceOf().remove(cFile);
                int sizeAfterRemoval = cFileContr.getCurrentSize();
                Miscellaneous.println(cFile.getFullyQualifiedClassName()
                        + ", method Removed. Method controller"
                        + " size is reduced from " + sizeBeforeRemoval
                        + " to " + sizeAfterRemoval);
            }
        }
    }

    public static void main(String args[]) throws Exception {
        (new StartMeOGC()).start(args);
    }
}
