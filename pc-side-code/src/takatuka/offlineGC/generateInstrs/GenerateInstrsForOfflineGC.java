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
package takatuka.offlineGC.generateInstrs;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.optimizer.VSS.logic.preCodeTravers.ResetBranches;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * 
 * It generates OFFLINE_GC_NEW and OFFLINE_GC_FREE instructions. These instruction are
 * used at run time to free the memory without doing any computation.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenerateInstrsForOfflineGC {

    private static final GenerateInstrsForOfflineGC genInstr = new GenerateInstrsForOfflineGC();
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private static Vector<Integer> allowedNewIdsPerlimitOfMaxSizedObjects = new Vector<Integer>();
    private NewInstrIdFactory newInstrIdFact = NewInstrIdFactory.getInstanceOf();
    /**
     * @todo this should be 1 instead of 2.
     */
    private static final int SIZE_OF_OFF_GC_INSTR_OPERAND = 2;
    private RemoveUnUsedNewIdInstr removeUnsedIds = new RemoveUnUsedNewIdInstr();
    private static LogRecordController logContr = LogRecordController.getInstanceOf();
    private static int numberOfFreeInstr = 0;
    private static HashMap<String, Integer> numberOfFreeInstrPerMethod = new HashMap<String, Integer>();
    /**
     * We might want to strict the number of newIds generated. That is for two reasons
     * 1. We have only one byte for newId hence cannot have more than 256 ids.
     * 2. For testing we want to know that exactly what ID generate an error.
     * Therefore we want to generate a set of ids starting from x and ending at y
     */
    private static int newIdStartRange = 0;
    private static int newIdEndRange = 255;
    private TreeSet<Integer> generatedNewInstr = new TreeSet<Integer>();
    private static HashSet<Integer> validNewIds = new HashSet<Integer>();
    private static boolean useValidNewIdsInsteadOfRange = false;
    private static int limitForTopNIDWithMaxSizeObjects = 255;
    private static int minSizeOfAnObjectToBeDellocated = 0;
    private ResetBranches resetBranch = null;

    public static GenerateInstrsForOfflineGC getInstanceOf() {
        return genInstr;
    }

    public static void setMaxSizeObjectsLimit(int limit) {
        limitForTopNIDWithMaxSizeObjects = limit;
    }

    public int getTotalNewIdGenerated() {
        return generatedNewInstr.size();
    }

    public int maxNewIdGenerated() {
        if (!generatedNewInstr.isEmpty()) {
            return generatedNewInstr.last();
        } else {
            return 0;
        }
    }

    public static void setValidNewIdsList(String idSeperatedByCommas) {
        StringTokenizer token = new StringTokenizer(idSeperatedByCommas, ",");
        if (token.hasMoreTokens()) {
            useValidNewIdsInsteadOfRange = true;
        }
        while (token.hasMoreTokens()) {
            String sId = token.nextToken().trim();
            validNewIds.add(Integer.parseInt(sId));
        }
    }

    public static void setNewIdEndRange(int endRange) {
        if (endRange < 0 && endRange > 255) {
            Miscellaneous.printlnErr("invalid end-range id");
            Miscellaneous.exit();
        }
        newIdEndRange = endRange;
    }

    public static void setNewIdStartRange(int startRange) {
        if (startRange < 0 && startRange > 255) {
            Miscellaneous.printlnErr("invalid start-range id");
            Miscellaneous.exit();
        }
        newIdStartRange = startRange;
    }

    /**
     * generates instructions for offline GC and append them in the original code.
     */
    public void execute() {
        //NewInstrIdFactory.getInstanceOf().removeNewOfMethod("java.");
        //new DeleteDupFreeInstrBasedOnAlgo().execute();
        /**
         * Will enable this code later on.
         */
        new CombineNewInstr().execute();
        numberOfFreeInstr = 0;
        numberOfFreeInstrPerMethod.clear();
        removeUnsedIds.execute();
        removeUnsedIds.LogMe();
        Oracle oracle = Oracle.getInstanceOf();

        allowedNewIdsPerlimitOfMaxSizedObjects = new NewIdAndObjectSizeUtil().getTopNIDCorrespToObjectSize(limitForTopNIDWithMaxSizeObjects, minSizeOfAnObjectToBeDellocated);

        Vector<CodeAttCache> codeAttInfoVec = oracle.getAllCodeAtt();
        for (int loop = 0; loop < codeAttInfoVec.size(); loop++) {
            CodeAttCache codeAttInfo = codeAttInfoVec.elementAt(loop);
            MethodInfo method = codeAttInfo.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);
            if (methodStr.equals("jvmTestCases.LoadAndStoreConstantMathOpcodes_1.<init>()V")) {
                //Miscellaneous.println("stop here baby");
            }
            int numberOfInstrFreeInTheMethod = numberOfFreeInstr;
            execute(method);
            numberOfInstrFreeInTheMethod = numberOfFreeInstr - numberOfInstrFreeInTheMethod;
            numberOfFreeInstrPerMethod.put(methodStr, numberOfInstrFreeInTheMethod);
        }

        logContr.generateLog();
    }

    public static HashMap<String, Integer> getInstrFreePerMethod() {
        return numberOfFreeInstrPerMethod;
    }

    public int getNumberOfFreeInstrGenerated() {
        return numberOfFreeInstr;
    }

    private void execute(MethodInfo method) {
        Vector instrVec = method.getInstructions();
        boolean methodChanged = false;
        int totalAddedInstr = 0;
        resetBranch = new ResetBranches(method);
        for (int loop = 0; loop < instrVec.size(); loop = loop + 1 + totalAddedInstr) {
            totalAddedInstr = execute(loop, instrVec, method);
            if (totalAddedInstr > 0) {
                methodChanged = true;
            }
        }
        if (methodChanged) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.OFFLINE_GC_FREE);
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.OFFLINE_GC_NEW);
            method.getCodeAtt().setInstructions(instrVec);
            resetBranch.restore();
        }
    }

    private int execute(int currentInstrIndex, Vector<GCInstruction> allInstrs, MethodInfo method) {
        int totalAdded = 0;
        if (generatedInstrsForNewAndLDC(currentInstrIndex, allInstrs, method)) {
            totalAdded++;
        }

        totalAdded += generateInstrsForFreeGC(currentInstrIndex, allInstrs, method);
        return totalAdded;
    }

    private boolean generatedInstrsForNewAndLDC(int currentInstrIndex,
            Vector<GCInstruction> allInstrs, MethodInfo method) {
        NewInstrKey newInstrKey = null;
        try {
            GCInstruction currentInstr = allInstrs.elementAt(currentInstrIndex);
            newInstrKey = new NewInstrKey(method, currentInstr);
            int newInstrId = newInstrIdFact.getNewInstrGCId(newInstrKey);
            if (newInstrId == -1) {
                return false;
            }
            generatedInstrForNewAndLDC(newInstrId, allInstrs, currentInstrIndex,
                    currentInstr, method.getCodeAtt());
        } catch (Exception d) {
            Miscellaneous.printlnErr("...... " + newInstrKey);
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return true;
    }

    private boolean mayGenerateNewOrFreeInstr(int newInstrId, int newnewInstrId) {
        if (allowedNewIdsPerlimitOfMaxSizedObjects.size() > 0
                && !allowedNewIdsPerlimitOfMaxSizedObjects.contains(newInstrId)) {
            return false;
        }
        if (newnewInstrId < 1
                || (!useValidNewIdsInsteadOfRange
                && (newnewInstrId > newIdEndRange || newnewInstrId < newIdStartRange))
                || (useValidNewIdsInsteadOfRange && !validNewIds.contains(newnewInstrId))) {
            return false;
        }

        return true;
    }

    private void generatedInstrForNewAndLDC(int newInstrId, Vector methodInstr,
            int instrIndex, GCInstruction currentInstr, CodeAtt codeAtt) throws Exception {
        /**
         * make newIds sequential.
         */
        int newnewInstrId = removeUnsedIds.getNew_NewId(newInstrId);
        if (!mayGenerateNewOrFreeInstr(newInstrId, newnewInstrId)) {
            logContr.addNewInstrNotFreedRecord(new LogRecord(currentInstr, -1));
            return;
        }
        generatedNewInstr.add(newnewInstrId);
        Instruction newlyCreateInstr = factory.createInstruction(JavaInstructionsOpcodes.OFFLINE_GC_NEW,
                factory.createUn(newnewInstrId).trim(SIZE_OF_OFF_GC_INSTR_OPERAND), codeAtt);
        //insert OFFLINE_GC_NEW instruction before the new instruction.
        methodInstr.insertElementAt(newlyCreateInstr, instrIndex /*+ 1*/);
        logContr.addNewInstrRecord(new LogRecord(currentInstr, newnewInstrId));
    }

    /**
     *
     * @param currentInstrIndex
     * @param methodInstr
     * @param methodInfo
     * @return
     */
    private int generateInstrsForFreeGC(int currentInstrIndex,
            Vector<GCInstruction> methodInstr, MethodInfo methodInfo) {
        int totalAdded = 0;
        try {
            GCInstruction instr = (GCInstruction) methodInstr.elementAt(currentInstrIndex);
            // Miscellaneous.println(" ----------- "+instr);
            HashSet set = instr.getReferencesFreedOnMe();
            Iterator<TTReference> it = set.iterator();
            while (it.hasNext()) {
                TTReference ref = it.next();
                int newInstrId = ref.getNewId();
                if (newInstrId == -1) {
                    continue;
                }
                if (generateInstrForFreeGC(methodInstr, currentInstrIndex, newInstrId, methodInfo,
                        instr, !it.hasNext())) {
                    numberOfFreeInstr++;
                    totalAdded++;
                }
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return totalAdded;
    }

    private boolean generateInstrForFreeGC(Vector<GCInstruction> methodInstr,
            int atIndex, int newInstrId, MethodInfo methodInfo,
            GCInstruction parentInstr, boolean isLastInstrToBeInserted) {
        try {
            int newnewInstrId = removeUnsedIds.getNew_NewId(newInstrId);
            if (!mayGenerateNewOrFreeInstr(newInstrId, newnewInstrId)) {
                return false;
            }

            GCInstruction freeInstr = (GCInstruction) factory.createInstruction(JavaInstructionsOpcodes.OFFLINE_GC_FREE,
                    factory.createUn(newnewInstrId).trim(SIZE_OF_OFF_GC_INSTR_OPERAND),
                    methodInfo.getCodeAtt());
            /**
             * The following code replace the id of the normal instruction with
             * the newly created free instruction. This is done so that newly created
             * free instruction become the branch target instead of normal original
             * instruction. This is to make sure that free instructions are always executed.
             */
            if (isLastInstrToBeInserted) {
                resetBranch.addToRestore(parentInstr, freeInstr);
            }
            methodInstr.insertElementAt((GCInstruction) freeInstr,
                    atIndex);
            logContr.addFreeInstrRecord(new LogRecord(parentInstr, newnewInstrId));

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return true;
    }
}
