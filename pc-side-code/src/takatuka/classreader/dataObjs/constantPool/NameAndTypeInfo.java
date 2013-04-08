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
package takatuka.classreader.dataObjs.constantPool;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.4.6 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * CONSTANT_NameAndType_info {
        u1 tag; //12
        u2 name_index;
        u2 descriptor_index;
 }
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class NameAndTypeInfo extends RegularInfoBase {

    private Un descriptorIndex = null; //u2.
    public NameAndTypeInfo() throws TagException, Exception {
        super(TagValues.CONSTANT_NameAndType);
    }

    private NameAndTypeInfo(Un tag) throws TagException {
        super(tag);
    }

    public void setDescriptorIndex(Un u2) throws Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.descriptorIndex = u2;
    }

    public Un getDescriptorIndex() {
        return descriptorIndex;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tNameAndTypeInfo=" + super.writeSelected(buff);
        ret = ret + ", descriptor_index=" + descriptorIndex.writeSelected(buff);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof NameAndTypeInfo)) {
            return false;
        }
        NameAndTypeInfo info = (NameAndTypeInfo) obj;
        if (super.equals(obj) &&
            this.descriptorIndex.equals(info.descriptorIndex)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(
                descriptorIndex).toHashCode();
    }

    @Override
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
}
