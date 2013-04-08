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
package takatuka.tukFormat.dataObjs.constantpool;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;
import takatuka.tukFormat.logic.util.*;
import takatuka.optimizer.bytecode.replacer.logic.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * This file will be the header of constant pool. It will have addresses refers
 * to things inside constant pool (e.g. field_table_address). Furthermore, it will
 * have tables of addresses.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CPHeader implements LFBaseObject {

    private static int nativeId = 0;
    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private Un address = null;
    private static final String TYPES_MSG = "Type (Long='J', Float='F', Double='D', Integer='I', String='C')=";
    private int startIndexforClassMethodAndFieldTables = 0; //U2
    private int numberClasses = 0; //2
    private int classTableAddress = 0; //U4
    private int numberMethods = 0; //2
    private int methodTableAddress = 0; //U4
    private int numberOfNonStaticFields = 0; //2
    private int fieldTableAddress = 0; //U4
    private int numberOfStaticFields = 0;

    //-----------
    private int numberOfstrings = 0;
    private int stringTableStartIndex = 0;
    private int stringTableAddress = 0;
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    private static final CPHeader cpHeader = new CPHeader();
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private static HashMap nativeIdsMap = new HashMap();

    private CPHeader() {
    //singelton
    }

    public Un getAddress() {
        return address;
    }

    public void setAddress(Un address) {
        this.address = address;
    }

    public final static CPHeader getInstanceof() {
        return cpHeader;
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

    public static UTF8Info getUTF8FromIndex(Un index) throws Exception {
        UTF8Info utf8 = (UTF8Info) pOne.get(index.intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        return utf8;
    }

    /**
     * The following will give warings when replacements are not available.
     */
    public void checkCPAddresses() {
        try {

            int size = pOne.getCurrentSize(TagValues.CONSTANT_Class);
            UTF8Info utf8 = null;
            LFClassInfo cInfo = null;
            String className = null;
            HashSet instRemoved = StartMeBCIR.getNameOfInterfacesRemove();
            for (int loop = 0; loop < size; loop++) {
                if (pOne.get(loop, TagValues.CONSTANT_Class) instanceof ClassInfo) {
                    cInfo = (LFClassInfo) pOne.get(loop, TagValues.CONSTANT_Class);
                    utf8 = getUTF8FromIndex(cInfo.getIndex());
                    className = utf8.convertBytes();
                    if (className.trim().startsWith("[")) {
                        continue; //ignore ????? todo
                    } else if (cInfo.getClassFileAddress().intValueUnsigned() == 0 && !instRemoved.contains(className)) {
                        logHolder.addLog("........ WARNING: Cannot find class file " +
                                className);
                    }

                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    public static int getNativeId(MethodInfo mInfo) {
        if (mInfo == null || !mInfo.getAccessFlags().isNative()) {
            return -1;
        }
        Object idObj = nativeIdsMap.get(mInfo);
        int ret = 0;
        if (idObj == null) {
            nativeIdsMap.put(mInfo, nativeId++);
            ret = nativeId;
        } else {
            ret = (Integer) idObj;
        }
        return ret;
    }

    private int getNumberOfStaticFields() {
        int size = pOne.getCurrentSize(TagValues.CONSTANT_Fieldref);
        int numberOfStaticFields = 0;
        for (int loop = 0; loop < size; loop++) {
            FieldRefInfo fInfo = (FieldRefInfo) pOne.get(loop, TagValues.CONSTANT_Fieldref);
            if (fInfo.isStatic) {
                numberOfStaticFields++;
            }
        }
        return numberOfStaticFields;
    }

    public void populateAddress() throws Exception {

        // for the time being we will just give warnings
        checkCPAddresses();

        // the classInfo starts first thing at the end of constants.
        startIndexforClassMethodAndFieldTables = SizeCalculator.getFirstInfoIndex(factory.createUn(TagValues.CONSTANT_Class).trim(1));

        numberClasses = SizeCalculator.getNumberOfInfo(factory.createUn(TagValues.CONSTANT_Class).trim(1));

        numberMethods = SizeCalculator.getNumberOfInfo(factory.createUn(TagValues.CONSTANT_Methodref).trim(1));

        numberOfNonStaticFields = SizeCalculator.getNumberOfInfo(factory.createUn(TagValues.CONSTANT_Fieldref).trim(1));

        numberOfStaticFields = getNumberOfStaticFields();
        numberOfNonStaticFields = numberOfNonStaticFields - numberOfStaticFields;

        //it will give address of first classInfo of the cp
        classTableAddress = (Integer) SizeCalculator.getCPAddress(
                startIndexforClassMethodAndFieldTables, TagValues.CONSTANT_Class).
                intValueUnsigned();

        int firstMethodInfo = SizeCalculator.getFirstInfoIndex(factory.createUn(
                TagValues.CONSTANT_Methodref).trim(1));
        if (pOne.getCurrentSize(TagValues.CONSTANT_Methodref) != 0) {
            methodTableAddress = (Integer) SizeCalculator.getCPAddress(firstMethodInfo,
                    TagValues.CONSTANT_Methodref).intValueUnsigned();

        }

        int firstFieldInfo = SizeCalculator.getFirstInfoIndex(factory.createUn(
                TagValues.CONSTANT_Fieldref).trim(1));
        if (pOne.getCurrentSize(TagValues.CONSTANT_Fieldref) != 0) {
            fieldTableAddress = (Integer) SizeCalculator.getCPAddress(firstFieldInfo,
                    TagValues.CONSTANT_Fieldref).intValueUnsigned();
        }

        numberOfstrings = SizeCalculator.getNumberOfInfo(factory.createUn(TagValues.CONSTANT_String).trim(1));
        stringTableStartIndex = SizeCalculator.getFirstInfoIndex(factory.createUn(TagValues.CONSTANT_String).trim(1));
        if (pOne.getCurrentSize(TagValues.CONSTANT_String) != 0) {
            stringTableAddress = (Integer) SizeCalculator.getCPAddress(stringTableStartIndex,
                    TagValues.CONSTANT_String).intValueUnsigned();
        }
    }

    /**
     * writeSelected
     *
     * @param buff BufferedByteCountingOutputStream
     * @return String
     * @throws Exception
     */
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        //Miscellaneous.println("So far 0= " + buff.numberOfBytesWritten());

        String ret =
                "========================== CP-Header =========================\n\n";
        ret = ret + "\nnumber of classes=" +
                factory.createUn(numberClasses).trim(2).writeSelected(buff);
        ret = ret + "\nclass Table Address=" +
                ((LFFactoryFacade) factory).createAddressUn(classTableAddress).
                writeSelected(buff); //4
        //Miscellaneous.println("So far 1= " + buff.numberOfBytesWritten());
        ret = ret + "\n----\n";
        ret = ret + "\nnumber of methods=" +
                factory.createUn(numberMethods).trim(2).writeSelected(buff);
        //Miscellaneous.println("So far 2= " + buff.numberOfBytesWritten());

        ret = ret + "\nmethod Table Address=" +
                ((LFFactoryFacade) factory).createAddressUn(methodTableAddress).
                writeSelected(buff); //4
        //Miscellaneous.println("So far 3= " + buff.numberOfBytesWritten());

        ret = ret + "\n----\n";
        ret = ret + "\nnumber of non-Static fields=" +
                factory.createUn(numberOfNonStaticFields).trim(2).writeSelected(buff);
        //Miscellaneous.println("So far 4= " + buff.numberOfBytesWritten());

        ret = ret + "\nfield Table Address=" +
                ((LFFactoryFacade) factory).createAddressUn(fieldTableAddress).
                writeSelected(buff);
        //Miscellaneous.println("So far 5= " + buff.numberOfBytesWritten());

        ret = ret + "\n----\n";
        ret = ret + "\nnumber of static fields=" +
                factory.createUn(numberOfStaticFields).trim(2).writeSelected(buff);

        ret = ret + "\n\n------ START OF CONSTANT TABLES ------\n";
        Vector mathSizeNo = pOne.getSortedMathSizeNumber();

        ret = ret + "number of constant tables=" + factory.createUn(
                mathSizeNo.size() + 1).trim(1).writeSelected(buff) + "\n";
        //Miscellaneous.println("So far 6= " + buff.numberOfBytesWritten());

        ret = writeStringTable(buff, ret);
        //Miscellaneous.println("So far 7= " + buff.numberOfBytesWritten());

        ret = writeSelectedMath(buff, ret, mathSizeNo);
        //Miscellaneous.println("So far = " + buff.numberOfBytesWritten());

        ret = ret +
                "\n========================== End-of-CP-Header =========================\n\n\n";

        return ret;
    }

    private String writeStringTable(BufferedByteCountingOutputStream buff,
            String ret) throws Exception {
        ret = ret + "\n-----Strings-----\n";
        ret = ret + TYPES_MSG +
                factory.createUn(FieldTypes.TYPE_JCHAR).trim(1).writeSelected(buff) + "\n";
        ret = ret + "Size (in bytes)=" + factory.createUn(4).
                trim(1).writeSelected(buff) + "\n";
        ret = ret + "String table start Index=" +
                factory.createUn(stringTableStartIndex).trim(2).writeSelected(
                buff);
        ret = ret + "\n//(only in txt)number Of Strings=" +
                factory.createUn(numberOfstrings).trim(2);
        ret = ret + "\nAddress=" + ((LFFactoryFacade) factory).createAddressUn(stringTableAddress).writeSelected(buff); //4
        return ret;
    }

    private String writeSelectedMath(BufferedByteCountingOutputStream buff,
            String ret, Vector mathSizeNo) throws Exception {

        for (int loop = 0; loop < mathSizeNo.size(); loop++) {
            MathInfoSizeAndNumber mathInfoSizeAndNo =
                    (MathInfoSizeAndNumber) mathSizeNo.elementAt(loop);
            ret = ret + "\n---------" + MathInfoSizeAndNumber.tagToString(mathInfoSizeAndNo.getTag()) +
                    "_" + mathInfoSizeAndNo.getSize() + "--------\n";
            ret = ret + TYPES_MSG +
                    factory.createUn(FieldTypes.getConstantFieldType(mathInfoSizeAndNo.getTag())).
                    trim(1).writeSelected(buff) + "\n";
            ret = ret + "Size (in bytes)=" + factory.createUn(mathInfoSizeAndNo.getSize()).trim(1).writeSelected(buff) + "\n";
            ret = ret + "Start Index=" +
                    factory.createUn(mathInfoSizeAndNo.getStartIndex()).
                    trim(2).writeSelected(buff) + "\n";
            ret = ret + "Address =" +
                    SizeCalculator.getCPAddress(mathInfoSizeAndNo.getStartIndex(),
                    mathInfoSizeAndNo.getTag()).trim(LFFactoryFacade.getTrimAddressValue()).
                    writeSelected(buff) + "\n";
        }

        return ret;
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        throw new UnsupportedOperationException();
    }
}
