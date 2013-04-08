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
package takatuka.verifier.dataObjs;

import takatuka.classreader.logic.constants.TagValues;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public interface TypeConstants {

    public static final short INTEGER = TagValues.CONSTANT_Integer; //Byte, char, boolean, short all are mapped to this value for validation
    public static final short FLOAT = TagValues.CONSTANT_Float;
    public static final short DOUBLE = TagValues.CONSTANT_Double;
    public static final short LONG = TagValues.CONSTANT_Long;
    public static final short STRING = TagValues.CONSTANT_String;
    
    public static final short VOID = 16;
    public static final short RETURN_ADDRESS = VOID + 1;
    public static final short SPECIAL_TAIL = RETURN_ADDRESS + 1;
    public static final short UNUSED = SPECIAL_TAIL + 1;

    public static final short BYTE = UNUSED+1;
    public static final short CHAR = BYTE+1;
    public static final short SHORT = CHAR+1;
    public static final short BOOLEAN = SHORT+1;
    public static final short BYTE_BOOLEAN = BOOLEAN+1;

    public static final short NULL = Short.MAX_VALUE;
}
