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
package takatuka.offlineGC.DFA.dataObjs.attribute;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.CodeAtt;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCInstruction extends VerificationInstruction implements Comparable<GCInstruction> {

    /**
     * New Id to ids of instruction where corresponding free instruction are
     * added for that newId.
     */
    private static HashMap<Integer, HashSet<Long>> recordOfFreeInstrPerNewId = new HashMap<Integer, HashSet<Long>>();
    private GCOperandStack leavingStack = null;
    private GCLocalVariables leavingLV = null;
    private static HashMap<Integer, HashSet<FreeInstrRecord>> referenceFreeOnMeGlobal = new HashMap<Integer, HashSet<FreeInstrRecord>>();
    private HashSet<TTReference> referenceFreeOnMeLocal = new HashSet<TTReference>();
    public static final int PNR = 1;
    public static final int FTT = 2;
    public static final int DAU = 3;
    public static final int DAU_PNR = 4;
    public static final int FTT_PNR = 5;
    public static final int ALGO_START_NUM = 1;
    public static final int ALGO_END_NUM = 5;
    /**
     * All the instructions of a method that are part of a strongly connected 
     * component has same stronglyConnectedID. Two instruction from different
     * stronglyConnected component of a method has two different
     * stronglyConnnectedIDs. 
     */
    private long stronglyConnectedID = -1;
    private boolean stronglyConnectedIDRepresentALoop = false;
    public boolean isIdChanged = false;

    /**
     * 
     * @param opcode
     * @param operands
     */
    public GCInstruction(int opcode, Un operands, CodeAtt codeAtt) {
        super(opcode, operands, codeAtt);
    }

    public static HashMap<Integer, HashSet<Long>> getRecordOfFreeInstrPerNewId() {
        return recordOfFreeInstrPerNewId;
    }

    public static HashMap<Integer, HashSet<FreeInstrRecord>> getRecordOfFreeInstrPerAlgo() {
        return referenceFreeOnMeGlobal;
    }


    public void setStronglyConnectedID(long stronglyConnectedID, boolean ifRepresentALoop) {
        this.stronglyConnectedID = stronglyConnectedID;
        stronglyConnectedIDRepresentALoop = ifRepresentALoop;
    }

    public boolean isInstrInAnIntraMethodLoop() {
        return stronglyConnectedIDRepresentALoop;
    }

    public long getStronglyConnectedID() {
        return this.stronglyConnectedID;
    }

    public static int numberOfRefFreeByAlgo(int Algo) {
        HashSet set = referenceFreeOnMeGlobal.get(Algo);
        if (set != null) {
            return set.size();
        } else {
            return 0;
        }
    }

    public boolean removeReferenceFreedOnMe(TTReference reference) {
        FreeInstrRecord toFind = new FreeInstrRecord(this, reference);
        for (int algoNum = ALGO_START_NUM; algoNum < ALGO_END_NUM; algoNum++) {
            HashSet<FreeInstrRecord> freeRecord = referenceFreeOnMeGlobal.get(algoNum);
            if (freeRecord!= null && freeRecord.remove(toFind)) {
                //break;
            }
        }
        return referenceFreeOnMeLocal.remove(reference);
    }

    public boolean addReferenceFreedOnMe(TTReference reference, int algorithm) {
        HashSet<TTReference> refSet = new HashSet<TTReference>();
        refSet.add(reference);
        return addReferencesFreedOnMe(refSet, algorithm);
    }

    public boolean addReferenceFreeOnMe(int algorithm, TTReference ref) {
        HashSet<FreeInstrRecord> refSet = referenceFreeOnMeGlobal.get(algorithm);
        if (refSet == null) {
            refSet = new HashSet<FreeInstrRecord>();
            referenceFreeOnMeGlobal.put(algorithm, refSet);
        }
        if (referenceFreeOnMeLocal.contains(ref)) {
            return false;
        } else {
            refSet.add(new FreeInstrRecord(this, ref));
            return referenceFreeOnMeLocal.add(ref);
        }
    }

    /**
     * @param references
     * @return true if the references are newly added and was not there before.
     */
    public boolean addReferencesFreedOnMe(HashSet<TTReference> references, int algorithm) {
        //if (referenceFreeOnMe.size() >= 20) {
        /**
         * Not allowed to add more than 20 request on a same instruction.
         */
        //  return false;
        //}
        Iterator<TTReference> refIt = references.iterator();
        HashSet<TTReference> toRemove = new HashSet<TTReference>();
        boolean isAdded = false;
        while (refIt.hasNext()) {
            TTReference ref = refIt.next();
            int newId = ref.getNewId();
            if (newId < 0 || newId > 255) {
                toRemove.add(ref);
                continue;
            }
            addNewIdFreeInstrRecord(ref);
            boolean temp = addReferenceFreeOnMe(algorithm, ref);
            if (temp) {
                isAdded = true;
                //System.out.println("Reference freed  =" + ref + ", instruction " + this);
            }
        }
        references.removeAll(toRemove);
        return isAdded;
    }

    private void addNewIdFreeInstrRecord(TTReference ref) {
        int newId = ref.getNewId();
        HashSet<Long> instrsWithFreeInstr = recordOfFreeInstrPerNewId.get(newId);
        if (instrsWithFreeInstr == null) {
            instrsWithFreeInstr = new HashSet<Long>();
            recordOfFreeInstrPerNewId.put(newId, instrsWithFreeInstr);
        }
        instrsWithFreeInstr.add(instructionId);
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> getReferencesFreedOnMe() {
        return referenceFreeOnMeLocal;
    }

    /**
     * save the state of method stack and local variables after execution of current
     * instruction.
     * 
     * @param leavingFrame
     */
    public void saveLeavingFrame(Frame leavingFrame) {
        /*if (leavingStack != null) {
        leavingStack.merge((GCOperandStack) leavingFrame.getOperandStack().clone());
        leavingLV.merge((GCLocalVariables) leavingFrame.getLocalVariables().clone());
        } else {*/
        leavingStack = (GCOperandStack) leavingFrame.getOperandStack().clone();
        leavingLV = (GCLocalVariables) leavingFrame.getLocalVariables().clone();
        /*}*/
    }

    /**
     * returns the instance of operand stack of the method after execution of this instruction.
     * @return
     */
    public GCOperandStack getLeavingOperandStack() {
        return leavingStack;
    }

    /**
     * returns the instance of local variables of the method after execution of this instruction.
     * @return
     */
    public GCLocalVariables getLeavingLocalVariables() {
        return leavingLV;
    }

    /**
     *
     * @param o
     * @return
     */
    public int compareTo(GCInstruction o) {
        return new Integer(getOffSet()).compareTo(o.getOffSet());
    }
}
