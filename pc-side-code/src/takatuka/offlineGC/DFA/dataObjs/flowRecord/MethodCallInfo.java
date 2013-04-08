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
import takatuka.offlineGC.DFA.dataObjs.functionState.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.offlineGC.DFA.logic.partialInstrOrder.FSKAsHashKeyInterface;
import takatuka.verifier.dataObjs.attribute.*;

/**
 * 
 * Description:
 * <p>
 * A node in the tree(s) of functions flow.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodCallInfo extends FunctionStateKey {

    private static Vector alreadyPrinted = new Vector();
    private TreeMap<Long, HashSet<MethodCallInfo>> childFunFlowNodes = new TreeMap<Long, HashSet<MethodCallInfo>>();
    private int connectedComponentID = -1;
    private boolean isRecursive = false;
    private HashSet referencesUsedOnMe = null;
    public boolean DFSTransposeVisited = false;
    /**
     * Function A is called by what instructions.
     * value = HashMap<Long, VerificationInstruction>
     */
    private static HashMap<FunctionStateKey, FunctionStateKeyMapValue> funAisCalledByMap = new HashMap<FunctionStateKey, FunctionStateKeyMapValue>();
    /**
     * Use only when populating method calls per invoke instruction
     */
    public int visitedBefore = 0;
    public static int vistId = 1; 
    
    public int visitedBefore2 = 0;
    public static int visitId2 = 1;

    /**
     * 
     * @param parentMethod
     * @param parentMethodParm
     */
    public MethodCallInfo(MethodInfo parentMethod, Vector parentMethodParm) {
        super(parentMethod, parentMethodParm);
    }

    public static void updateHashMapUsingFSK() {
        FunctionStateKeyMapValue.update(funAisCalledByMap);
    }

    /**
     * 
     * @return
     */
    public Set<Long> getAllChildrenCorrespondingInvokeInstrId() {
        return childFunFlowNodes.keySet();
    }

    /**
     * 
     * @return
     * all references used on current node and does not include references used on the 
     * child node of the current node. 
     *
     * @param node
     * @return
     */
    public HashSet<TTReference> getAllReferencesUsedOnMeOnly() {
        if (referencesUsedOnMe != null) {
            return (HashSet<TTReference>) referencesUsedOnMe.clone();
        }
        referencesUsedOnMe = new HashSet();
        HashMap<Long, FunctionStateValueElement> stateRecord = FunctionStateRecorder.getInstanceOf().getFunctionState(this);
        Vector<Instruction> instrsVec = getMethod().getInstructions();
        for (int loop = 0; loop < instrsVec.size(); loop++) {
            GCInstruction instr = (GCInstruction) instrsVec.elementAt(loop);
            FunctionStateValueElement stateElm = stateRecord.get(instr.getInstructionId());
            referencesUsedOnMe.addAll(stateElm.getAllReferenceUsed());
        }
        return (HashSet<TTReference>) referencesUsedOnMe.clone();
    }

    /**
     * 
     * @return
     * true if the function call is recursive
     */
    public boolean isRecursive() {
        return isRecursive;
    }

    /**
     * 
     * @param isRec
     */
    public void setRecursive(boolean isRec) {
        this.isRecursive = isRec;
    }

    /**
     * 
     * @return
     */
    public int getStronglyConnectedCompId() {
        return connectedComponentID;
    }

    /**
     * 
     * @param id
     */
    public void setStronglyConnectedCompId(int id) {
        this.connectedComponentID = id;
    }

    /**
     * 
     * @param invokeInstrId
     * @return
     * children nodes from a given invoke instruction index
     */
    public Set<MethodCallInfo> getChildren(long invokeInstrId) {
        Set<MethodCallInfo> ret =  childFunFlowNodes.get(invokeInstrId);
        if (ret == null) {
            ret = new TreeSet<MethodCallInfo>();
        }
        return ret;
    }

    /**
     * 
     * @return
     */
    public HashSet<MethodCallInfo> getAllChildren() {
        Collection<HashSet<MethodCallInfo>> allTheMethodCallCollection = childFunFlowNodes.values();
        Iterator<HashSet<MethodCallInfo>> it = allTheMethodCallCollection.iterator();
        HashSet<MethodCallInfo> ret = new HashSet<MethodCallInfo>();
        while (it.hasNext()) {
            HashSet<MethodCallInfo> set = it.next();
            ret.addAll(set);
        }
        return ret;
    }

    /**
     * 
     * @param instr
     * a invoke instruction (like INVOKESTATIC, INVOKEVIRTUAL, INVOKESPECIAL)
     * @return
     * children nodes from a given invoke instruction index
     */
    public Set<MethodCallInfo> getChild(VerificationInstruction instr) {
        return getChildren(instr.getInstructionId());
    }

    /**
     * What invoke instruction has invoked what function.
     * @param invokeIndex
     * @param child
     */
    public void addChildNode(VerificationInstruction currentInvokeInstr, MethodCallInfo child) {
        HashSet childSet = childFunFlowNodes.get(currentInvokeInstr.getInstructionId());
        if (childSet == null) {
            childSet = new HashSet();
            childFunFlowNodes.put(currentInvokeInstr.getInstructionId(), childSet);
        }
        childSet.add(child);
        addParentNode(currentInvokeInstr, child);
    }

    /**
     * a function A is invoked by which set of instructions.
     * @param currentInvokeInstr
     * @param child
     */
    private static void addParentNode(VerificationInstruction currentInvokeInstr, MethodCallInfo child) {
        FunctionStateKeyMapValue specialValue = funAisCalledByMap.get(child);
        HashMap<Long, VerificationInstruction> value = null;
        if (specialValue == null) {
            value = new HashMap<Long, VerificationInstruction>();
            specialValue = new FunctionStateKeyMapValue(child, value);
            funAisCalledByMap.put(child, specialValue);
        } else {
            value = (HashMap<Long, VerificationInstruction>) specialValue.getValue();
        }
        value.put(currentInvokeInstr.getInstructionId(), currentInvokeInstr);
    }

    /**
     * Function A is invoked by which set of instructions.
     * @return
     */
    public static Collection<VerificationInstruction> getFunAInvokedBy(FunctionStateKey funA) {
        FunctionStateKeyMapValue specialValue = funAisCalledByMap.get(funA);
        HashMap<Long, VerificationInstruction> value = (HashMap<Long, VerificationInstruction>) specialValue.getValue();
        return value.values();
    }

    /**
     * only for debugging
     * @return
     * the method name of the method represented by the node.
     */
    public String toStringMeOnly() {
        return super.toString();
    }

    public static void callMeBeforeEachToStringCallFromOutside() {
        alreadyPrinted.clear();
    }

    private String childrenToString(HashSet<MethodCallInfo> childrenSet) {
        String ret = "";
        Iterator<MethodCallInfo> childIt = childrenSet.iterator();
        while (childIt.hasNext()) {
            MethodCallInfo flowNode = childIt.next();
            ret = ret = ret + "[" + flowNode.toStringMeOnly() + ", isRec=" + flowNode.isRecursive() + "]";
            if (childIt.hasNext()) {
                ret = ret + ", ";
            }

        }
        return ret;
    }

    @Override
    public String toString() {
        String ret = "" + super.toString();
        /*
        boolean iAmPrinted = alreadyPrinted.contains(this);
        Iterator<HashSet<FunctionFlowNode>> childIterator = childFunFlowNodes.values().iterator();
        if (!childIterator.hasNext() || iAmPrinted) {            
        ret = ret = ret + "{" + super.toString() + "}";
        return ret;
        }
        alreadyPrinted.add(this);
        while (childIterator.hasNext()) {
        HashSet<FunctionFlowNode> childrenSet = childIterator.next();
        ret = ret + "{" + super.toString() + ", isRecursive=" + isRecursive + "}------>";
        ret = ret + childNode.toString() + "\n";
        }*/
        return ret;
    }
}
