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
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import java.util.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.OGI.PNR.ChildNodeToRemove;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.vm.autoGenerated.forExceptionPrettyPrint.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CreateInterMethodLinks {

    private static final CreateInterMethodLinks myObj = new CreateInterMethodLinks();
    private static HashMap<InstrGraphNode, InstrGraphNode> invokeInstrOriginalChild = new HashMap<InstrGraphNode, InstrGraphNode>();
    private static MethodLevelGraphController methodLevelGraphContr = MethodLevelGraphController.getInstanceOf();
    private Vector<ChildNodeToRemove> graphLinksToBeRemoved = new Vector<ChildNodeToRemove>();

    /**
     *
     */
    private CreateInterMethodLinks() {
    }

    /**
     * 
     * @return
     */
    public static CreateInterMethodLinks getInstanceOf() {
        return myObj;
    }

    public GCInstruction getInvokeInstrOriginalChild(InstrGraphNode invokeInstr) {
        InstrGraphNode gNode = invokeInstrOriginalChild.get(invokeInstr);
        return gNode.getInstruction().getNormalInstrs().firstElement();
    }

    public InstrGraphNode getInvokeOriginalChild(InstrGraphNode nodeWithInvokeInstr) {
        GCInstruction invokeInstr = nodeWithInvokeInstr.getInstruction().getNormalInstrs().firstElement();
        Vector<Instruction> instrs = nodeWithInvokeInstr.getMethod().getInstructions();
        Iterator<Instruction> it = instrs.iterator();
        Instruction nextInstrFromInvoke = null;
        while (it.hasNext()) {
            Instruction instr = it.next();
            if (instr.getInstructionId() == invokeInstr.getInstructionId()) {
                nextInstrFromInvoke = it.next();
            }
        }
        Iterator<InstrGraphNode> childIt = nodeWithInvokeInstr.getChildren().iterator();
        while (childIt.hasNext()) {
            InstrGraphNode childNode = childIt.next();
            GCInstruction childInstr = childNode.getInstruction().getNormalInstrs().firstElement();
            if (childInstr.getInstructionId() == nextInstrFromInvoke.getInstructionId()) {
                return childNode;
            }
        }
        Miscellaneous.printlnErr("Error # 3323 " + invokeInstr + ", line number ="
                + LineNumberController.getInstanceOf().getLineNumberInfo(invokeInstr.getInstructionId()));
        PNRAlgo.debugOn();
        Miscellaneous.printlnErr(nodeWithInvokeInstr);
        Miscellaneous.exit();
        return null;
    }

    public void execute(InstrGraphNode fromGraph, InstrGraphNode toGraph,
            GCInstruction fromInstr, boolean createMethodLevelGraph) {
        boolean printGraph = false;
        Oracle oracle = Oracle.getInstanceOf();

        if (oracle.getMethodOrFieldString(fromGraph.getMethod()).contains("java.lang.StringBuilder.append(Ljava/lang/String;)Ljava/lang/StringBuilder;")
                || oracle.getMethodOrFieldString(toGraph.getMethod()).contains("Main.<init>sfas")
                || printGraph) {
            //IntraMethodAlgorithm.shouldDebugPrint = true;
            PNRAlgo.debug("Stop here " + oracle.getMethodOrFieldString(fromGraph.getMethod())
                    + "---" + oracle.getMethodOrFieldString(toGraph.getMethod()) + "\n\n");
            PNRAlgo.debug("toGraph = " + toGraph);
            PNRAlgo.debug("fromGraph = " + fromGraph);
            printGraph = true;
            //printGraph = true;
        }
        /**
         * find the from instruction (invoke-instruction) in the fromGraph.
         */
        InstrGraphNode nodeWithInvokeInstr = fromGraph.findGraphNodeWithFromInstru(fromInstr);


        if (nodeWithInvokeInstr == null) {
            Miscellaneous.printlnErr("Cannot find node with invoke instruction ");
            PNRAlgo.debugOn();
            Miscellaneous.printlnErr(oracle.getMethodOrFieldString(fromGraph.getMethod()) + ", "
                    + oracle.getMethodOrFieldString(toGraph.getMethod()));
            Miscellaneous.printlnErr(fromInstr + "\n\n\n");
            Miscellaneous.printlnErr(fromGraph.getMethod().getInstructions());
            Miscellaneous.printlnErr(fromGraph);
            fromGraph.findGraphNodeWithFromInstru(fromInstr);
            Miscellaneous.exit();
        }

        /**
         * find all the return instructions in the method invoked.
         */
        HashSet<InstrGraphNode> retInstrSet = toGraph.findMethodReturnInstructions();

        PNRAlgo.debug("ret graph node set " + retInstrSet);

        InstrGraphNode oldChildOfInvoke = invokeInstrOriginalChild.get(nodeWithInvokeInstr);
        if (oldChildOfInvoke == null) {
            oldChildOfInvoke = getInvokeOriginalChild(nodeWithInvokeInstr);
            invokeInstrOriginalChild.put(nodeWithInvokeInstr, oldChildOfInvoke);
            graphLinksToBeRemoved.add(new ChildNodeToRemove(nodeWithInvokeInstr, oldChildOfInvoke));
            //nodeWithInvokeInstr.removeChildFully(oldChildOfInvoke);
        }
        /**
         * create Link between invoke and the first instruction of the method being invoked.
         */
        addChild(nodeWithInvokeInstr, toGraph, createMethodLevelGraph);

        /**
         * Now connect all those ret-Instr with the old child of invoke.
         */
        Iterator<InstrGraphNode> retInstrIt = retInstrSet.iterator();
        while (retInstrIt.hasNext()) {
            InstrGraphNode aRetInstr = retInstrIt.next();
            PNRAlgo.debug(" add in " + aRetInstr + "  a child " + oldChildOfInvoke);
            //addChild(aRetInstr, oldChildOfInvoke);
            aRetInstr.addChild(oldChildOfInvoke);
        }
        if (printGraph) {
            PNRAlgo.debug("To graph  =" + toGraph + "\n\n\n");
            PNRAlgo.debug("graph after transformation =" + fromGraph);
        }
    }

    public void removeAllChildNodesGeneratedFromInvoke() {
        Iterator<ChildNodeToRemove> it = graphLinksToBeRemoved.iterator();
        while (it.hasNext()) {
            it.next().removeTheChild();
        }
    }

    private void addChild(InstrGraphNode parentNode, InstrGraphNode childNode,
            boolean createMethodLevelGraph) {
        parentNode.addChild(childNode);
        if (createMethodLevelGraph) {
            MethodLevelGraphNode mlnParent = methodLevelGraphContr.getRecord(parentNode.getIntraMethodGraphID());
            MethodLevelGraphNode mlnChild = methodLevelGraphContr.getRecord(childNode.getIntraMethodGraphID());
            mlnParent.addChild(new MethodLevelChildInfo(mlnChild, parentNode));
            //methodLevelGraphContr.clearPrintCache();
            //Miscellaneous.println("\n\n\n ß ---"+methodLevelGraphContr);
        }
    }
}
