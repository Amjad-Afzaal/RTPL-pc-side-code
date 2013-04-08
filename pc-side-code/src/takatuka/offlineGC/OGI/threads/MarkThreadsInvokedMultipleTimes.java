/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.OGI.threads;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.DAGNode;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.offlineGC.OGI.FTT.*;
import takatuka.offlineGC.OGI.GraphUtils.ReferenceFilter;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * If a thread is called multiple times all references used by it cannot be freed.
 * To find that a thread may be called multiple times we do following.
 *
 * 1) get the thread.
 * 2) If the thread is already been called multiple time then we are done. Otherwise
 * 3) Get the starting method of the thread and see if that method is recursive.
 * If so then thread might be called multiple times. If not then
 * 4) Make the DAG of the method instructions and see if the thread starting point is inside a
 * strongly connected component with more than one nodes. If so then the thread might be
 * started in a loop hence might be called multiple times.
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class MarkThreadsInvokedMultipleTimes {

    private static final MarkThreadsInvokedMultipleTimes myObj = new MarkThreadsInvokedMultipleTimes();

    /**
     * 
     */
    private MarkThreadsInvokedMultipleTimes() {
    }

    /**
     * 
     * @return
     */
    public static MarkThreadsInvokedMultipleTimes getInstanaceOf() {
        return myObj;
    }

    public void execute() {
        VirtualThreadController threadContr = VirtualThreadController.getInstanceOf();
        Collection<VirtualThread> vThreadCollection = threadContr.getAllFinishedThreads();
        Iterator<VirtualThread> vThreadIt = vThreadCollection.iterator();
        while (vThreadIt.hasNext()) {
            VirtualThread vThread = vThreadIt.next();
            if (vThread.getThreadStartedMultipleTimes()) {
                continue; // as 2 says we are done here.
            }
            MethodInfo method = vThread.getStartingMethod();
            GCType type = (GCType) vThread.getObjectType();
            FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();

            if (checkIfTheMethodIsRecursive(method, type, flowRecorder)) {
                vThread.setThreadStartedMultipleTimes(true);
                continue; // as 3 says we method is recursive hence done.
            }
            /**
             * now starting with part 4
             */
            GCInstruction instrCreatedTheThread = vThread.getInstrStartedTheThread();
            CreateIntraMethodDAG createIntraMethodDAG = CreateIntraMethodDAG.getInstanceOf();
            Vector<DAGNode> dagNodesVec = createIntraMethodDAG.createDAGNodes(method, null, null, ReferenceFilter.getInstanceOf());
            if (isInstrCreatedTheThreadInsideAloop(instrCreatedTheThread, dagNodesVec)) {
                vThread.setThreadStartedMultipleTimes(true);
            }
        }
    }

    private boolean containsInstr(SuperInstruction superInstr, GCInstruction instrCreatedThread) {
        Vector<GCInstruction> normalInstrVector = superInstr.getNormalInstrs();
        Iterator<GCInstruction> it = normalInstrVector.iterator();
        if (instrCreatedThread == null) {
            return false;
        }
        while (it.hasNext()) {
            GCInstruction instr = it.next();
            if (instr.getInstructionId() == instrCreatedThread.getInstructionId()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * check all the DAGnodes:
     * 1) if the DAGNode has single node then ignore it.
     * 2) If the DAGNode has multiple nodes and one of those nodes has the instruction that
     * has created the thread then return true otherwise false;
     * 
     * @param instrCreatedThread
     * @param dagNodeVec
     * @return
     */
    private boolean isInstrCreatedTheThreadInsideAloop(GCInstruction instrCreatedThread, Vector<DAGNode> dagNodeVec) {
        Iterator<DAGNode> dagNodeIt = dagNodeVec.iterator();
        while (dagNodeIt.hasNext()) {
            DAGNode dagNode = dagNodeIt.next();
            HashSet<InstrGraphNode> setForInstrNodes = dagNode.getGraphNodes();
            if (setForInstrNodes.size() == 1) {
                continue;
            } else {
                Iterator<InstrGraphNode> instrNodeIt = setForInstrNodes.iterator();
                while (instrNodeIt.hasNext()) {
                    InstrGraphNode instrGraphNode = instrNodeIt.next();
                    if (containsInstr(instrGraphNode.getInstruction(), instrCreatedThread)) {
                        return true;
                    }

                }
            }
        }
        return false;
    }

    private boolean checkIfTheMethodIsRecursive(MethodInfo method, GCType type,
            FunctionsFlowRecorder flowRecorder) {
        boolean isRecursive = false;
        if (method.getInstructions().size() == 0) {
            return isRecursive;
        }
        try {
            Oracle oracle = Oracle.getInstanceOf();
            int maxLocalSize = method.getCodeAtt().getMaxLocals().intValueUnsigned();
            String methodDesc = oracle.methodOrFieldDescription(method, GlobalConstantPool.getInstanceOf());
            String methodString = oracle.getMethodOrFieldString(method);
            if (methodString.contains("tests.test.offLineGCTest.ThreadTest1$Thread2.run()V")) {
                
            }
            LocalVariables lc = InitializeFirstInstruction.createLocalVariablesOfFirstInstruction(maxLocalSize,
                    methodDesc, method.getAccessFlags().isStatic(), type);
            Vector localVariables = null;
            if (lc != null) {
                localVariables = lc.getAll();
            }
            MethodCallInfo methodCallInfo = flowRecorder.getFunctionFlowNode(method, localVariables);
            MethodCallInfo methodToFind = methodCallInfo;
            Stack<MethodCallInfo> stack = new Stack<MethodCallInfo>();
            stack.addAll(methodCallInfo.getAllChildren());
            HashSet<MethodCallInfo> alreadyVisited = new HashSet<MethodCallInfo>();
            while (!stack.empty()) {
                methodCallInfo = stack.pop();
                if (methodCallInfo.equals(methodToFind)) {
                    isRecursive = true;
                    break;
                }
                if (alreadyVisited.contains(methodCallInfo)) {
                    continue;
                }
                stack.addAll(methodCallInfo.getAllChildren());
                alreadyVisited.add(methodCallInfo);
            }
        } catch (Exception d) {
            Miscellaneous.printlnErr("error at method " + Oracle.getInstanceOf().getMethodOrFieldString(method));
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return isRecursive;
    }
}
