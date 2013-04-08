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

/**
 * <p>Title: </p>
 * <p>Description:
 * As explained in section 4.7 at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 * An attribute_info has attribute_name_index, the value of that index (in the constant pool) tells us the kind
 * of attribute. This class has collection of those constant values to be used else where....
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class AttributeNameIndexValues {
    private AttributeNameIndexValues() {
        super();
    }

    public static String CONSTANT_VALUE = "ConstantValue";
    public static String CODE = "Code";
    public static String EXCEPTION = "Exceptions";
    public static String INNERCLASSES = "InnerClasses";
    public static String SYNTHETIC = "Synthetic";
    public static String SOURCE_FILE = "SourceFile";
    public static String LINE_NUMBER_TABLE = "LineNumberTable";
    public static String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    public static String DEPRECATED = "Deprecated";

    public static void validateAttributeNameIndexValue(String value) throws
            Exception {
        if (value.equals(CONSTANT_VALUE) || value.equals(CODE) &&
            value.equals(EXCEPTION) && value.equals(INNERCLASSES) &&
            value.equals(SYNTHETIC) && value.equals(SOURCE_FILE) &&
            value.equals(LINE_NUMBER_TABLE) &&
            value.equals(LOCAL_VARIABLE_TABLE) &&
            value.equals(DEPRECATED)) {
            throw new Exception("Attribute Name Index Value Exception, value= " +
                                value);
        }
    }

}
