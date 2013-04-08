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

import java.util.*;

import org.apache.commons.lang.builder.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * It is the super class for OperandStack and LocalVariables
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public abstract class FrameElement {

    protected int maxSize = 0;
    protected Vector elements = null;

    public FrameElement() {
        super();
    }

    public Vector getAll() {
        return (Vector) elements.clone();
    }

    public abstract Type set(int index, Type type);

    public abstract Type get(int index);

    public FrameElement(int maxSize) {
        this.maxSize = maxSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void clear() {
        elements.removeAllElements();
    }

    public int getCurrentSize() {
        return elements.size();
    }

    public static Vector createEntry(Type type) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Vector<Type> entry = new Vector<Type>();
        entry.addElement(type);
        for (int loop = 0; loop < type.getBlocks() - 1; loop++) {
            entry.addElement(frameFactory.createType(Type.SPECIAL_TAIL));
        }
        return entry;
    }

    @Override
    public abstract Object clone();

    /**
     * merge two FrameElements (e.g. two OperandStacks) and return true
     * if something has changed after merge. It take cares of the things mentioned
     * in Section 4.9.2 at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#9801
     * @param frameElm FrameElement
     * @return boolean
     */
    public abstract boolean merge(FrameElement frameElm);

    /**
     * merge the references based on the documentations
     * 
     * @param mineType: present type
     * @param inputType: new Type
     * @return: true if merged otherwise return false
     * @throws java.lang.Exception
     */
    public Type mergeReferences(Type mineType, Type inputType) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        if (!mineType.isReference || !inputType.isReference
                || (inputType.type == mineType.type)) {
            return null;
        }
        int newType = Oracle.getInstanceOf().getClass("java/lang/Object").getThisClass().intValueUnsigned();
        /*Oracle.getInstanceOf().getNearestCommonSuperClass(
        mineType.type, inputType.type);*/
        if (newType != mineType.type) {
            Type newTypeObj = frameFactory.createType((byte) newType);
            if (mineType.isArray) {
                newTypeObj.isArray = true;
            }
            newTypeObj.isReference = true;
            return newTypeObj;
        }
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null && !(obj instanceof FrameElement)) {
            return false;
        }
        FrameElement fElm = (FrameElement) obj;
        if (fElm.maxSize == maxSize && elements.equals(fElm.elements)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(maxSize).append(elements).
                toHashCode();
    }

    @Override
    public String toString() {
        String ret = "max-size =" + maxSize
                + ", current-size =" + elements.size()
                + ", Elements={";
        for (int loop = 0; loop < elements.size(); loop++) {
            if (loop != 0) {
                ret = ret + ", ";
            }
            ret = ret + loop + ":" + elements.elementAt(loop);
        }
        ret = ret + "}";
        return ret;
    }
}
