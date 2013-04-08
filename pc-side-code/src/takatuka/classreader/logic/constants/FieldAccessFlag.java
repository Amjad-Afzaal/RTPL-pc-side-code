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

import takatuka.classreader.logic.exception.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.5 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public final class FieldAccessFlag {
    private FieldAccessFlag() {
        super();
    }

    public static int ACC_PUBLIC = 0x0001; //  Declared public; may be accessed from outside its package.
    public static int ACC_PRIVATE = 0x0002; //Declared private; usable only within the defining class.
    public static int ACC_PROTECTED = 0x0004; //	Declared protected; may be accessed within subclasses.
    public static int ACC_STATIC = 0x0008; // 	Declared static.
    public static int ACC_FINAL = 0x0010; // 	Declared final; no further assignment after initialization.
    public static int ACC_VOLATILE = 0x0040; // 	Declared volatile; cannot be cached.
    public static int ACC_TRANSIENT = 0x0080; // 	Declared transient; not written or read by a persistent object manager.


    /**
     * Fields of classes may set any of the flags in Table 4.4.
     * However, a specific field of a class may have at most one of its
     * ACC_PRIVATE, ACC_PROTECTED, and ACC_PUBLIC flags set (2.7.4) and may not
     * have both its ACC_FINAL and ACC_VOLATILE flags set (2.9.1).
     */
    public void validateAccessFlag() throws FieldAttributeException {
        //todo
    }
}
