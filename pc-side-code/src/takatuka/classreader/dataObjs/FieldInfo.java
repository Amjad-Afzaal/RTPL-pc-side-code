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
package takatuka.classreader.dataObjs;

import java.util.*;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: based on section 4.5 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html </p>
 * field_info {
u2 access_flags;
u2 name_index;
u2 descriptor_index;
u2 attributes_count;
attribute_info attributes[attributes_count];
}
 * @author Faisal Aslam
 * @version 1.0
 */
public class FieldInfo implements BaseObject {

    private AccessFlags access_flags = null; //(2);
    private Un name_index = null; //(2);
    private Un descriptor_index = null; //(2);
    private Un attributes_count = null; //(2);
    private AttributeInfoController attributesController = null;
    protected static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private ClassFile myClass = null;

    public FieldInfo(ClassFile myClass) {
        this.myClass = myClass;
    }

    public ClassFile getClassFile() {
        return myClass;
    }
    /*public Un getThisClass() {
        return myClass.getThisClass();
    }*/

    public boolean isKey(String key) {
        return key.equals(getKey());
    }

    public static String createKey(NameAndTypeInfo nAndt) {
        Un descIndex = nAndt.getDescriptorIndex();
        Un nameIndex = nAndt.getIndex();
        return createKey(descIndex, nameIndex);
    }

    public static String createKey(int methodDescIndex, int methodNameIndex) {
        try {
            Un descUn = factory.createUn(methodDescIndex).trim(2);
            Un nameUn = factory.createUn(methodNameIndex).trim(2);
            return createKey(descUn, nameUn);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }

    public static String createKey(Un methodDescriptionIndex, Un nameIndex) {
        return methodDescriptionIndex + ", " + nameIndex;
    }

    /**
     * Method name and description indentify a method uniquely.
     * @param method MethodInfo
     * @return String
     */
    public String getKey() {
        Un desc = getDescriptorIndex();
        Un name = getNameIndex();
        return createKey(desc, name);
    }

    public void setAccessFlags(Un u2) throws UnSizeException, Exception {
        this.access_flags = factory.createAccessFlag(u2);
    }

    public AccessFlags getAccessFlags() {
        return this.access_flags;
    }

    public void setNameIndex(Un u2) throws UnSizeException, Exception {
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.name_index = u2;
        /*
        Miscellaneous.println("-------------->>"+((UTF8Info)ClassFile.
        currentClassToWorkOn.getConstantPool().
        get(u2.intValueUnsigned())).convertBytes());
         */
    }

    public Un getNameIndex() {
        return this.name_index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FieldInfo)) {
            return false;
        }
        FieldInfo fInfo = (FieldInfo) obj;
        if (fInfo.access_flags.equals(access_flags) && fInfo.name_index.equals(name_index) &&
                fInfo.descriptor_index.equals(descriptor_index) && fInfo.attributes_count.equals(attributes_count) &&
                fInfo.attributesController.equals(attributesController) &&
                fInfo.myClass.getThisClass().equals(myClass.getThisClass())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(access_flags).append(name_index).
                append(descriptor_index).append(attributes_count).
                append(attributesController).append(myClass.getThisClass()).toHashCode();
    }

    public void setDescriptorIndex(Un u2) throws UnSizeException, Exception {
        //Miscellaneous.println(name_index);
        //Miscellaneous.println(u2);
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.descriptor_index = u2;
    }

    public Un getDescriptorIndex() {
        return this.descriptor_index;
    }

    public void setAttributeCount(Un u2) throws UnSizeException, Exception {
        Un.validateUnSize(2, u2);
        this.attributes_count = u2;
        attributesController = factory.createAttributeInfoController(u2.intValueUnsigned());
    }

    public AttributeInfoController getAttributes() {
        return attributesController;
    }

    public Un getAttributeCount() {
        return this.attributes_count;
    }

    public AttributeInfoController getAttributeController() {
        return attributesController;
    }

    public void addAttribute(GenericAtt gAtt) throws Exception {
        attributesController.add(gAtt);
    }

    public void addAttribute(Vector gAtts) throws Exception {
        attributesController.add(gAtts);
    }

    public AttributeInfo getAttribute(int index) throws Exception {
        return (AttributeInfo) attributesController.get(index);
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
        String ret = "\tFieldInfo=[";
        ret = ret + " access_flag=" + access_flags.writeSelected(buff);
        ret = ret + ", name_index= " + name_index.writeSelected(buff);
        ret = ret + ", descriptor_index=" + descriptor_index.writeSelected(buff);
        //ret = ret + ", attributes_count=" + attributes_count.writeSelected(buff);
        ret = ret + ", attributes=" + attributesController.writeSelected(buff);
        return ret;
    }
}
