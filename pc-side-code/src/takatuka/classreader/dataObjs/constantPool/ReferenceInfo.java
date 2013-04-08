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
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 *
 * <p>Description:
 * (base on section 4.4.2 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html)
 * Fields, methods, and interface methods are represented by similar structures:
 *  CONSTANT_Field(Method or Interface)ref_info {
 *     u1 tag; //9 if field, 10 if method, 11 if InterfaceMethod
 *      u2 class_index; //index to the costant_pool  (need validation checks here at least 2)
 *      u2 name_and_type_index; //index to the cost_pool table
 * }
 The items of these structures are as follows:

 class_index
    The value of the class_index item must be a valid index into the constant_pool table.
 The constant_pool entry at that index must be a CONSTANT_Class_info (4.4.1)
 structure representing the class or interface type that contains the declaration
    of the field or method.

    The class_index item of a CONSTANT_Methodref_info structure must be a class type,
    not an interface type. The class_index item of a CONSTANT_InterfaceMethodref_info
    structure must be an interface type. The class_index item of a CONSTANT_Fieldref_info
    structure may be either a class type or an interface type.

 name_and_type_index
    The value of the name_and_type_index item must be a valid index into the
    constant_pool table. The constant_pool entry at that index must be a
 CONSTANT_NameAndType_info (4.4.6) structure. This constant_pool entry indicates
 the name and descriptor of the field or method. In a CONSTANT_Fieldref_info the
    indicated descriptor must be a field descriptor (4.3.2). Otherwise, the indicated
    descriptor must be a method descriptor (4.3.3).

    If the name of the method of a CONSTANT_Methodref_info structure begins with a' <' ('\u003c'),
    then the name must be the special name <init>, representing an instance initialization method (3.9).
    Such a method must return no value.
 * </p>
 * <p> Copyright: 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details
 *
 *  * If you need additional information or have any questions   
 * then contact Faisal Aslam, at aslam AT informatik.uni-freiburg.de or 
 * at studentresearcher AT gmail.
 *
 * <p>Company: University of Freiburg </p>
 * @author Faisal Aslam
 * @version 1.0
 */


public abstract class ReferenceInfo extends RegularInfoBase {

    private Un nameAndTypeIndex; //(2);

    public ReferenceInfo(Un tag) throws TagException {
        super(tag);
    }

    public ReferenceInfo(byte tag) throws TagException, Exception {
        super(tag);
    }

    public ReferenceInfo() throws TagException, Exception {
        super();
    }

    public void setNameAndType(Un u2) throws Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.nameAndTypeIndex = u2;
    }

    public Un getNameAndTypeIndex() {
        return this.nameAndTypeIndex;
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

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        ret = ret + super.writeSelected(buff) + ", nameAndTypeIndex=" +
              nameAndTypeIndex.writeSelected(buff) /*+ getDebugInfo()*/;
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ReferenceInfo)) {
            return false;
        }
        ReferenceInfo fmiRef = (ReferenceInfo)
                               obj;
        if (super.equals(obj) &&
            getNameAndTypeIndex().equals(fmiRef.getNameAndTypeIndex())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(
                getIndex()).append(getNameAndTypeIndex()).toHashCode();
    }


}
