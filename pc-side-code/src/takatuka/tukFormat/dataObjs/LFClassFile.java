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
package takatuka.tukFormat.dataObjs;

import takatuka.optimizer.cpGlobalization.logic.util.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.tukFormat.logic.factory.LFFactoryFacade;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.verifier.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFClassFile extends VerifyClassFile implements LFBaseObject {

    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    public Vector toReplaceCP = new Vector();
    public int mineAddress = 0;
    private Un address = null;
    private static final String OPPRAM = "jvm_extra_bytes_per_object.properties";
    private static final PropertyReader propReader = PropertyReader.getInstanceOf();
    private static Properties extraBytesPerObject = propReader.loadProperties(OPPRAM);
    public static int startAddressForAllClasses = 0;
    private MethodTable myMethodTable = null;

    public LFClassFile(MultiplePoolsFacade constantPool, String uniqueName) {
        super(constantPool, uniqueName);
    }

    /**
     * returns -1 if the method/field with given nameAndType does not exist in
     * the class
     * @param nameAndType NameAndTypeInfo
     * @return int
     */
    public int findMethodField(Un nameAndType, boolean isField) throws
            Exception {
        ControllerBase contr = getMethodInfoController();
        if (isField) {
            contr = getFieldInfoController();
        }
        LFFieldInfo field = null;
        for (int loop = 0; loop < contr.getCurrentSize(); loop++) {
            field = (LFFieldInfo) contr.get(loop);
            if (field.checkNameAndType(nameAndType)) {
                return loop;
            }
        }
        return -1;
    }

    /**
     *
     * @return
     */
    public MethodTable getMyMethodTable() {
        return myMethodTable;
    }

    /**
     * return address of method atIndex controller
     * 
     * @param atIndex
     * @param isField
     * @return
     * @throws java.lang.Exception
     */
    public Un getMethodOrFieldAddress(int atIndex, boolean isField) throws Exception {
        ControllerBase contr = getMethodInfoController();
        if (isField) {
            contr = getFieldInfoController();
        }
        return ((LFFieldInfo) contr.get(atIndex)).getAddress();
    }

    /**
     * returns the address of object
     * @return Un
     */
    public Un getAddress() {
        return address;
    }

    /**
     * set the address of object
     * @param address Un
     */
    public void setAddress(Un address) {
        this.address = address;
    }

    @Override
    public void addMethodInfo(MethodInfo fInfo) throws Exception {
        getConstantPool().add(fInfo, TagValues.CONSTANT_Methodref);
    }

    public int isReferred(Un nameAndTypeIndex, boolean isField) throws
            Exception {
        ControllerBase cont = getMethodInfoController();
        LFFieldInfo field = null;
        if (isField) {
            cont = getFieldInfoController();
        }
        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            field = (LFFieldInfo) cont.get(loop);

            if (field.checkNameAndType(nameAndTypeIndex)) {
                return LFFieldInfo.computeRefInfoUsingInfo(field);
            }
        }
        return -1;
    }

    ControllerBase getReferedMethodFieldAddressTables(boolean isField) throws
            Exception {
        ControllerBase retVec = new ControllerBase();
        ControllerBase base = getMethodInfoController();
        if (isField) {
            base = getFieldInfoController();
        }
        LFFieldInfo mInfo = null;
        for (int loop = 0; loop < base.getCurrentSize(); loop++) {
            mInfo = (LFFieldInfo) base.get(loop);
            //todo if (mInfo.getReferIndex() < 0) continue;
            retVec.add(mInfo);

        }
        return retVec;
    }

    public static UTF8Info getUTF8FromIndex(Un index) throws Exception {
        UTF8Info utf8 = (UTF8Info) pOne.get(index.intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        return utf8;
    }

    public static int calculateFieldLength(LFFieldInfo field) throws Exception {
        int length = 0;
        UTF8Info info = (UTF8Info) pOne.get(field.getDescriptorIndex().
                intValueUnsigned(), TagValues.CONSTANT_Utf8);
        String descStr = info.convertBytes();
        if (descStr.equals("B")) { //byte
            length = 1;
        } else if (descStr.equals("C")) { //char
            length = 2;
        } else if (descStr.equals("I")) { //int
            length = 4;
        } else if (descStr.equals("S")) { //short
            length = 2;
        } else if (descStr.equals("F")) { //float
            length = 4;
        } else if (descStr.equals("Z")) { //boolean
            length = 1;
        } else if (descStr.equals("D")) { //double
            length = 8;
        } else if (descStr.equals("J")) { //long
            length = 8;
        } else if (descStr.startsWith("L") || descStr.endsWith(";")
                || descStr.startsWith("[")) { //reference
            length = 2;
        } else {
            throw new Exception("Some exception with field description: "
                    + descStr);
        }
        return length;
    }

    /**
     *
     * @return
     * return total field length super as well as this class fields.
     * @throws Exception
     */
    public int getNonStaticFieldLength() throws Exception {
        ControllerBase temp = getReferedMethodFieldAddressTables(true);
        int allFieldLenght = 0;
        if (getSuperClass() != null && getSuperClass().intValueUnsigned() != 0) {
            LFClassFile file = (LFClassFile) Oracle.getInstanceOf().getClass(
                    getSuperClass(), pOne);
            if (file != null) { //loaded
                allFieldLenght += file.getNonStaticFieldLength();
            }
        }
        LFFieldInfo field = null;
        for (int loop = 0; loop < temp.getCurrentSize(); loop++) {
            field = (LFFieldInfo) temp.get(loop);
            if (!field.getAccessFlags().isStatic()) {
                allFieldLenght = allFieldLenght + calculateFieldLength(field);
            }
        }
        int offSet = 0;
        String strOffSet = extraBytesPerObject.getProperty(this.getFullyQualifiedClassName());
        if (strOffSet != null) {
            offSet = Integer.parseInt(strOffSet);
        }
        allFieldLenght += offSet;

        return allFieldLenght;

    }

    private int getStaticFieldLength() throws Exception {
        ControllerBase temp = getReferedMethodFieldAddressTables(true);
        int allFieldLenght = 0;
        LFFieldInfo field = null;
        for (int loop = 0; loop < temp.getCurrentSize(); loop++) {
            field = (LFFieldInfo) temp.get(loop);
            if (field.getAccessFlags().isStatic()) {
                allFieldLenght = allFieldLenght + calculateFieldLength(field);
            }
        }
        return allFieldLenght;
    }

    private static Un getFieldType(FieldInfo field) throws Exception {
        int code = 0;
        UTF8Info info = (UTF8Info) pOne.get(field.getDescriptorIndex().
                intValueUnsigned(), TagValues.CONSTANT_Utf8);
        String descStr = info.convertBytes();
        if (descStr.startsWith("L") || descStr.endsWith(";")
                || descStr.startsWith("[")) { //reference
            code = FieldTypes.TYPE_JREF;
        } else if (descStr.startsWith("B")) { //byte
            code = FieldTypes.TYPE_JBYTE;
        } else if (descStr.startsWith("C")) { //char
            code = FieldTypes.TYPE_JCHAR;
        } else if (descStr.startsWith("I")) { //int
            code = FieldTypes.TYPE_JINT;
        } else if (descStr.startsWith("S")) { //short
            code = FieldTypes.TYPE_JSHORT;
        } else if (descStr.startsWith("F")) { //float
            code = FieldTypes.TYPE_JFLOAT;
        } else if (descStr.startsWith("Z")) { //boolean
            code = FieldTypes.TYPE_JBOOLEAN;
        } else if (descStr.startsWith("D")) { //double
            code = FieldTypes.TYPE_JDOUBLE;
        } else if (descStr.startsWith("J")) { //long
            code = FieldTypes.TYPE_JLONG;
        } else {
            Miscellaneous.printlnErr("ERROR: Unkown field Type ");
            Miscellaneous.exit();
        }
        return factory.createUn(code).trim(1);
    }

    private String writeSelectedMethods(BufferedByteCountingOutputStream buff, LFMethodInfo method) throws
            Exception {
        method.setAddress(((LFFactoryFacade) factory).createAddressUn(
                buff.numberOfBytesWritten() + startAddressForAllClasses));

        String ret = "\n\n//method-index=" + method.getReferenceIndex() + ": "
                + Oracle.getInstanceOf().getMethodOrFieldString(method)
                + "\n"
                + method.writeSelected(buff) + "\n";
        return ret;
    }

    private String writeSelectedMethodTable(BufferedByteCountingOutputStream buff) throws Exception {
        myMethodTable = new MethodTable(this);
        return myMethodTable.writeSelected(buff);
    }

    private int superObjectSize() throws Exception {
        int superSize = 0;
        if (this.getSuperClass() != null) {
            LFClassFile file = (LFClassFile) Oracle.getInstanceOf().getClass(this.getSuperClass(), pOne);
            if (file != null) { //loaded
                superSize = file.getNonStaticFieldLength();
            }
        }
        return superSize;
    }

    private int totalObjectFieldCount() throws Exception {
        int superSize = 0;
        superSize += getFieldInfoController().getCurrentSize();
        if (this.getSuperClass() != null) {
            LFClassFile file = (LFClassFile) Oracle.getInstanceOf().getClass(this.getSuperClass(), pOne);
            if (file != null) { //loaded
                superSize += file.totalObjectFieldCount();
            }
        }
        return superSize;
    }

    public String writeSelectedFieldInfos(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        LFFieldInfo field = null;
        //--------------------- Field -------------------
        Un nonStaticfieldStart = factory.createUn(0).trim(2);
        ControllerBase mineInfos = getReferedMethodFieldAddressTables(true);
        mineInfos.sort(new FieldComparator());

        int lastNonStaticFieldOffset = superObjectSize();
        int offSet = 0;
        String strOffSet = extraBytesPerObject.getProperty(this.getFullyQualifiedClassName());
        if (strOffSet != null) {
            offSet = Integer.parseInt(strOffSet);
        }
        lastNonStaticFieldOffset += offSet;

        int fieldCount = 0;
        boolean firstTimeNonStatic = true;
        for (int loop = 0; loop < mineInfos.getCurrentSize(); loop++) {
            field = (LFFieldInfo) mineInfos.get(loop);
            if (field.getReferenceIndex() < 0) {
                continue;
            }
            if (!field.getAccessFlags().isStatic()) {
                lastNonStaticFieldOffset = setFieldOffsetAndType(field, lastNonStaticFieldOffset);
                if (firstTimeNonStatic) {
                    nonStaticfieldStart = factory.createUn(field.getReferenceIndex()).trim(2);
                    firstTimeNonStatic = false;
                }
                fieldCount++;
            }
        }
        ret = ret + "non_statics_field_start=" + nonStaticfieldStart.writeSelected(buff);
        ret = ret + "\ntotal_non_static_field_count="
                + factory.createUn(fieldCount).trim(2).
                writeSelected(buff);
        //}

        return ret;
    }

    /**
     * set the field type and offset.
     * The offset is relative to the given lastFieldOffset
     * @param field: the given field to compute and set offset
     * @param lastFieldOffset : offset of the previous field
     * @return : given field offset
     * @throws java.lang.Exception
     */
    public static int setFieldOffsetAndType(LFFieldInfo field, int lastFieldOffset) throws Exception {
        LFFieldRefInfo refInfo = null;
        Un specialAddress = getFieldType(field);
        specialAddress.conCat(factory.createUn(lastFieldOffset).trim(2));
        lastFieldOffset += calculateFieldLength(field);
        refInfo = (LFFieldRefInfo) pOne.get(field.getReferenceIndex(),
                TagValues.CONSTANT_Fieldref);
        refInfo.setFieldMethodInfoAddress(specialAddress);
        return lastFieldOffset;
    }

    private String writeSelectedHeader(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        if (getAccessFlags().isInterface()) {
            ret += "\nsuper_class="
                    + super.getSuperClass().writeSelected(buff);
            return ret;
        }
        ret += "\nthis_class="
                + super.getThisClass().writeSelected(buff);
        ret += "\nsuper_class="
                + super.getSuperClass().writeSelected(buff);
        ret += "\nnon_static_field_size="
                + factory.createUn(getNonStaticFieldLength()).trim(2).
                writeSelected(buff);
        ret += "\nstatic_field_size="
                + factory.createUn(getStaticFieldLength()).trim(2).
                writeSelected(buff);

        ret += "\n\n";

        if (!ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
            return "";
        }

        buff.flush();
        return ret;

    }

    private String writeSelectedInterfaceInfo(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\n--- Interfaces: ---";
        ret = ret + "\n" + super.getInterfaceController().writeSelected(buff);
        ret = ret + "\n---- end of interfaces ---\n";
        buff.flush();
        return ret;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\n ----------------------------------------------- \n";
        ret += "\n//class_name=" + getFullyQualifiedClassName();
        ret += "\n//myAddress =" + getAddress();

        ret += writeSelectedHeader(buff);
        if (getAccessFlags().isInterface()) {
            ret += writeSelectedInterfaceInfo(buff);
            return ret;
        }

        ret += writeSelectedFieldInfos(buff);
        ret += writeSelectedMethodTable(buff);
        ret += writeSelectedInterfaceInfo(buff);
        ret += "------- Methods -----\n";
        LFMethodInfo temp = null;
        ControllerBase mineInfos = getMethodInfoController();
        for (int loop = 0; loop < mineInfos.getCurrentSize(); loop++) {
            temp = (LFMethodInfo) mineInfos.get(loop);
            ret += writeSelectedMethods(buff, temp);
        }
        return ret;
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff);
    }
}
