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
package takatuka.classreader.dataObjs.attribute;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Based on the structure defined at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 *   attribute_info {
 *      u2 attribute_name_index;
 *      u4 attribute_length;
 *      u1 info[attribute_length];
 *   }
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class AttributeInfo implements BaseObject {

    private Un attribute_name_index; //(2);
    private Un attribute_length; //(4);

    public AttributeInfo() {
    }

    public AttributeInfo(Un u2_attrNameIndex, Un u4_attributeLength) throws
            Exception {
        setAttributeNameIndex(u2_attrNameIndex);
        setAttributeLength(u4_attributeLength);
    }

    /**
     *
     * @param u2 Un
     * @throws UnSizeException
     */
    public void setAttributeNameIndex(Un u2) throws UnSizeException, Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.attribute_name_index = u2;
    }

    /**
     *
     * @return Un
     */
    public Un getAttributeNameIndex() {
        return this.attribute_name_index;
    }

    /**
     *
     * @param u4 Un
     * @throws UnSizeException
     */
    public void setAttributeLength(Un u4) throws UnSizeException {
        Un.validateUnSize(4, u4);
        this.attribute_length = u4;
    }

    public Un getAttributeLength() throws UnSizeException {
        return this.attribute_length;
    }

    /**
     *
     * @return String
     */
    public String toString() {
        try {
            return writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        ret = ret + "attribute_name_index =" +
                attribute_name_index.writeSelected(buff);
        ret = ret + ", attribute_length =" +
                attribute_length.writeSelected(buff);
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof AttributeInfo)) {
            return false;
        }
        AttributeInfo att = (AttributeInfo) obj;
        if (attribute_name_index.equals(att.attribute_name_index) &&
                attribute_length.equals(att.attribute_length)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(attribute_name_index).append(
                attribute_length).toHashCode();
    }
}
