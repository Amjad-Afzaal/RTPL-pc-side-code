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
 * Based on section 4.7.2 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 * todo: If a field_info structure representing a non-static field has a ConstantValue attribute, then that attribute must silently be ignored.
 * Every Java virtual machine implementation must recognize ConstantValue attributes.
 *
 * ConstantValue_attribute {
        u2 attribute_name_index; //ConstantValue
        u4 attribute_length; //should be always 2
        u2 constantvalue_index;
    }
 * The constantvalue index my point to CONSTANT_Long, CONSTANT_Float, 
 * CONSTANT_Double, CONSTANT_Integer and String CONSTANT_String
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ConstantValueAtt extends AttributeInfo {



    private Un constantvalue_index; //(2);
    public ConstantValueAtt() {
    }

    private ConstantValueAtt(Un u2_attrNameIndex, Un u4_attributeLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
        if (u4_attributeLength.intValueUnsigned() != 2) { //per documentation it should always be 2
            throw new Exception("Invalid length in ConstantValue attribute");
        }
    }

    public ConstantValueAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                            Un u2_constantvalueIndex) throws Exception {
        this(u2_attrNameIndex, u4_attributeLength);
        this.setConstantValueIndex(u2_constantvalueIndex);
    }


    public void setConstantValueIndex(Un u2) throws UnSizeException, Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.constantvalue_index = u2;
    }

    public Un getConstantValueIndex() {
        return constantvalue_index;
    }

    public String toString() {
        String ret = "";
        try {
            ret = ret + writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }


    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "ConstantValueAtt= ";
        ret = ret + super.writeSelected(buff);
        ret = ret + ", constantvalue_index=" +
              constantvalue_index.writeSelected(buff);
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ConstantValueAtt)) {
            return false;
        }
        ConstantValueAtt att = (ConstantValueAtt) obj;
        if (super.equals(att) &&
            constantvalue_index.equals(att.constantvalue_index)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(constantvalue_index).append(super.
                hashCode()).toHashCode();
    }

}
