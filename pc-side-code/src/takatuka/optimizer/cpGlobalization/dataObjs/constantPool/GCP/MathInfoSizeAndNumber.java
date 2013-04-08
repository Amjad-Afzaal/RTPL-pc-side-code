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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class MathInfoSizeAndNumber {
    private GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                           getFactory();

    private int tag = 0;
    private int size = 0;
    private int numberOfElements = 0;
    private int startIndex = 0;


    public MathInfoSizeAndNumber(int tag, int size) {
        this.tag = tag;
        this.size = size;
    }

    public MathInfoSizeAndNumber(int tag, int size, int startIndex) {
        this(tag, size);
        this.startIndex = startIndex;
    }

    public MathInfoSizeAndNumber(int tag, int size, int numberOfElements,
                                 int startIndex) {
        this(tag, size);
        this.startIndex = startIndex;
        this.numberOfElements = numberOfElements;

    }

    public int getTag() {
        return tag;
    }

    public int getSize() {
        return size;
    }

    public int getNumberOfElements() {
        return numberOfElements;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setNumberOfElements(int numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MathInfoSizeAndNumber)) {
            return false;
        }
        MathInfoSizeAndNumber mathInfo = (MathInfoSizeAndNumber) obj;
        if (mathInfo.tag == tag && mathInfo.size == size) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tag).append(size).toHashCode();
    }

    public static String tagToString(int tag) {
        if (tag == TagValues.CONSTANT_Double) {
            return "Double";
        } else if (tag == TagValues.CONSTANT_Float) {
            return "Float";
        } else if (tag == TagValues.CONSTANT_Integer) {
            return "Integer";
        } else if (tag == TagValues.CONSTANT_Long) {
            return "Long";
        }
        return null; //should never come here.
    }


}
