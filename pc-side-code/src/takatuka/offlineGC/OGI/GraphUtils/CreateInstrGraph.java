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
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.offlineGC.DFA.dataObjs.flowRecord.*;
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.offlineGC.OGI.factory.*;
import takatuka.offlineGC.OGI.superInstruction.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateInstrGraph {

    private static final CreateInstrGraph myObj = new CreateInstrGraph();
    private static int intraMethodGraphId = 0;

    /**
     * 
     */
    protected CreateInstrGraph() {
    }

    /**
     *
     * @return
     */
    public static CreateInstrGraph getInstanceOf() {
        return myObj;
    }

    public InstrGraphNode createInstrGraph(MethodCallInfo flowNode,
            VirtualThread vThread, boolean shouldCreateMethodLevelGraph) {
        MethodInfo method = flowNode.getMethod();
        HashMap<Long, FunctionStateValueElement> functionState = FunctionStateRecorder.getInstanceOf().getFunctionState(flowNode);
        Oracle oracle = Oracle.getInstanceOf();
        InstrGraphNode gNode = createInstrGraph(flowNode,
                functionState, SuperInstrsController.getInstanceOf(), vThread,
                shouldCreateMethodLevelGraph);
        return gNode;

    }

    public InstrGraphNode createInstrGraph(MethodCallInfo callInfo,
            HashMap<Long, FunctionStateValueElement> functionState,
            SuperInstrsController superInstrContr, VirtualThread thread,
            boolean shouldCreateMethodLevelNodes) {
        MethodInfo method = callInfo.getMethod();
        SuperInstruction superInstr = superInstrContr.getSuperInstrForMethod(method);
        if (superInstr == null) {
            return null;
        }

        HashMap<SuperInstruction, InstrGraphNode> alreadyCreatedNodes =
                new HashMap<SuperInstruction, InstrGraphNode>();
        SuperInstruction sourceInstr = superInstr;
        IFactory factory = FactoryPlaceHolder.getInstanceOf().getCurrentFactory();
        Stack<InstrAndParent> instrStack = new Stack<InstrAndParent>();
        instrStack.push(new InstrAndParent(sourceInstr, null));
        InstrGraphNode sourceToReturn = null;
        Oracle oracle = Oracle.getInstanceOf();
        intraMethodGraphId++;
        //Miscellaneous.println("method instructions ="+method.getInstructions());
        while (!instrStack.empty()) {
            InstrAndParent instrAndParent = instrStack.pop();
            SuperInstruction instr = instrAndParent.currentInstr;
            InstrGraphNode parentGraph = instrAndParent.parentGraph;
            PNRAlgo.debug(" method =", Oracle.getInstanceOf().getMethodOrFieldString(method));
            if (Oracle.getInstanceOf().getMethodOrFieldString(method).contains("fooNew")) {
                // Miscellaneous.println("Stop here 1245");
            }
            InstrGraphNode currentGNode = null;
            if (alreadyCreatedNodes.get(instr) != null) {
                currentGNode = alreadyCreatedNodes.get(instr);
                parentGraph.addChild(currentGNode);
                continue;
            }
            currentGNode = factory.createGraphNode(instr, method, intraMethodGraphId, callInfo);
            if (sourceToReturn == null) {
                sourceToReturn = currentGNode;
                if (shouldCreateMethodLevelNodes) {
                    MethodLevelGraphController.getInstanceOf().
                            addRecord(new MethodLevelGraphNode(intraMethodGraphId, method),
                            thread);
                }
            }
            if (parentGraph != null) {
                parentGraph.addChild(currentGNode);
            }
            PNRAlgo.debug(" creating graph " + instr.printMe());
            alreadyCreatedNodes.put(instr, currentGNode);

            currentGNode.setReferencesUsed(functionState);
            HashSet<SuperInstruction> nextInstrSet = instr.getNextSuperInstrs();
            //IntraMethodAlgorithm.debug("\t\t next instr =  " + nextInstrSet);
            Iterator<SuperInstruction> it = nextInstrSet.iterator();
            while (it.hasNext()) {
                SuperInstruction nextInstr = it.next();
                instrStack.push(new InstrAndParent(nextInstr, currentGNode));
            }
        }
        return sourceToReturn;
    }

    private class InstrAndParent {

        public SuperInstruction currentInstr = null;
        public InstrGraphNode parentGraph = null;

        public InstrAndParent(SuperInstruction currentInstr, InstrGraphNode parentGraph) {
            this.currentInstr = currentInstr;
            this.parentGraph = parentGraph;
        }
    }
}
