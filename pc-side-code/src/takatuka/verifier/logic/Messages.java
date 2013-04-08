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
package takatuka.verifier.logic;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * All the messages for verification are listed here.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class Messages {

    private Messages() {
        super();
    }

    //Access Verification
    public static final String INVALID_CLASS_ACCESS =
            "Invalid access privilages to a class";
    public static final String INVALID_METHODFIELD_ACCESS =
            "Invalid method or field privilages to a class";

    //constant pool static check
    public static final String INVALID_CONSTANT_POOL =
            "Invalid Global Constant Pool Entry, at index #";

    //pass 1
    public static final String MAGIC_NUMBER_ILLEGAL =
            "Illegal class file magic number";

    //pass 2
    public static final String FINAL_SUPERCLASS =
            "A final class cannot be extended";
    public static final String NO_SUPER_CLASS =
            "Each class, other than Object, must have a valid super class";
    public static final String SUPER_FINAL_METHOD =
            "A final method cannot be overridden";

    //pass 3
    public static final String NON_NULL_CODEATT =
            "A native and abstract method is not allowed to have code";
    public static final String NULL_CODEATT =
            "Code Attribtue cannot be null when a function is neither native nor abstract";
    public static final String INOVKE_VIRTUAL_VERIFICATION =
            "InvokeVirtual is used with either a <init> or <clinit>";
    public static final String INOVKE_SPECIAL_VERIFICATION = "????";
    public static final String INOVKE_STATIC_VERIFICATION =
            "InvokeStatic is used with either non-static or abstract method";
    public static final String INOVKE_INTERFACE_VERIFICATION =
            "InvokeInterface is used when method was not of an interface";
    //pass4
    public static final String REFERENCE_NOT_EXIST =
            "Invalid constant pool index";
    public static final String METHOD_NOT_EXIST = "Method not found in a class";
    public static final String FIELD_NOT_EXIST = "FIELD not found in a class";

    //Data Flow Analyzer
    public static final String STACK_INVALID = "Invalid Stack Value(s)";
    public static final String LOCALVAR_INVALID = "Invalid Local Variable";
    public static final String INVALID_BYTECODE =
            "Invalid bytecode instruction ";
    public static final String CP_INDEX_INVALID =
            "Invalid Run Time Constant Pool Index";
    public static final String INVALID_FUNCTION_ARGUMENTS =
            "Invalid function arguments";


    //operand stack
    public static final String STACK_SIZE_EXCEEDS_MAX =
            "Operand-Stack size grows than maximum limit";
    public static final String EMPTY_STACK_POPPED =
            "Empty Operand-Stack was popped";
    public static final String STACK_MERGE_ERROR =
            "Invalid stack: Merging stack failed";

    //local variable
    public final static String LOCAL_VAR_SIZE_UPPER_LIMIT =
            "Local variables size grows than maximum limit";
    public final static String LOCAL_VAR_UNUSED =
            "Invalid access to local variable";
    public final static String LOCAL_VAR_MERGE =
            "Invalid local variables: Merging local variable failed";

}
