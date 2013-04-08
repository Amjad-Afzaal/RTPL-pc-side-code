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
package takatuka.offlineGC.OGI.GraphUtils;

import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.offlineGC.generateInstrs.LogRecord;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateIMGraph extends CreateInstrGraph {

    private static final CreateIMGraph myObj = new CreateIMGraph();
    private HashSet<String> graphCreatedMultipleTimes = new HashSet<String>();
    private HashSet<String> graphCreatedOnce = new HashSet<String>();
    private static HashSet<VirtualThread> alreadyHasTheThreadData = new HashSet<VirtualThread>();
    private static boolean writtenLogOnce = false;
    /**
     * This records number of times graph is created for a given methodcall.
     * This is for scalability so that a graph should never be created from 
     * more than numberOfTimeCreatingGraphIsAllowed.
     */
    private HashMap<MethodCallInfo, Integer> numberOfTimesGraphCreated = new HashMap<MethodCallInfo, Integer>();
    private static final int numberOfTimeCreatingGraphIsAllowed = 1;
    /**
     * It cache all the references used in certain graphs.
     * We do not create those graphs but export their references to the
     * parent method instruction that should originate such a graph.
     */
    private HashMap<String, HashSet<TTReference>> refsOfGraphNotCreated = null;
    private HashSet<String> methodNotToCreateGraphsCache = null;

    /**
     *
     * @return
     */
    public static CreateIMGraph getInstanceOf() {
        return myObj;
    }

    public boolean isGraphCreatedMultipleTimes(String methodStr) {
        if (!writtenLogOnce) {
            LogHolder.getInstanceOf().addLog("\n\n\n*** Method called multiple Time and"
                    + " hence not allowed to be free refs at =\n" + graphCreatedMultipleTimes + "\n\n\n\n",
                    LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
            writtenLogOnce = true;
        }
        return graphCreatedMultipleTimes.contains(methodStr);
    }

    private void clear() {
        numberOfTimesGraphCreated.clear();
        //graphCreatedOnce.clear();
        //graphCreatedMultipleTimes.clear();
        refsOfGraphNotCreated = new HashMap<String, HashSet<TTReference>>();
        methodNotToCreateGraphsCache = new HashSet<String>();
    }

    public InstrGraphNode createInterMethodGraph(VirtualThread vthread, boolean createMethodLevelGraph) {
        clear();
        MethodInfo mainMethod = vthread.getStartingMethod();
        FunctionsFlowRecorder flowRecorder = FunctionsFlowRecorder.getInstanceOf();
        HashSet<MethodCallInfo> mainMethodFlowNodeSet = flowRecorder.getMainMethods();
        Iterator<MethodCallInfo> mainMethodFlowIt = mainMethodFlowNodeSet.iterator();
        MethodCallInfo mainMethodFlow = null;
        Oracle oracle = Oracle.getInstanceOf();
        while (mainMethodFlowIt.hasNext()) {
            MethodCallInfo funFlowNode = mainMethodFlowIt.next();
            if (oracle.getMethodOrFieldString(mainMethod).equals(oracle.getMethodOrFieldString(funFlowNode.getMethod()))) {
                mainMethodFlow = funFlowNode;
                break;
            }
        }
        if (mainMethodFlow == null) {
            Miscellaneous.printlnErr("Cannot find the thread main method. Error # 68923 ");
            Miscellaneous.exit();
        }

        InstrGraphNode interMethodGraph = (InstrGraphNode) createInterMethodGraph(vthread, mainMethodFlow,
                new HashSet<Long>(), null,
                new HashMap<String, InstrGraphNode>(),
                createMethodLevelGraph);
        alreadyHasTheThreadData.add(vthread);
        /**
         * The will remove a useless link from an invoke instruction to
         * the next instruction of the same method method.
         */
        CreateInterMethodLinks.getInstanceOf().removeAllChildNodesGeneratedFromInvoke();
        return interMethodGraph;
    }

    private HashSet<String> methodNotToCreateGraph() {
        if (methodNotToCreateGraphsCache.size() != 0) {
            return methodNotToCreateGraphsCache;
        }
        try {
            /* uncomment following to test string appends.
            methodNotToCreateGraphsCache.add("<init>");
            methodNotToCreateGraphsCache.add("getChars");
            methodNotToCreateGraphsCache.add("arraycopy");
            methodNotToCreateGraphsCache.add("expandCapacity");
            methodNotToCreateGraphsCache.add("valueOf");
             */

//            methodNotToCreateGraphsCache.add("getChars");
            Oracle oracle = Oracle.getInstanceOf();
            HashSet<ClassFile> allSubClassesOfException = oracle.getAllSubClasses("java/lang/Exception");
            //allSubClassesOfException.addAll(oracle.getAllClassesStartWith("java/"));
            //allSubClassesOfException.addAll(oracle.getAllSubClasses("java/lang/RuntimeException"));
            Iterator<ClassFile> it = allSubClassesOfException.iterator();
            while (it.hasNext()) {
                methodNotToCreateGraphsCache.add(it.next().getFullyQualifiedClassName());
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return methodNotToCreateGraphsCache;

    }

    /**
     * Check for two things:
     * 
     * Number 1:
     * If graph is already created more than what is allowed then do not create such
     * a graph again for scalability.
     * 
     * Number 2;
     * If graph is for one of the functions whose GC we are not interested in then 
     * do not create the graph too.
     * 
     * @param currentMethodCallInfo
     * @return 
     */
    private boolean shouldCreateGraph(MethodCallInfo currentMethodCallInfo) {
        //Number 1:
        Oracle oracle = Oracle.getInstanceOf();
        String currentMethodStr = oracle.getMethodOrFieldString(currentMethodCallInfo.getMethod());
        Integer noOfTimesgraphCreated= numberOfTimesGraphCreated.get(currentMethodCallInfo);
        if (noOfTimesgraphCreated != null && noOfTimesgraphCreated > numberOfTimeCreatingGraphIsAllowed) {
            //System.out.println("graph is not created for "+currentMethodStr);
            return false;
        }
        //Number 2:
        HashSet<String> methodNotToBeCreateGrpah = methodNotToCreateGraph();
        currentMethodStr = currentMethodStr.replace(".", "/");
        Iterator<String> it = methodNotToBeCreateGrpah.iterator();
        while (it.hasNext()) {
            String methodStr = it.next();
            if (currentMethodStr.startsWith(methodStr)) {
                return false;
            }
        }
        return true;
    }

    private void incrementCounterForGraphCreation(MethodCallInfo mInfo) {
        Integer currentCount = numberOfTimesGraphCreated.get(mInfo);
        if (currentCount == null) {
            currentCount = new Integer(0);
        }
        numberOfTimesGraphCreated.put(mInfo, currentCount + 1);

    }

    private InstrGraphNode createInterMethodGraph(VirtualThread vthread,
            MethodCallInfo mainMethodFlow,
            HashSet<Long> underConsiderationInvokes,
            GCInstruction currentInvokeInstr,
            HashMap<String, InstrGraphNode> methodUnderConsideration,
            boolean createMethodLevelGraph) {
        if (currentInvokeInstr != null
                && underConsiderationInvokes.contains(currentInvokeInstr.getInstructionId())) {
            return null;
        }
        String methodStr = Oracle.getInstanceOf().getMethodOrFieldString(mainMethodFlow.getMethod());
        InstrGraphNode previousGraph = methodUnderConsideration.get(methodStr);
        /**
         * @Todo recursion is not correctly implmented.
         * It is because if a method calls itself with different parameters then
         * this will not work.
         * This could be fixed easily by merging two methods with different calls.
         * The code is already there in the mergeInstrGraph however it has a bug that
         * needs to be fixed.
         */
        if (previousGraph != null /*&& previousGraph.getMethodCallInfo().contains(mainMethodFlow)*/) {
            return previousGraph;
        } else if (previousGraph != null && !previousGraph.getMethodCallInfo().contains(mainMethodFlow)) {
            //System.out.println("see me ---> "+methodStr);
            //PNRAlgo.debugOn();
            InstrGraphNode newMethodInstrGraph = createInstrGraph(mainMethodFlow, vthread, createMethodLevelGraph);
            MergeInstrGraph.getInstanceOf().merge(previousGraph, newMethodInstrGraph);
            return previousGraph;

        }
        if (currentInvokeInstr != null) {
            underConsiderationInvokes.add(currentInvokeInstr.getInstructionId());
        }
        if (methodStr.contains("testInovkeVirtual(I)")) {
            //IntraMethodAlgorithm.shouldDebugPrint = true;
            //IntraMethodAlgorithm.debug("Stop here baby");
            //IntraMethodAlgorithm.debug(" method Instr = " + mainMethodFlow.getMethod().getInstructions());
        }
        /**
         * Make the graph of given method.
         */
        InstrGraphNode methodGraph = createInstrGraph(mainMethodFlow, vthread, createMethodLevelGraph);

        if (!alreadyHasTheThreadData.contains(vthread)) {
            if (!graphCreatedOnce.contains(methodStr)) {
                graphCreatedOnce.add(methodStr);
            } else {
                graphCreatedMultipleTimes.add(methodStr);
            }
        }
        //IntraMethodAlgorithm.debug("--------- "+methodGraph);
        methodUnderConsideration.put(methodStr, methodGraph);

        PNRAlgo.debug("method=" + Oracle.getInstanceOf().getMethodOrFieldString(mainMethodFlow.getMethod())/*+
                ", graph=\n"+methodGraph*/);

        InvokeInstructionRecord invokeInstrRecord = InvokeInstructionRecord.getInstanceOf();
        Collection<GCInstruction> invokeIsntr = invokeInstrRecord.getInvokeInstrRecord(mainMethodFlow);
        PNRAlgo.debug(" invoke Instru =" + invokeIsntr + ", " + Oracle.getInstanceOf().getMethodOrFieldString(mainMethodFlow.getMethod()));
        Iterator<GCInstruction> invokeIt = invokeIsntr.iterator();
        CreateInterMethodLinks createIMLink = CreateInterMethodLinks.getInstanceOf();
        /**
         * Make the graph of all method invoked by of a method
         * and create links.
         */
        while (invokeIt.hasNext()) {
            GCInstruction nextInvokeInstr = invokeIt.next();
            Set<MethodCallInfo> nextMethodsFlowSet = mainMethodFlow.getChildren(nextInvokeInstr.getInstructionId());
            if (nextMethodsFlowSet == null) {
                continue;
            }
            Iterator<MethodCallInfo> nextMethodIt = nextMethodsFlowSet.iterator();

            while (nextMethodIt.hasNext()) {
                MethodCallInfo nextMethodFlow = nextMethodIt.next();
                Oracle oracle = Oracle.getInstanceOf();

                if (!shouldCreateGraph(nextMethodFlow)) {
                    /**
                     * For some methods we do not create graphs but import their references on the
                     * invoke node of the parent graph.
                     */
                    importRefAndAvoidGraph(vthread, nextMethodFlow, methodGraph,
                            nextInvokeInstr);
                } else {
                    incrementCounterForGraphCreation(nextMethodFlow);
                    InstrGraphNode childGraph = createInterMethodGraph(vthread,
                            nextMethodFlow,
                            underConsiderationInvokes, nextInvokeInstr,
                            methodUnderConsideration,
                            createMethodLevelGraph);

                    if (childGraph == null) {
                        continue;
                    }
                    String methodGraphStr = oracle.getMethodOrFieldString(methodGraph.getMethod());
                    String childGraphStr = oracle.getMethodOrFieldString(childGraph.getMethod());
                    //System.out.println(nextInvokeInstr.getMnemonic()+", created link between ="
                    //      + methodGraphStr + " ------> " + childGraphStr);
                    PNRAlgo.debug("created link between ="
                            + methodGraphStr + " ------> " + childGraphStr);
                    createIMLink.execute(methodGraph, childGraph, nextInvokeInstr,
                            createMethodLevelGraph);
                }
            }
        }
        if (currentInvokeInstr != null) {
            underConsiderationInvokes.remove(currentInvokeInstr.getInstructionId());
        }

        methodUnderConsideration.remove(methodStr);
        workAfterMethodIsAddedInInterMethodGraph(mainMethodFlow, methodGraph,
                vthread);
        return methodGraph;
    }

    private HashSet<TTReference> getRefFromCallingPara(Vector callingParm) {
        HashSet<TTReference> ret = new HashSet<TTReference>();
        Iterator<GCType> it = callingParm.iterator();
        while (it.hasNext()) {
            GCType type = it.next();
            ret.addAll(type.getReferences());
        }
        return ret;
    }

    /**
     * This function is important. It is used to get references of
     * a subgraph in the node originating such a graph without making a graph.
     * This significantly reduces the complexity of the graph making and processing
     *  
     * 
     * @param vthread
     * @param methodCallInfo
     * @param parentMethodGraph
     * @param parentInvokeInstr 
     */
    private void importRefAndAvoidGraph(VirtualThread vthread,
            MethodCallInfo methodCallInfo, InstrGraphNode parentMethodGraph,
            GCInstruction parentInvokeInstr) {
        Oracle oracle = Oracle.getInstanceOf();
        String methodString = oracle.getMethodOrFieldString(methodCallInfo.getMethod());
        HashSet<TTReference> refUsedInGraph = refsOfGraphNotCreated.get(methodString);
        /**
         * check if the cache already have references for a given method.
         * If yes then avoid this function.
         */
        if (refUsedInGraph == null) {
            refUsedInGraph = new HashSet<TTReference>();
            /**
             * Step 0: record the method name and parameters. So that same method
             * is processed only once.
             * 
             * Step 1: 
             * Go to each instruction of the method and collect all the references
             * of method instructions. Furthermore, collect all the instruction
             * which can start a new method in a stack.
             * 
             * Step 2: 
             * Pop from the stack (see step 1) and start a new method. 
             * Check if the method is already processed. If no then go to step 1
             * with that method.
             * 
             */
            Stack<MethodCallInfo> methodCallStack = new Stack<MethodCallInfo>();
            methodCallStack.push(methodCallInfo);
            HashSet<MethodCallInfo> alreadyVisitedMethodCalls = new HashSet<MethodCallInfo>();
            while (!methodCallStack.empty()) {
                MethodCallInfo currentInfo = methodCallStack.pop();
                if (alreadyVisitedMethodCalls.contains(currentInfo)) {
                    continue;
                } else {
                    alreadyVisitedMethodCalls.add(currentInfo);
                }
                refUsedInGraph.addAll(currentInfo.getAllReferencesUsedOnMeOnly());
                methodCallStack.addAll(currentInfo.getAllChildren());
            }
        }
        InstrGraphNode invokeInstrGraph = parentMethodGraph.findGraphNodeWithFromInstru(parentInvokeInstr);
        /**
         * The following instruction adds references of a sub-graph
         * on a given node.
         * The following instruction also apply filter to remove references
         * not used by the given algorithm.
         */
        invokeInstrGraph.addReferencesAfterFilter(refUsedInGraph);

    }

    private void workBeforeMethodIsAddedInInterMethodGraph(MethodCallInfo flowNode,
            InstrGraphNode methodGraph) {
    }

    private void workAfterMethodIsAddedInInterMethodGraph(MethodCallInfo flowNode,
            InstrGraphNode methodGraph, VirtualThread vThread) {
        //Comment temp 1 TemplateCreaterAndRetreiver.getInstanceOf().createTemplate(flowNode, methodGraph, vThread);
    }
}
