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
package takatuka.offlineGC.DFA.dataObjs;

import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.offlineGC.DFA.logic.factory.NewInstrIdFactory;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.dataObjs.Type;
import takatuka.vm.autoGenerated.forExceptionPrettyPrint.LineNumberController;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class TTReference implements Comparable<TTReference> {

    private int classId = -1;
    private int newId = -1;
    private static final Oracle oracle = Oracle.getInstanceOf();
    private static final LineNumberController lineNumberContr = LineNumberController.getInstanceOf();

    public static TTReference createNULLReference() {
        return new TTReference(Type.NULL, -1);
    }

    public boolean isNullReference() {
        if (classId == Type.NULL) {
            return true;
        }
        return false;
    }

    @Override
    public Object clone() {
        throw new UnsupportedOperationException();
    }

    /**
     *
     * @param classId: tell the class whose reference it is
     * @param newId : tell the new instruction that has created it.
     */
    public TTReference(int classId, int newId) {
        this.classId = classId;
        if (newId > 0) {
            this.newId = newId;
        }
    }



    /**
     * newId is a unique id coressponds to a new instruction apearing in a method at
     * a specific location.
     * @return
     */
    public int getNewId() {
        return newId;
    }

    /**
     * 
     * @param newId
     * @return
     */
    public void setNewId(int newId) {
        this.newId = newId;
    }

    /**
     * this pointer of the class of the reference.
     * @return
     */
    public int getClassThisPointer() {
        return classId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TTReference)) {
            return false;
        }
        TTReference inputObj = (TTReference) obj;
        if (inputObj.classId == classId && inputObj.newId == newId) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + this.classId;
        hash = 83 * hash + this.newId;
        return hash;
    }

    private int compare(TTReference o1, TTReference o2) {
        int comp = new Integer(o1.classId).compareTo(o2.classId);
        if (comp == 0) {
            comp = new Integer(o1.newId).compareTo(o2.newId);
        }
        return comp;
    }

    @Override
    public String toString() {
        String ret = "(" + classId + "," + newId;
        if (newId > 0) {
            GCInstruction instr = (GCInstruction) NewInstrIdFactory.getInstanceOf().
                    getInstrANewIdAssignedTo(newId);
            String methodStr = oracle.getMethodOrFieldString(instr.getMethod());
            int lineNumber = lineNumberContr.getLineNumberInfo(instr);
            ret += ", line#=" + lineNumber + ", at=" + methodStr;
        }
        ret += ")";
        return ret;
    }

    @Override
    public int compareTo(TTReference input) {
        return compare(this, input);
    }
}
