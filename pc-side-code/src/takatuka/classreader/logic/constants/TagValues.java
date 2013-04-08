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
package takatuka.classreader.logic.constants;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.exception.*;
import java.util.*;
import java.lang.reflect.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * This file represents table 4.3 (http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#87125)
 * The table has constant value for tags of constant pool  </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class TagValues {
    public static final byte CONSTANT_Class = 7;
    public static final byte CONSTANT_Fieldref = 9;
    //following Tag-value is used by TakaTuka only and not found in original JVM
    //specifications.
   // public static final byte CONSTANT_StaticFielddref = 17;
    public static final byte CONSTANT_Methodref = 10;
    //following Tag-value is used by TakaTuka only and not found in original JVM
    //specifications.
    //public static final byte CONSTANT_StaticMethodref = 16;
    public static final byte CONSTANT_InterfaceMethodref = 11;
    public static final byte CONSTANT_String = 8;
    public static final byte CONSTANT_Integer = 3;
    public static final byte CONSTANT_Float = 4;
    public static final byte CONSTANT_Long = 5;
    public static final byte CONSTANT_Double = 6;
    public static final byte CONSTANT_NameAndType = 12;
    public static final byte CONSTANT_Utf8 = 1;
    public static final byte CONSTANT_Empty = 15; //empty info is used to place on space in constant pool after long and double

    private static void validateTag(byte tag) throws TagException {
        if (tag != CONSTANT_Class && tag != CONSTANT_Fieldref &&
            tag != CONSTANT_Methodref && tag != CONSTANT_InterfaceMethodref &&
            tag != CONSTANT_String && tag != CONSTANT_Integer &&
            tag != CONSTANT_Float && tag != CONSTANT_Long &&
            tag != CONSTANT_Double && tag != CONSTANT_NameAndType &&
            tag != CONSTANT_Utf8 && tag != CONSTANT_Empty) {
            throw new TagException(tag);
        }
    }
    public static Vector getAllTagValues() {
        return ReflectionUtil.getClassFieldsValues(new TagValues());
    }
    
    public static void main (String args[]) {
        getAllTagValues();
    }
    
    public static void validateTag(int tag) throws TagException {
        validateTag((byte) tag);
    }

    public static void validateTag(Un tag) throws TagException {
        if (tag == null || tag.size() == 0) {
            return;
        }
        if (tag.size() != 1) {
            throw new TagException(
                    "Invalid tag size. It was not requal to 1 but " + tag.size());
        }
        byte data[] = tag.getData();
        validateTag(data[0]);
    }

    protected TagValues() {
        super();
    }
}
