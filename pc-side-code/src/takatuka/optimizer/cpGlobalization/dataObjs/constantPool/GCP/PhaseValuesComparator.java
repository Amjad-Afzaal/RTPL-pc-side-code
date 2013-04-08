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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP;

import java.util.*;

import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.tukFormat.dataObjs.constantpool.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * This class order the value based on their tags.
 * However how to order them is decided by a vector kept in this classes. Each element of
 * that vector will be tag and it could be updated externally. However, the class will define
 * a default order.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PhaseValuesComparator implements Comparator {

    private static final PhaseValuesComparator pOneValueComp =
            new PhaseValuesComparator();

    private PhaseValuesComparator() {
    }

    public static PhaseValuesComparator getInstanceOf() {
        return pOneValueComp;
    }

    /**
     * Compares its two arguments for order.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first
     *   argument is less than, equal to, or greater than the second.     *
     */
    public int compare(Object o1, Object o2) {
        GlobalizationRecord gRec1 = (GlobalizationRecord) o1;
        GlobalizationRecord gRec2 = (GlobalizationRecord) o2;

        Object record1 = gRec1.getObject();
        Object record2 = gRec2.getObject();

        if (record1 instanceof MathInfoBase) {
            return mathInfoCompare(gRec1, gRec2);
        } else if (record1 instanceof RegularInfoBase) {
            return regularBaseCompare(gRec1, gRec2);
        } else if (record1 instanceof UTF8Info) {
            return utf8Compare((UTF8Info) record1, (UTF8Info) record2);
        } else {
            return 0;
        }

    }

    /**
     * first comes class files then comes interfaces and lastly comes classfiles for arrays.
     * 
     * @param classCPIndex1
     * @param classCPIndex2
     * @param gRec1
     * @param gRec2
     * @param class1
     * @param class2
     * @return
     */
    private int sortClassInfo(int classCPIndex1, int classCPIndex2,
            GlobalizationRecord gRec1, GlobalizationRecord gRec2,
            Class class1, Class class2) {
        ClassInfo obj1 = (ClassInfo) gRec1.getObject();
        ClassInfo obj2 = (ClassInfo) gRec2.getObject();

        int sortingKey1 = classInfoGroupId(obj1);
        int sortingKey2 = classInfoGroupId(obj2);
        if (sortingKey1 == sortingKey2) {
            //same group the sort them with  number of time referred
            sortingKey1 = GlobalConstantPool.getGroupReferenceCount(classCPIndex1, class1);
            sortingKey2 = GlobalConstantPool.getGroupReferenceCount(classCPIndex2, class2);
        }
        return compareInt(sortingKey2, sortingKey1);
    }

    /**
     * Group 3: class that is not interfaces and are not belongs to array
     * Group 2: class that is an interface
     * Group 1: class that belongs to array
     *
     * @param cInfo
     * @return
     */
    private int classInfoGroupId(ClassInfo cInfo) {
        int ret = 3;
        if (cInfo.getIsInterface()) {
            ret = 2;
        }
        if (cInfo.getClassName().trim().startsWith("[")) {
            ret = 1;
        }
        return ret;
    }

    private int referenceCountCompare(int groupId1, int groupId2,
            GlobalizationRecord gRec1, GlobalizationRecord gRec2,
            Class class1, Class class2) {

        int sortingKey1 = groupId1;
        int sortingKey2 = groupId2;
        //if same group then sort them using individual reference count
        if (sortingKey1 == sortingKey2) {
            sortingKey1 = gRec1.getReferredCount();
            sortingKey2 = gRec2.getReferredCount();
        } else {
            if (gRec1.getObject() instanceof ClassInfo) {
                return sortClassInfo(groupId1, groupId2, gRec1, gRec2, class1, class2);
            }
            //if not same group then a group with highest total reference count should come first
            sortingKey1 = GlobalConstantPool.getGroupReferenceCount(groupId1, class1);
            sortingKey2 = GlobalConstantPool.getGroupReferenceCount(groupId2, class2);
            if (sortingKey1 == sortingKey2) { //if two groups have same group reference count then do not mix them up
                sortingKey1 = groupId1;
                sortingKey2 = groupId2;
            }
        }
        return compareInt(sortingKey2, sortingKey1);
    }

    private int mathInfoCompare(GlobalizationRecord gRec1, GlobalizationRecord gRec2) {
        MathInfoBase math1 = (MathInfoBase) gRec1.getObject();
        MathInfoBase math2 = (MathInfoBase) gRec2.getObject();

        return referenceCountCompare(math1.size(), math2.size(), gRec1, gRec2,
                math1.getClass(), math2.getClass());

    }

    private int regularBaseCompare(GlobalizationRecord gRec1,
            GlobalizationRecord gRec2) {
        RegularInfoBase regBase1 = (RegularInfoBase) gRec1.getObject();
        RegularInfoBase regBase2 = (RegularInfoBase) gRec2.getObject();
        int groupId1 = regBase1.getIndex().intValueUnsigned();
        int groupId2 = regBase2.getIndex().intValueUnsigned();
        if (regBase1 instanceof FieldRefInfo) {
            FieldRefInfo field1 = (FieldRefInfo) regBase1;
            FieldRefInfo field2 = (FieldRefInfo) regBase2;
            groupId1 = field1.isStatic ? GlobalConstantPool.STATIC_FIELD_GROUP_ID : groupId1;
            groupId2 = field2.isStatic ? GlobalConstantPool.STATIC_FIELD_GROUP_ID : groupId2;
            if (field1.isStatic && field2.isStatic) {
                return ((Integer) regBase1.getIndex().intValueUnsigned()).compareTo(regBase2.getIndex().intValueUnsigned());
            }
        }
        if (regBase1 instanceof LFMethodRefInfo || regBase1 instanceof InterfaceMethodRefInfo) {
            groupId1 = 0; //do not sort them with classindex just based on referencecount
            groupId2 = 0;
        }
        if (regBase1 instanceof NameAndTypeInfo) {
            return new NameAndTypeComparator().compare((NameAndTypeInfo) regBase1,
                    (NameAndTypeInfo) regBase1);
        }
        return referenceCountCompare(groupId1, groupId2, gRec1, gRec2,
                regBase1.getClass(), regBase2.getClass());

    }

    private int utf8Compare(UTF8Info utf1, UTF8Info utf2) {
        int ret = compareInt(utf1.getLength().intValueUnsigned(),
                utf2.getLength().intValueUnsigned());
        if (ret == 0) {
            //slow and useless ret = utf1.convertBytes().compareTo(utf2.convertBytes());
        }
        return ret;
    }

    public static int compareInt(int i, int j) {
        if (i == j) {
            return 0;
        } else if (i > j) {
            return 1;
        } else {
            return -1;
        }
    }
}
