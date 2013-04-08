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
 * <p>Description: Based on section 4.7.7
 * SourceFile_attribute {
        u2 attribute_name_index;
        u4 attribute_length;
        u2 sourcefile_index;
    }

 *
 *  </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SourceFileAtt extends AttributeInfo {
    Un sourcefile_index; //(2);

    public SourceFileAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                         Un u2_sourceFileIndex) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
        setSourcefileIndex(u2_sourceFileIndex);

    }

    public void setSourcefileIndex(Un u2_sourceFileIndex) throws
            UnSizeException, Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2_sourceFileIndex);
        this.sourcefile_index = u2_sourceFileIndex;
    }

    public Un getSourcefileIndex() {
        return sourcefile_index;
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

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SourceFileAtt)) {
            return false;
        }
        SourceFileAtt att = (SourceFileAtt) obj;
        if (super.equals(att) && sourcefile_index.equals(att.sourcefile_index)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(sourcefile_index).append(super.
                hashCode()).toHashCode();
    }


    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tSourceFileAtt=";
        ret = ret + super.writeSelected(buff);
        ret = ret + ", sourcefile_index= " +
              sourcefile_index.writeSelected(buff);
        return ret;
    }
}
