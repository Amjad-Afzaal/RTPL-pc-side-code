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
package takatuka.verifier.dataObjs;

import takatuka.optimizer.VSS.logic.preCodeTravers.ReduceTheSizeOfLocalVariables;
import java.util.HashSet;
import org.apache.commons.lang.builder.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class Type implements TypeConstants {

    protected int type = 0;
    protected boolean isReference = false;
    protected boolean isArray = false;
    //public int arraySize = 0;
    public Object value = null;

    /**
     *
     * @param type
     * @param isReference
     * @param newId
     */
    public Type(int type, boolean isReference, int newId) {
        this.type = type;
        this.isReference = isReference;
    }

    public Type(boolean isReference) {
        this.isReference = isReference;
    }

    /**
     * create a type which is not reference and not array. The reference type is
     * set to VOID
     */
    public Type() {
        this.type = Type.VOID;
    }

    public Type(int type) {
        this.type = type;
    }

    @Override
    public Object clone() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type ret = frameFactory.createType();
        ret.type = type;
        ret.isReference = isReference;
        ret.isArray = isArray;
        ret.value = value;
        return ret;
    }

    public boolean isReference() {
        return isReference;
    }

    /**
     * returns single type if not reference. Otherwise returns 0
     * @return
     */
    public int getType() {
        return type;
    }

    public void setType(int type, boolean isReference) {
        this.type = type;
        this.isReference = isReference;
    }

    public int setReferenceType(String name, int refIndex) {
        int cur = refIndex;
        int start = cur + 1;
        String refName = name.substring(start, name.indexOf(";", start));
        int localType = Oracle.getInstanceOf().getClassInfoByName(refName);
        this.setType(localType, true);
        return name.indexOf(";", start);
    }

    /**
     * int, float, char, short, byte, boolean, are four byte original values 
     * @return
     */
    public boolean isOriginalFourByteLiteralValue() {
        if (isReference) {
            return false;
        } else if (type == Type.INTEGER || type == Type.SHORT
                || type == Type.BOOLEAN || type == Type.BYTE_BOOLEAN
                || type == Type.CHAR || type == Type.BYTE ||
                type == Type.FLOAT) {
            return true;
        }
        return false;

    }
    public boolean isIntOrShortOrByteOrBooleanOrCharType() {
        if (isReference) {
            return false;
        } else if (type == Type.INTEGER || type == Type.SHORT
                || type == Type.BOOLEAN || type == Type.BYTE_BOOLEAN || type == Type.CHAR || type == Type.BYTE) {
            return true;
        }
        return false;
    }

    public boolean isArrayReference() {
        return (isArray && isReference);
    }

    public void setIsArray() {
        this.isArray = true;
        //----- to check again following.
        this.isReference = true;
    }

    public static boolean isCompatableTypes(Type typeOne, Type typeTwo) {
        if (typeOne.isReference() && typeTwo.isReference()) {
            return true;
        } else if (typeOne.isReference() != typeTwo.isReference()) {
            return false;
        }
        int blockSize1 = typeOne.getBlocks();
        int blockSize2 = typeTwo.getBlocks();
        if (typeOne.isDoubleOrLong() && typeTwo.isDoubleOrLong()) {
            return true;
        } else if (typeOne.isOriginalFourByteLiteralValue()
                && typeTwo.isOriginalFourByteLiteralValue()) {
            return true;
        } else if (blockSize1 == blockSize2) {
            return true;
        }
        return false;
    }

    public boolean isDoubleOrLong() {
        return !isReference && isDoubleOrLong(this.type);
    }

    /**
     * 
     * @param type
     * @return
     */
    public static boolean isDoubleOrLong(int type) {
        if (type == Type.DOUBLE || type == Type.LONG) {
            return true;
        }
        return false;
    }

    /**
     * The number of blocks taken by a type
     */
    public int getBlocks() {
        return getBlocks(this.type, this.isReference);
    }

    /**
     *
     * @param typeInput
     * @param isReference
     * @return the number of blocks taken by a type.
     */
    public static int getBlocks(int typeInput, boolean isReference) {
        int minBlockSize = ReduceTheSizeOfLocalVariables.BLOCK_SIZE_IN_BYTES;
        if (minBlockSize > 4) {
            Miscellaneous.printlnErr("Wrong block size of stack/local variables."
                    + " Allowed values are 1, 2, 4");
            System.exit(1);
        }
        if (isReference) {
            return minBlockSize <= 2 ? 2 / minBlockSize : 1;
        } else if (typeInput == Type.INTEGER || typeInput == FLOAT ||
                typeInput == SPECIAL_TAIL) {
            return 4 / minBlockSize;
        } else if (typeInput == Type.DOUBLE || typeInput == Type.LONG) {
            return 8 / minBlockSize;
        } else if (typeInput == Type.SHORT || typeInput == Type.CHAR) {
            return minBlockSize <= 2 ? 2 / minBlockSize : 1;
        } else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(isReference).append(type).
                toHashCode();
    }

    /**
     *
     * @param obj Object
     * @return boolean
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Type)) {
            return false;
        }
        Type varObj = (Type) obj;
        if (varObj.isReference != isReference || varObj.type != type) {
            return false;
        }
        return true;
    }

    public static String typeToString(int type) {
        if (type == INTEGER) {
            return "INTEGER";
        } else if (type == BOOLEAN) {
            return "BOOLEAN";
        } else if (type == BYTE) {
            return "BYTE";
        } else if (type == BYTE_BOOLEAN) {
            return "BYTE_BOOLEAN";
        } else if (type == CHAR) {
            return "CHAR";
        } else if (type == DOUBLE) {
            return "DOUBLE";
        } else if (type == SPECIAL_TAIL) {
            return "SPECIAL_TAIL";
        } else if (type == FLOAT) {
            return "FLOAT";
        } else if (type == LONG) {
            return "LONG";
        } else if (type == NULL) {
            return "NULL";
        } else if (type == RETURN_ADDRESS) {
            return "RETURN_ADDRESS";
        } else if (type == SHORT) {
            return "SHORT";
        } else if (type == STRING) {
            return "STRING";
        } else if (type == UNUSED) {
            return "UNUSED";
        } else if (type == VOID) {
            return "VOID";
        } else {
            return "" + type;
        }
    }

    /**
     *
     * @param obj Object
     * @return boolean
     */
    public boolean equalsLight(Object obj) {
        if (obj == null || !(obj instanceof Type)) {
            return false;
        }
        Type varObj = (Type) obj;
        if (varObj.isReference != isReference
                || (!isReference && varObj.type != type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "(" + isReference + "," + (!isReference ? typeToString(type) : type) + "," + value + ")";
    }

    public HashSet<Integer> getRefClassThisPtr() {
        HashSet<Integer> ret = new HashSet<Integer>();
        ret.add(type);
        return ret;
    }
}
