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

import java.util.*;
import takatuka.classreader.logic.file.*;

public class FieldTypes {

    private static final String TYPES_FOR_CASTING_PROPERTY = "types.properties";
    private static final Properties typesForCastingProperties = PropertyReader.getInstanceOf().loadProperties(TYPES_FOR_CASTING_PROPERTY);
    private static final String TYPE_JCHAR_STR = "TYPE_JCHAR";
    private static final String TYPE_JBOOLEAN_STR = "TYPE_JBOOLEAN";
    private static final String TYPE_JBYTE_STR = "TYPE_JBYTE";
    private static final String TYPE_JSHORT_STR = "TYPE_JSHORT";
    private static final String TYPE_JREF_STR = "TYPE_JREF";
    private static final String TYPE_JINT_STR = "TYPE_JINT";
    private static final String TPYE_JFLOAT_STR = "TPYE_JFLOAT";
    private static final String TYPE_JDOUBLE_STR = "TYPE_JDOUBLE";
    private static final String TYPE_JLONG_STR = "TYPE_JLONG";
    public static final byte TYPE_JBYTE = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JBYTE_STR));
    public static final byte TYPE_JCHAR = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JCHAR_STR));
    public static final byte TYPE_JDOUBLE = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JDOUBLE_STR));
    public static final byte TYPE_JFLOAT = Byte.parseByte(typesForCastingProperties.getProperty(TPYE_JFLOAT_STR));
    public static final byte TYPE_JINT = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JINT_STR));
    public static final byte TYPE_JLONG = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JLONG_STR));
    public static final byte TYPE_JREF = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JREF_STR));
    public static final byte TYPE_JSHORT = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JSHORT_STR));
    public static final byte TYPE_JBOOLEAN = Byte.parseByte(typesForCastingProperties.getProperty(TYPE_JBOOLEAN_STR));

    public static Byte getConstantFieldType(int tag) {
        if (tag == TagValues.CONSTANT_Integer) {
            return TYPE_JINT;
        } else if (tag == TagValues.CONSTANT_Double) {
            return TYPE_JDOUBLE;
        } else if (tag == TagValues.CONSTANT_Float) {
            return TYPE_JFLOAT;
        } else if (tag == TagValues.CONSTANT_String) {
            return TYPE_JCHAR;
        } else if (tag == TagValues.CONSTANT_Long) {
            return TYPE_JLONG;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private FieldTypes() {
    }
}
