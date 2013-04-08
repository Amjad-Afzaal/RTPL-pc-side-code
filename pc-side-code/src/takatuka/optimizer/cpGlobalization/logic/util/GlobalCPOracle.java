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
package takatuka.optimizer.cpGlobalization.logic.util;

import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.tukFormat.dataObjs.constantpool.LFFieldRefInfo;

/**
 *
 * Description:
 * <p>
 *
 * It has all the utility function to add field and methods in the class files
 * to the constant pool.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class GlobalCPOracle {

    private FactoryFacade factory =
            FactoryPlaceholder.getInstanceOf().getFactory();
    private GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private static final GlobalCPOracle cfileToCP = new GlobalCPOracle();

    private GlobalCPOracle() {
    }

    final static GlobalCPOracle getInstanceOf() {
        return cfileToCP;
    }

    /**
     * It add a fieldInfo in the CP by creating a FieldRefInfo. In case field already
     * exist then it return the nameIndex of already exisiting field.
     *       
     * @param field
     * @param isMethod
     * @param cFileIndex
     * @return
     * return -1 if the field already exist in CP otherwise, return the index of newly added field.
     */
    final int addFieldorMethodInfoInCP(FieldInfo field, boolean isMethod, int cFileIndex) {
        //UTF8Info name = (UTF8Info)pOne.get(field.getNameIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8);
        //Miscellaneous.println("name of method/field ="+name);

        int fieldAlreadyInCP = existFieldInfoCPIndex(field, isMethod, cFileIndex);
        //UTF8Info nameUtf8 = (UTF8Info) pOne.get(field.getNameIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8);

        if (fieldAlreadyInCP != -1) {
            return -1; //already there. No need to be added
        }
        ReferenceInfo refInfo = createReferenceInfo(field, isMethod, cFileIndex);
        if (field.getAccessFlags().isStatic() && (refInfo instanceof FieldRefInfo)) {
            ((FieldRefInfo) refInfo).isStatic = true;
        }
        return addReferenceInfoInCP(refInfo, isMethod);
    }

    /**
     * adds a fields in a classfile if not already exist and also add the field in CP
     *
     * @param fieldName
     * @param fieldDescription
     * @param cFile
     * @param isMethod
     * @param accessFlags
     * return -1 if field already exist in classFile. Otherwise return CP index of the field.
     */
    final int addFieldInfoInClassFileAndCP(String fieldName, String fieldDescription,
            ClassFile cFile, boolean isMethod, Un accessFlags) {
        FieldInfo field = Oracle.getInstanceOf().getMethodOrField(cFile, fieldName,
                fieldDescription, isMethod);
        if (field != null) {
            return -1;
        }
        int nameIndex = checkAndAddUTF8(fieldName);
        int descIndex = checkAndAddUTF8(fieldDescription);
        field = addFieldorMethodInfoInClassFile(nameIndex, descIndex, cFile, isMethod, accessFlags);
        int cfileIndex = cFile.getThisClass().intValueUnsigned();
        int ret = existFieldInfoCPIndex(field, isMethod, cfileIndex);
        if (ret == -1) {
            ret = addFieldorMethodInfoInCP(field, isMethod, cfileIndex);
        }
        Oracle.getInstanceOf().clearMethodCodeAttAndClassFileCache();

        return ret;
    }

    final int getFieldCPIndex(String fieldName, String fieldDescription,
            ClassFile cFile, boolean isMethod) {
        FieldInfo field = Oracle.getInstanceOf().getMethodOrField(cFile, fieldName,
                fieldDescription, isMethod);
        if (field == null) {
            return -1;
        }
        int cfileIndex = cFile.getThisClass().intValueUnsigned();
        return existFieldInfoCPIndex(field, isMethod, cfileIndex);
    }

    private final FieldInfo addFieldorMethodInfoInClassFile(int nameCPIndex,
            int descrCPIndex, ClassFile cFile, boolean isMethod, Un accessFlags) {
        FieldInfo field = null;
        try {
            field = factory.createFieldInfo(cFile);
            if (isMethod) {
                field = factory.createMethodInfo(cFile);
            }
            field.setDescriptorIndex(factory.createUn(descrCPIndex).trim(2));
            field.setNameIndex(factory.createUn(nameCPIndex).trim(2));
            field.setAttributeCount(factory.createUn(0).trim(2));
            field.setAccessFlags(accessFlags);
            ControllerBase contr = cFile.getMethodInfoController();
            if (!isMethod) {
                contr = cFile.getFieldInfoController();
            }
            int maxSize = contr.getMaxSize();
            contr.setMaxSize(maxSize + 1);
            contr.add(field);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return field;
    }

    final String getClassInfoName(Un thisPointer) {
        ClassInfo cInfo = (ClassInfo) pOne.get(thisPointer.intValueUnsigned(), TagValues.CONSTANT_Class);
        int classNameIndex = cInfo.getIndex().intValueUnsigned();
        UTF8Info utf8 = (UTF8Info) pOne.get(classNameIndex, TagValues.CONSTANT_Utf8);
        return utf8.convertBytes();
    }

    final int getUTF8InfoIndex(String name) {
        int size = pOne.getCurrentSize(TagValues.CONSTANT_Utf8);
        for (int loop = 0; loop < size; loop++) {
            UTF8Info utf8Info = (UTF8Info) pOne.get(loop, TagValues.CONSTANT_Utf8);
            if (utf8Info.convertBytes().equals(name)) {
                return loop;
            }
        }
        return -1;
    }

    private final int addUTF8Info(String name) {
        try {
            UTF8Info utf8 = factory.createUTF8Info();
            byte[] data = name.getBytes();
            utf8.setBytes(factory.createUn(data));
            utf8.setLength(factory.createUn(data.length).trim(2));
            return pOne.add(utf8, TagValues.CONSTANT_Utf8);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return -1;
    }

    private int checkAndAddUTF8(String newName) {
        int index = getUTF8InfoIndex(newName);
        if (index == -1) {
            index = addUTF8Info(newName);
        }
        return index;
    }

    private int checkAndAddNameAndTypeInfo(int nameIndex, int descriptionIndex) {
        try {
            Un descIndex = factory.createUn(descriptionIndex).trim(2);
            return checkAndAddNameAndTypeInfo(nameIndex, descIndex);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return -1;
    }

    private int checkNameAndTypeInfoInCP(NameAndTypeInfo nAT) {
        int size = pOne.getCurrentSize(TagValues.CONSTANT_NameAndType);
        for (int loop = 0; loop < size; loop++) {
            NameAndTypeInfo nATypeInfo = (NameAndTypeInfo) pOne.get(loop, TagValues.CONSTANT_NameAndType);
            if (nATypeInfo.equals(nAT)) {
                return loop;
            }
        }
        return -1;
    }

    private int checkAndAddNameAndTypeInfo(int nameIndex, Un descriptionIndex) {
        NameAndTypeInfo nAt = createNameAndType(nameIndex, descriptionIndex);
        int nAtIndex = checkNameAndTypeInfoInCP(nAt);
        if (nAtIndex == -1) {
            nAtIndex = addNameAndType(nAt);
        }
        return nAtIndex;
    }

    final void renameFieldInfo(FieldInfo fieldInfo, int nameAndTypeIndex) {
        try {
            NameAndTypeInfo nAt = (NameAndTypeInfo) pOne.get(nameAndTypeIndex,
                    TagValues.CONSTANT_NameAndType);
            fieldInfo.setNameIndex(nAt.getIndex());
            fieldInfo.setDescriptorIndex(nAt.getDescriptorIndex());
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    final void renameFieldInfo(FieldInfo fieldInfo, String newName) {
        int nameAndTypeIndex = checkAndAddNameAndTypeInfo(checkAndAddUTF8(newName), fieldInfo.getDescriptorIndex());
        renameFieldInfo(fieldInfo, nameAndTypeIndex);
    }

    /**
     * rename fieldRefInfo
     * return nameAndType Index
     * @param cpIndexFieldRef
     * @param newName
     * @return
     */
    final int renameFieldRefInfo(int cpIndexFieldRef, String newName) {
        //first check if newName is already in CP
        //if No then add it
        //now check if nameAndType with new name and old description is in the CP
        //if No then add it
        //now change the nameAndType of the current entry of the constant pool
        //also rename the corresponding FieldInfo
        int nAtIndex = 0;
        if (cpIndexFieldRef == -1) {
            return -1;
        }
        try {
            int nameIndex = checkAndAddUTF8(newName);
            FieldRefInfo fRef = (FieldRefInfo) pOne.get(cpIndexFieldRef,
                    TagValues.CONSTANT_Fieldref);
            NameAndTypeInfo nAt = (NameAndTypeInfo) pOne.get(fRef.getNameAndTypeIndex().intValueUnsigned(),
                    TagValues.CONSTANT_NameAndType);
            nAtIndex = checkAndAddNameAndTypeInfo(nameIndex,
                    nAt.getDescriptorIndex());
            fRef.setNameAndType(factory.createUn(nAtIndex).trim(2));

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return nAtIndex;
    }

    final int addReferenceInfoInCP(ReferenceInfo refInfo, boolean isMethod) {
        int ret = -1;
        int poolId = TagValues.CONSTANT_Fieldref;
        if (isMethod) {
            poolId = TagValues.CONSTANT_Methodref;
        }
        //following code need a review.
        Un thisClass = refInfo.getIndex();
        ClassFile.currentClassToWorkOn = Oracle.getInstanceOf().getClass(thisClass, pOne);
        ret = pOne.add(refInfo, poolId);
        return ret;
    }

    /**
     * return -1 if the field does not exist. otherwise it nameIndex
     * @param field
     * @param isMethod
     * @param cFileIndex
     * @return
     */
    final int existFieldInfoCPIndex(FieldInfo field, boolean isMethod, int cFileIndex) {
        int ret = -1;
        try {
            int nAtIndex = getNameAndType(field);
            if (nAtIndex == -1) {
                return -1;
            }

            ReferenceInfo refInfo = factory.createFieldRefInfo();
            if (isMethod) {
                refInfo = factory.createMethodRefInfo();
            }
            refInfo.setNameAndType(factory.createUn(nAtIndex).trim(2));
            refInfo.setIndex(factory.createUn(cFileIndex).trim(2));
            return existReferenceCPIndex(refInfo, isMethod);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    final int existReferenceCPIndex(ReferenceInfo refInfo, boolean isMethod) {
        int tag = isMethod ? TagValues.CONSTANT_Methodref : TagValues.CONSTANT_Fieldref;
        return pOne.getAll(tag).indexOf(refInfo);
    }

    /**
     * In case a nameAndType already exist then it returns it otherwise,
     * it return -1
     * @param field
     * @return
     */
    private int getNameAndType(FieldInfo field) {
        NameAndTypeInfo nameAndType = createNameAndType(field);
        int tag = TagValues.CONSTANT_NameAndType;
        return pOne.getAll(tag).indexOf(nameAndType);
    }

    /**
     *
     * @param nameIndex
     * @param descIndex
     * @return
     */
    final int findNameAndType_GCP(int nameIndex, int descIndex) {
        int size = pOne.getCurrentSize(TagValues.CONSTANT_NameAndType);
        for (int loop = 0; loop < size; loop ++) {
           NameAndTypeInfo nAT = (NameAndTypeInfo) pOne.get(loop, TagValues.CONSTANT_NameAndType);
           int nATNameIndex = nAT.getIndex().intValueUnsigned();
           int nATDescIndex = nAT.getDescriptorIndex().intValueUnsigned();
           if (nATNameIndex == nameIndex && nATDescIndex == descIndex) {
               return loop;
           }
        }
        return -1;
    }

    private NameAndTypeInfo createNameAndType(int nameIndex, Un uDescIndex) {
        NameAndTypeInfo nameAndType = null;
        try {
            Un uNameIndex = factory.createUn(nameIndex).trim(2);
            nameAndType = factory.createNameAndTypeInfo();
            nameAndType.setDescriptorIndex(uDescIndex);
            nameAndType.setIndex(uNameIndex);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return nameAndType;
    }

    /**
     * It creates a nameAndType for a field
     * @param field
     * @return
     */
    final NameAndTypeInfo createNameAndType(FieldInfo field) {
        NameAndTypeInfo nameAndType = null;
        try {
            Un uNameIndex = field.getNameIndex();
            Un uDescIndex = field.getDescriptorIndex();
            nameAndType = factory.createNameAndTypeInfo();
            nameAndType.setDescriptorIndex(uDescIndex);
            nameAndType.setIndex(uNameIndex);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return nameAndType;
    }

    private int addNameAndType(NameAndTypeInfo nAt) {
        int ret = pOne.add(nAt, TagValues.CONSTANT_NameAndType);
        return ret;

    }

    private int addNameAndType(FieldInfo field) {
        int ret = getNameAndType(field);
        if (ret != -1) {
            return ret; //already there no need to be added
        }
        NameAndTypeInfo nAt = createNameAndType(field);
        return addNameAndType(nAt);
    }

    private ReferenceInfo createReferenceInfo(FieldInfo field,
            boolean isMethod, int classRefCPIndex) {
        ReferenceInfo refInfo = null;
        try {
            if (isMethod) {
                refInfo = factory.createMethodRefInfo();
            } else {
                refInfo = factory.createFieldRefInfo();
            }
            int nAtIndex = getNameAndType(field);
            if (nAtIndex == -1) {
                nAtIndex = addNameAndType(field);

            }
            refInfo.setNameAndType(factory.createUn(nAtIndex).trim(2));
            refInfo.setIndex(factory.createUn(classRefCPIndex).trim(2));
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return refInfo;
    }

    private void recordUnSortedIndexes(Vector vec, HashMap map) {
        for (int loop = 0; loop < vec.size(); loop++) {
            map.put(vec.elementAt(loop), loop);
        }
    }
}
