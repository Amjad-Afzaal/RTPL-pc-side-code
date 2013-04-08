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

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: This file represents a class file.
 * It containts what we have in a class file. That means following
         u4 magic;
         u2 minor_version;
         u2 major_version;
         u2 constant_pool_count;
         cp_info constant_pool[constant_pool_count-1];
         u2 access_flags;
         u2 this_class;
         u2 super_class;
         u2 interfaces_count;
         u2 interfaces[interfaces_count];
         u2 fields_count;
         field_info fields[fields_count];
         u2 methods_count;
         method_info methods[methods_count];
         u2 attributes_count;
         attribute_info attributes[attributes_count]
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */



public class ClassFile implements BaseObject, Comparable<ClassFile> {
    private Un magic; //(SectionSizes.MAGIC_NUMBER_SIZE);
    private Un minor_version; //(SectionSizes.MINOR_VERSION_SIZE);
    private Un major_version; //(SectionSizes.MAGOR_VERSION_SIZE);
    private MultiplePoolsFacade constant_pool = null;
    private AccessFlags access_flags = null;
    private Un this_class; //(SectionSizes.THIS_CLASS_SIZE);
    private Un super_class; //(SectionSizes.SUPER_CLASS_SIZE);
    private int cpSize = -1;
    private InterfaceController interfaces = null;
    private FieldInfoController fieldController = null;
    private MethodInfoController methodsCont = null;
    private AttributeInfoController attributesCont = null;
    private String name;
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    public static ClassFile currentClassToWorkOn = null;
    private String fullyQualifiedNamedcached = null;
    private String cachedPackageName = null;
    public ClassFile(MultiplePoolsFacade constantPool, String uniqueFullName) {
        this.constant_pool = constantPool;
        this.name = uniqueFullName;
    }

    private ClassFile() {
    }

    //public static ClassFile getInstanceOf() {
    //  return cfile;
    //}

    /**
     * It returns the actual name of the class instead of the name of file it is
     * stored into.... For example of Object was stored NonObject.class, this method will
     * still return "java/lang/Object"
     * @return String
     * @throws Exception
     */
    public String getFullyQualifiedClassName() {
        if (fullyQualifiedNamedcached != null) {
            return fullyQualifiedNamedcached;
        }
        try {            
            MultiplePoolsFacade base = getConstantPool();
            if (base == null) {
                return null;
            }
            ClassInfo cinfo = (ClassInfo) base.get(this_class.
                    intValueUnsigned(), TagValues.CONSTANT_Class);
            UTF8Info utf8 = (UTF8Info) getConstantPool().get(cinfo.getIndex().
                    intValueUnsigned(), TagValues.CONSTANT_Utf8);
            fullyQualifiedNamedcached =  utf8.convertBytes();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return fullyQualifiedNamedcached;
    }

    public static String getPackageName(ClassFile file) {
        String packageName = "";
        String className = file.getFullyQualifiedClassName();
        if (className.indexOf("/") > 0) {
            packageName = className.substring(0, className.lastIndexOf("/"));
        }
        return packageName;
    }
    /**
     * 
     * @return - the package names based on fully qualified name. In case
     * the package does not exist then it returns empty string (not null).
     */
    public String getPackageName() {
        if (cachedPackageName == null) {
            cachedPackageName = getPackageName(this);
        }
        return cachedPackageName;
    }
    
    /**
     * It returns file name used to read the class. That mean if Object.class has
     * some other class says NonObject then it will return Object.class. It also
     * return full path.
     * @return String
     */
    public String getSourceFileNameWithPath() {
        return name;
    }

    public void setMagicNumber(Un u4) throws Exception {
        this.magic = u4;
    }

    public Un getMagicNumber() {
        return this.magic;
    }


    public void setMinorVersion(Un u2) {
        this.minor_version = u2;
    }

    public Un getMiniorVersion() {
        return this.minor_version;
    }


    public void setMajorVersion(Un u2) {
        this.major_version = u2;
    }

    public Un getMajorVersion() {
        return this.major_version;
    }


    public void setConstantPoolCount(Un u2) throws Exception {
        this.cpSize = u2.intValueUnsigned();//this.constant_pool_count = u2;
        constant_pool.setMaxSize(cpSize);
    }

    public int getConstantPoolCount() {
        return this.cpSize;
    }


    public void setConstantPool(MultiplePoolsFacade cp) {
        this.constant_pool = cp;
    }

    public MultiplePoolsFacade getConstantPool() {
        return this.constant_pool;
    }


    public void setAccessFlags(AccessFlags accFlag) {
        this.access_flags = accFlag;
    }

    public AccessFlags getAccessFlags() {
        return this.access_flags;
    }

    public void setThisClass(Un u2) throws UnSizeException, Exception {
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.this_class = u2;
    }

    public Un getThisClass() {
        return this.this_class;
    }

    public void setSuperClass(Un u2) throws UnSizeException, Exception {
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.super_class = u2;
    }

    public Un getSuperClass() {
        return this.super_class;
    }


    public void setInterfaceCount(Un u2) throws Exception {
        //this.interfaces_count = u2;
        if (u2 != null) {
            this.interfaces = factory.createInterfaceController(u2.intValueUnsigned());
        } else {
            this.interfaces = factory.createInterfaceController(0);
        }
    }

//    public Un getInterfaceCount() throws Exception {
//        return factory.createUn(interfaces.getCurrentSize());
//    }

    public void addInterfacesInfos(Un bytes) throws Exception {
        for (int loop = 0; loop < interfaces.getMaxSize(); loop++) {
            interfaces.add(Un.cutBytes(2, bytes));
        }
    }

    public void setInterfacesInfos(InterfaceController controller) throws
            Exception {
        this.interfaces = controller;
    }

    public Un getInterfacesInfo(int index) throws Exception {
        return (Un) interfaces.get(index);
    }

    public void setAttributeInfoController(AttributeInfoController attCont) {
        this.attributesCont = attCont;
    }

    public InterfaceController getInterfaceController() {
        return interfaces;
    }

    public void setFieldCount(Un u2) throws Exception {
        //this.fields_count = u2;
        if (u2 != null) {
            this.fieldController = factory.createFieldInfoController(u2.
                    intValueUnsigned());
        } else {
            this.fieldController = factory.createFieldInfoController(0);
        }
    }

//    public Un getFieldCount() throws Exception {
//        return  factory.createUn(this.fieldController.getCurrentSize());
//    }

    public FieldInfoController getFieldInfoController() {
        return fieldController;
    }

    public MethodInfoController getMethodInfoController() {
        return methodsCont;
    }

    public void addFieldInfo(FieldInfo fInfo) throws Exception {
        fieldController.add(fInfo);
    }

    public FieldInfo getFieldsInfo(int index) throws Exception {
        return (FieldInfo) fieldController.get(index);
    }

    public MethodInfo hasMethod(String fieldKey) {
        FieldInfo method = hasMethodOrField(fieldKey, false);
        if (method == null) {
            return null;
        }
        return (MethodInfo) method;
    }

    public FieldInfo hasField(String fieldKey) {
        return hasMethodOrField(fieldKey, true);
    }

    public FieldInfo hasMethodOrField(String fieldKey, boolean isField) {
        ControllerBase cont = fieldController;
        if (!isField) {
            cont = methodsCont;
        }
        int size = cont.getCurrentSize();
        FieldInfo field = null;
        for (int loop = 0; loop < size; loop++) {
            field = (FieldInfo) cont.get(loop);
            if (field.isKey(fieldKey)) {
                return field;
            }
        }
        return null;
    }


    public void setMethodCount(Un u2) throws Exception {
        this.getFullyQualifiedClassName(); //it is for caching the names so that later cached can be used. Do not remove it.
        //this.methods_count = u2;
        if (u2 != null) {
            this.methodsCont = factory.createMethodInfoController(u2.intValueUnsigned());
        } else {
            this.methodsCont = factory.createMethodInfoController(0);
        }
    }

//    public Un getMethodCount() throws Exception {
//        return factory.createUn(methodsCont.getCurrentSize());
//    }


    public void addMethodInfo(MethodInfo fInfo) throws Exception {
        methodsCont.add(fInfo);
    }

//    public MethodInfo getMethodInfo(int index) throws Exception {
//        return (MethodInfo) methodsCont.get(index);
//    }


//    public Un getAttributesCount() throws Exception {
//        return factory.createUn(attributesCont.getCurrentSize());
//    }

    public void setAttributesCount(Un attributes_count) throws Exception {
        //this.attributes_count = attributes_count;
        if (attributes_count != null) {
            attributesCont = factory.createAttributeInfoController(
                    attributes_count.
                    intValueUnsigned());
        } else {
            attributesCont = factory.createAttributeInfoController(0);
        }
    }

    public AttributeInfoController getAttributeInfoController() {
        return attributesCont;
    }

    public void addAttributeInfo(AttributeInfo fInfo) throws Exception {
        attributesCont.add(fInfo);
    }

    public AttributeInfo getAttributeInfo(int index) throws Exception {
        return (AttributeInfo) attributesCont.get(index);
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
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ClassFile)) {
            return false;
        }

        ClassFile file = (ClassFile) obj;
        if (file.getFullyQualifiedClassName().equals(getFullyQualifiedClassName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fullyQualifiedNamedcached).toHashCode();
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        try {
            ret = ret + "\n ---------------------- \n";
            ret = ret + " Magic Number = " + magic.writeSelected(buff);
            ret = ret + "\n Minor Version=" + minor_version.writeSelected(buff);
            ret = ret + "\n Major version=" + major_version.writeSelected(buff);
            //ret = ret + "\n ---------------------- \n";
            ret = ret + "\n ----- Constant Pool ---- \n" +
                  constant_pool.writeSelected(buff);
            ret = ret + "\n ---------------------- \n";
//            Miscellaneous.println(ret);
            ret = ret + "\n Access flags=" + access_flags.writeSelected(buff);
            ret = ret + "\n this class=" + this_class.writeSelected(buff);
            ret = ret + "\n super class=" + super_class.writeSelected(buff);
            ret = ret + "\n Interfaces Info=" + interfaces.writeSelected(buff);
            ret = ret + "\n" + fieldController.writeSelected(buff);
            ret = ret + "\n--------\n" + methodsCont.writeSelected(buff);
            ret = ret + "\n" + attributesCont.writeSelected(buff);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;

    }

    @Override
    public int compareTo(ClassFile input) {
         return input.getFullyQualifiedClassName().
                 compareTo(getFullyQualifiedClassName());
    }

}
