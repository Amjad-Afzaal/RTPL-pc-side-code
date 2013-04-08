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
package takatuka.offlineGC.DFA.dataObjs.flowRecord;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.logic.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.DFA.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FunctionsFlowRecorder {

    private HashSet<MethodCallInfo> mainMethodRecord = new HashSet<MethodCallInfo>();
    private HashSet<MethodCallInfo> methodCalled = new HashSet<MethodCallInfo>();
    private HashMap<MethodInfo, HashSet<Long>> methodInvokeRecord =
            new HashMap<MethodInfo, HashSet<Long>>();
    private HashSet<FunctionStateKey> callingParaUsedInChildMethodLookups = new HashSet<FunctionStateKey>();
    private static final FunctionsFlowRecorder myObj = new FunctionsFlowRecorder();
    private static final HashSet<MethodCallInfo> methodToBeCalled = new HashSet<MethodCallInfo>();

    private FunctionsFlowRecorder() {
    }

    public static FunctionsFlowRecorder getInstanceOf() {
        return myObj;
    }

    /**
     * 
     * @return
     */
    public HashSet<MethodCallInfo> getAllFunctionFlowNodes() {
        return methodCalled;
    }

    public void setCallingParaUsedInMethodLookups(FunctionStateKey flowNode) {
        callingParaUsedInChildMethodLookups.add(flowNode);
    }

    /**
     * 
     * @param flowNode
     * @return
     */
    public boolean isCallingParaUsedInMethodLookups(FunctionStateKey flowNode) {
        if (callingParaUsedInChildMethodLookups.contains(flowNode)) {
            return true;
        }
        return false;
    }

    /**
     * record the main methods.
     * The main method are starting point of code anaylsis
     * 
     * @param method
     * @param callingParam
     * @throws java.lang.Exception
     */
    public void addMainMethod(MethodInfo method, Vector callingParam) throws Exception {
        if (callingParam == null) {
            Frame frame = InitializeFirstInstruction.createFrameAndInitFirstInstr(method, null);
            callingParam = frame.getLocalVariables().getAll();
        }
        callingParam = TransformCallingParameters.transformCallingParameters(method, callingParam);
        MethodCallInfo input = createFFNodeAndAddInTheList(method, callingParam);
        mainMethodRecord.add(input);
    }

    /**
     * 
     * @return
     */
    public HashSet<MethodCallInfo> getMainMethods() {
        return mainMethodRecord;
    }

    public MethodCallInfo getFunctionFlowNode(MethodInfo method, Vector parms) {
        MethodCallInfo input = new MethodCallInfo(method, parms);
        parms = TransformCallingParameters.transformCallingParameters(method, parms);
        Iterator<MethodCallInfo> nodesIt = methodCalled.iterator();
        while (nodesIt.hasNext()) {
            MethodCallInfo node = nodesIt.next();
            //      Miscellaneous.println("finding = "+input+" -- current --"+node);
            if (node.equals(input)) {
                return node;
            }
        }
        return null;
    }

    /**
     * This function should be called before execution of a method.
     * This record that method in the set of methods called.
     * 
     * @param method
     * @param parms 
     */
    public void recordMethodCalled(MethodInfo method, Vector parms) {
        MethodCallInfo node = new MethodCallInfo(method, parms);
        Iterator<MethodCallInfo> it = methodToBeCalled.iterator();
        MethodCallInfo toRemove = null;
        while (it.hasNext()) {
            MethodCallInfo savedNode = it.next();
            if (savedNode.equals(node)) {
                toRemove = savedNode;
                break;
            }
        }
        if (toRemove == null) {
            /**
             * should come here only for the main method of the thread.
             * Otherwise should never come here.
             */
            toRemove = node;
            Oracle oracle = Oracle.getInstanceOf();
            String methodStr = oracle.getMethodOrFieldString(method);
        }
        methodCalled.add(toRemove);
        methodToBeCalled.remove(toRemove);

    }

    private MethodCallInfo createFFNodeAndAddInTheList(MethodInfo method, Vector parms) {
        MethodCallInfo node = getFunctionFlowNode(method, parms);
        if (node == null) {
            node = new MethodCallInfo(method, parms);
            methodToBeCalled.add(node);
            //debuging
            /*Oracle oracle = Oracle.getInstanceOf();
            String methodStr = oracle.getMethodString(method);
            if (methodStr.contains("tests.test.offLineGCTest.ThreadTest1$Thread2.run()V")) {
            }*/

        }
        return node;
    }

    /**
     * it record that what an invoke instruction has invoked.
     * 
     *
     * 
     * @param parentMethod
     * @param parentParams
     * @param currentInstr
     * @param callingMethod
     * @param callingParams
     */
    public void addInvokeCall(MethodInfo parentMethod, Vector parentParams,
            VerificationInstruction currentInstr, MethodInfo callingMethod, Vector callingParams) {
        parentParams = TransformCallingParameters.transformCallingParameters(parentMethod, parentParams);
        callingParams = TransformCallingParameters.transformCallingParameters(callingMethod, callingParams);
        /*Oracle oracle = Oracle.getInstanceOf();
        String callingMethodName = oracle.getMethodString(callingMethod);
        String parentMethodName = oracle.getMethodString(parentMethod);
         */
        //find existing recrod.
        MethodCallInfo parentNode = createFFNodeAndAddInTheList(parentMethod, parentParams);
        MethodCallInfo childNode = createFFNodeAndAddInTheList(callingMethod, callingParams);

        parentNode.addChildNode(currentInstr, childNode);


        //  Miscellaneous.println("added = " + lastAdditionRequest);
        saveMethodInvokeInstrId(parentMethod, currentInstr);
        // FunctionFlowNode.callMeBeforeEachToStringCallFromOutside();
        // Miscellaneous.println("\n\n------ Stop here " + parentMethodName + "->" + callingMethodName + "\nall Nodes="+allNodes);
        // Miscellaneous.println("Stop here ");

    }


    /***
     * return true if function has been invoked before.
     * It check the main method record and see if the function exist there.
     * and subsequently it also check the invoke call record.
     */
    public boolean invokedBefore(MethodInfo method, Vector param) {
        param = TransformCallingParameters.transformCallingParameters(method, param);
        MethodCallInfo value = new MethodCallInfo(method, param);
        return methodCalled.contains(value);
    }

    private void saveMethodInvokeInstrId(MethodInfo method, VerificationInstruction instr) {

        HashSet<Long> instrRecord = methodInvokeRecord.get(method);
        if (instrRecord == null) {
            instrRecord = new HashSet<Long>();
            methodInvokeRecord.put(method, instrRecord);
        }
        instrRecord.add(instr.getInstructionId());
    }

    /**
     *
     * @param method
     * @return
     */
    private HashSet<Integer> getMethodInvokeInstrOffSets(MethodInfo method) {
        HashSet tree = methodInvokeRecord.get(method);
        if (tree == null) {
            tree = new HashSet();
        }
        return tree;
    }

    private String toString(MethodCallInfo node, String ret, HashSet alreadyVisitedMyChildren) {
        String toReturn = "";
        Set<Long> invokeInstrIds = node.getAllChildrenCorrespondingInvokeInstrId();
        Iterator<Long> it = invokeInstrIds.iterator();
        while (it.hasNext()) {
            long invokeId = it.next();
            Set<MethodCallInfo> childrens = node.getChildren(invokeId);
            Iterator<MethodCallInfo> childIt = childrens.iterator();
            while (childIt.hasNext()) {
                MethodCallInfo childNode = childIt.next();
                toReturn += ret + invokeId + ":" + childNode + "\n";
                if (alreadyVisitedMyChildren.contains(childNode)) {
                    toReturn += ret + "\t...\n";
                } else {
                    alreadyVisitedMyChildren.add(childNode);
                    toReturn += toString(childNode, ret + "\t", alreadyVisitedMyChildren);
                }
            }
        }
        return toReturn;
    }

    @Override
    public String toString() {
        String ret = "";
        HashSet<String> alreadyVisitedMyChildren = new HashSet<String>();
        Iterator<MethodCallInfo> mainIterator = mainMethodRecord.iterator();
        while (mainIterator.hasNext()) {
            MethodCallInfo mainNode = mainIterator.next();
            ret += "\n@MAIN:" + mainNode + "\n";
            ret += toString(mainNode, "\t", alreadyVisitedMyChildren) + "\n";
        }
        return ret;
    }
}
