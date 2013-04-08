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
package takatuka.offlineGC.OGI.superInstruction;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * A super instruction contains a group of instruction.
 * We traverse a method using the flow of its instructions.
 * A group of instruction visited one after anther are made part of a super
 * instruction if they use no-reference or same-reference and has no branch target/source
 * in between them. Note that along with normal branch source instructions,
 * the invokeinstruction is also consider as a branch source instruction.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateIntraMethodSuperInstrs {

    /**
     * 
     */
    private static final CreateIntraMethodSuperInstrs superInstr = new CreateIntraMethodSuperInstrs();
    private static final boolean debugMe = false;
    private static double totalNumberOfInstr = 0;
    private static double totalSuperInstr = 0;
    /**
     * To verify that all the method instructions has become part of at least one
     * super instruction.
     */
    private int totalNormalInstrInsideSuperInstrForAMethod = 0;

    /**
     *
     */
    private CreateIntraMethodSuperInstrs() {
    }

    private static final void print_debug(Object str, Object obj) {
        if (debugMe) {
            Miscellaneous.println(str + ", " + obj);
        }
    }

    private static final void print_debug(Object obj) {
        if (debugMe) {
            Miscellaneous.println(obj);
        }
    }

    /**
     *
     * @return
     */
    public static CreateIntraMethodSuperInstrs getInstanceOf() {
        return superInstr;
    }

    private class InstrAndParent {

        public GCInstruction instr = null;
        public SuperInstruction parentSuperInstr = null;

        public InstrAndParent(GCInstruction instr, SuperInstruction parentSuperInstr) {
            this.instr = instr;
            this.parentSuperInstr = parentSuperInstr;
        }

        @Override
        public String toString() {
            return "\nParent =" + parentSuperInstr.printMe() + ", Instr = " + instr;
        }
    }

    public void execute() {
        VirtualThreadController vContr = VirtualThreadController.getInstanceOf();
        Collection<VirtualThread> vThreadCollection = vContr.getAllFinishedThreads();
        Iterator<VirtualThread> it = vThreadCollection.iterator();
        FunctionStateRecorder.getInstanceOf().updateHashMapUsingFSK();
        FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();

        HashSet<MethodCallInfo> allFlowNodes = flowRecorder.getAllFunctionFlowNodes();
        FunctionStateRecorder stateRecorder = FunctionStateRecorder.getInstanceOf();
        Iterator<MethodCallInfo> flowNodeIt = allFlowNodes.iterator();
        SuperInstrsController superInstrContr = SuperInstrsController.getInstanceOf();
        print_debug("function state =", stateRecorder);
        print_debug(" flow record =", allFlowNodes);
        while (flowNodeIt.hasNext()) {
            MethodCallInfo flowNode = flowNodeIt.next();
            HashMap<Long, FunctionStateValueElement> state = stateRecorder.getFunctionState(flowNode);
            if (state.size() == 0) {
                print_debug("the state is empty for flowNode ", flowNode);
            }
            SuperInstruction newSuperInstrCreated = execute(flowNode, state, superInstrContr);
            print_debug("\n\n current super instruction = ", newSuperInstrCreated);
        }
        print_debug("\n\n\nvthread super controller = ", superInstrContr);

    }

    public static String statPrint() {
        double reduction = ((totalNumberOfInstr - totalSuperInstr) / totalNumberOfInstr) * 100;
        return "total Number of instruct = " + totalNumberOfInstr
                + ", superInstr created = " + totalSuperInstr + ", i.e. reduction=" + reduction;

    }

    public SuperInstruction execute(MethodCallInfo flowNode,
            HashMap<Long, FunctionStateValueElement> functionState,
            SuperInstrsController contr) {
        totalNormalInstrInsideSuperInstrForAMethod = 0;
        MethodInfo method = flowNode.getMethod();
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        print_debug(methodStr);

        if (methodStr.equals("tests.test.offLineGCTest.Main.indirectAndDirectRecusion()V")) {
            // Miscellaneous.println("Stop here abc");
        }
        SuperInstruction startingSuperInstr = contr.getSuperInstrForMethod(method);
        if (startingSuperInstr != null
                || method.getInstructions() == null
                || method.getInstructions().size() == 0) {
            return startingSuperInstr; //record already exist or has no instrutions.
        }
        HashMap<Long, SuperInstruction> alreadyVisitedInstr = new HashMap<Long, SuperInstruction>();
        Stack<InstrAndParent> stack = new Stack<InstrAndParent>();
        stack.push(new InstrAndParent((GCInstruction) method.getInstructions().firstElement(), null));
        SuperInstruction currentSuperInstr = null;
        while (!stack.empty()) {
            InstrAndParent instAndParent = stack.pop();
            GCInstruction currentInstr = instAndParent.instr;
            currentSuperInstr = instAndParent.parentSuperInstr;
            if (currentInstr.getInstructionId() == 33 || currentInstr.getInstructionId() == 34) {
                //Miscellaneous.println("stop here");
            }
            FunctionStateValueElement stateElms = null;
            try {
                stateElms = functionState.get(currentInstr.getInstructionId());
                print_debug("Current instr ", currentInstr);
                print_debug(" Ref =", stateElms.getAllReferenceUsedCache());
            } catch (Exception d) {
                continue;
                //Miscellaneous.printlnErr(" cannot find instruction state " + currentInstr + " of method " + methodStr);
                //Miscellaneous.printlnErr(" function state =" + functionState);
            }
            print_debug("Stack = " + stack);
            boolean shouldCreateAnewSuperInstr = shouldCreateANewSuperInstruction(currentSuperInstr,
                    currentInstr, stateElms);
            if (shouldCreateAnewSuperInstr) {
                SuperInstruction parentSuperInstr = currentSuperInstr;
                if (alreadyVisitedInstr.get(currentInstr.getInstructionId()) != null) {
                    currentSuperInstr = alreadyVisitedInstr.get(currentInstr.getInstructionId());
                } else {

                    currentSuperInstr = createNewSuperInstruction(
                            currentInstr, method, stateElms);
                }
                if (parentSuperInstr != null) {
                    parentSuperInstr.addNextSuperInstr(currentSuperInstr);
                    //print_debug(parentSuperInstr);
                }
                if (startingSuperInstr == null) {
                    startingSuperInstr = currentSuperInstr;
                }
            } else {
                addDataInExistingSuperInstruction(currentSuperInstr, currentInstr, stateElms);
            }
            //decide if should be a new super instruction or added in current super instruction
            //if should be a new super instruction then current instruction becomes parent superinstruction
            if (alreadyVisitedInstr.get(currentInstr.getInstructionId()) != null) {
                continue;
            }
            totalNumberOfInstr++;
            alreadyVisitedInstr.put(currentInstr.getInstructionId(), currentSuperInstr);
            Vector nxtInstrVec = currentInstr.getNextInstrsToBeExecutedRecord();
            Iterator nextInstrIt = nxtInstrVec.iterator();
            while (nextInstrIt.hasNext()) {
                GCInstruction nxtInstr = (GCInstruction) nextInstrIt.next();
                stack.push(new InstrAndParent(nxtInstr, currentSuperInstr));
            }
            print_debug("so far ", startingSuperInstr);
        }
        contr.addSuperInstrsForMethod(method, startingSuperInstr);
        if (totalNormalInstrInsideSuperInstrForAMethod != method.getInstructions().size()) {
            LogHolder.getInstanceOf().addLog("Warning!! Some instructions are not part of super instruction ("
                    + totalNormalInstrInsideSuperInstrForAMethod + " Vs "
                    + method.getInstructions().size() + ") at method ="
                    + methodStr);
            //findInstructionsNotPartofSuperInstructions((Vector) method.getInstructions().clone(),
            //      startingSuperInstr);
            //Miscellaneous.exit();
        }
        return startingSuperInstr;
    }

    /**
     * Called only in case of error. It generate more detail error report by
     * listing instructions that has not become part of any super instruction.
     *
     * @param methodInstrClone
     * @param superInstr
     */
    private void findInstructionsNotPartofSuperInstructions(Vector methodInstrClone,
            SuperInstruction superInstr) {
        Miscellaneous.println("\n\n\n\n " + superInstr);
        Miscellaneous.println("\n\n\n\n " + methodInstrClone);
        Stack<SuperInstruction> stack = new Stack<SuperInstruction>();
        stack.push(superInstr);
        while (!stack.empty()) {
            SuperInstruction currentSInstr = stack.pop();
            if (currentSInstr.visitedOnce) {
                continue;
            }
            currentSInstr.visitedOnce = true;
            methodInstrClone.removeAll(currentSInstr.getNormalInstrs());
            stack.addAll(currentSInstr.getNextSuperInstrs());
        }
        Miscellaneous.printlnErr("Following instructions of the method are not\n"
                + " made part of any super instruction =\n"
                + methodInstrClone);
    }

    private void addDataInExistingSuperInstruction(SuperInstruction existingSuperInstr,
            GCInstruction currentInstruction, FunctionStateValueElement instrState) {
        if (existingSuperInstr.addNormalInstrs(currentInstruction)) {
            totalNormalInstrInsideSuperInstrForAMethod++;
        }
        existingSuperInstr.tempRefForComputation.addAll(instrState.getAllReferenceUsedCache());
    }

    private SuperInstruction createNewSuperInstruction(
            GCInstruction currentInstruction,
            MethodInfo currentMethod,
            FunctionStateValueElement stateOfCurrentInstr) {
        SuperInstruction newSuperInstr = new SuperInstruction(currentMethod);
        if (newSuperInstr.addNormalInstrs(currentInstruction)) {
            totalNormalInstrInsideSuperInstrForAMethod++;
        }
        newSuperInstr.tempRefForComputation = stateOfCurrentInstr.getAllReferenceUsedCache();
        totalSuperInstr++;
        return newSuperInstr;

    }

    private boolean shouldCreateANewSuperInstruction(SuperInstruction lastSuperInstr,
            GCInstruction currentInstr,
            FunctionStateValueElement stateElmForCurrInstr) {
        if (true || lastSuperInstr == null || lastSuperInstr.tempRefForComputation == null) {
            return true;
        }
        HashSet<TTReference> lastInstrRef = lastSuperInstr.tempRefForComputation;

        HashSet<TTReference> curretInstrRef = stateElmForCurrInstr.getAllReferenceUsedCache();
        GCInstruction lastInstr = null;
        if (lastSuperInstr != null) {
            lastInstr = lastSuperInstr.getNormalInstrs().lastElement();
            if (lastInstr != null
                    && (lastInstr.isBranchSource()
                    || lastInstr.isBranchTarget()
                    || lastInstr.isBranchSourceInstruction()
                    || lastInstr.getMnemonic().contains("INVOKE")
                    || lastInstr.getMnemonic().contains("STATIC")
                    || lastInstr.getMnemonic().contains("RETURN"))) {
                return true;
            }
        }
        if (!currentInstr.isBranchSource()
                && !currentInstr.isBranchTarget()
                && !currentInstr.isBranchSourceInstruction()
                && !currentInstr.getMnemonic().contains("INVOKE")
                && !currentInstr.getMnemonic().contains("STATIC")
                && !currentInstr.getMnemonic().contains("RETURN")
                && (curretInstrRef.equals(lastInstrRef) /*|| curretInstrRef.size() == 0
                || lastInstrRef.size() == 0*/)) {
            return false;
        }
        return true;
    }
}
