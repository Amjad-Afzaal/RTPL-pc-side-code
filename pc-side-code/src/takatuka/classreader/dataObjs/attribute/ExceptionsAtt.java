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

import java.util.*;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.7.4. at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 *
 * Exceptions_attribute {
        u2 attribute_name_index; //super
        u4 attribute_length; //super
        u2 number_of_exceptions;
        u2 exception_index_table[number_of_exceptions];
   }
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ExceptionsAtt extends AttributeInfo {
    private Un number_of_exceptions;
    private Vector exception_index_table = new Vector(); //u2 array not u1 arry Noted (based on number_of_exceptions)

    public ExceptionsAtt() {
    }

    private ExceptionsAtt(Un u2_attrNameIndex, Un u4_attributeLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
    }

    public ExceptionsAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                         Un u2_numberOfExceptions) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
        this.setNumberOfExceptions(u2_numberOfExceptions);
    }

    public void setNumberOfExceptions(Un u2) throws UnSizeException {
        Un.validateUnSize(2, u2);
        this.number_of_exceptions = u2;
    }

    public Un getNumberOfExceptions() throws UnSizeException {
        return this.number_of_exceptions;
    }


    public void setAllExceptionIndexTable(Un bytes) throws UnSizeException,
            Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        ValidateUn vUn = factory.createValidateUn();
        vUn.validateConsantPoolIndexMultiple(bytes);
        for (int loop = 0; loop < number_of_exceptions.intValueUnsigned(); loop++) {
            exception_index_table.addElement(Un.cutBytes(2, bytes));
        }
    }

    public Un getExceptionIndexAt(int index) {
        return (Un) exception_index_table.elementAt(index);
    }

    public void updateExceptionIndex(int index, Un newValue) throws Exception {
        Un oldIndex = getExceptionIndexAt(index);
        if (oldIndex == null) {
            throw new Exception("Invalid exception index");
        }
        oldIndex.setData(newValue);
    }

    //public Vector getExceptionIndexTable() throws UnSizeException {
//        return this.exception_index_table;
//    }

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
        String ret = "\tExceptionsAtt=";
        ret = ret + super.writeSelected(buff);
        ret = ret + ", number_of_exceptions (int)=" +
              number_of_exceptions.writeSelected(buff);
        ret = ret + ", Indexes{";
        for (int loop = 0; loop < number_of_exceptions.intValueUnsigned(); loop++) {
            ret = ret +
                  ((Un) exception_index_table.elementAt(loop)).
                  writeSelected(buff);
            if (loop != (number_of_exceptions.intValueUnsigned() - 1)) {
                ret = ret + ", ";
            }

        }
        ret = ret + "}";
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExceptionsAtt)) {
            return false;
        }
        ExceptionsAtt att = (ExceptionsAtt) obj;
        if (super.equals(att) &&
            number_of_exceptions.equals(att.number_of_exceptions) &&
            exception_index_table.equals(att.exception_index_table)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(number_of_exceptions).
                append(exception_index_table).
                append(super.hashCode()).toHashCode();
    }


}
