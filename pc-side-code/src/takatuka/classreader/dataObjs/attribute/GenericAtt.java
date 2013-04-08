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
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.7.1 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 * A VM may define additional attributes for its use.
 * However, other VM does not have to support those attribute.
 * We have named such attributes as generic attributes and this class is holding them
 * Furthremore, this class is also used to send data to AttributeInfoController to make specific attributes.
 * place of such attributes.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenericAtt extends AttributeInfo {
    private Un bytes;
    public GenericAtt() {
        super();
    }

    public GenericAtt(Un u2_attrNameIndex, Un u4_attributeLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
    }


    public void setInfo(Un bytes) throws Exception {
        if (bytes.size() != super.getAttributeLength().intValueUnsigned()) {
            throw new Exception("Generic Attribute Exception");
        }
        this.bytes = bytes;
    }

    public Un getInfo() {
        return bytes;
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
        return super.writeSelected(buff) + ", " + bytes.writeSelected(buff);
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GenericAtt)) {
            return false;
        }
        GenericAtt att = (GenericAtt) obj;
        if (super.equals(att) && bytes.equals(att.bytes)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(bytes).
                append(super.hashCode()).toHashCode();
    }


}
