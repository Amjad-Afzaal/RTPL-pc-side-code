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
package takatuka.classreader.dataObjs.constantPool;

import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * Based on 4.4.3 http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#1221
 *
 The CONSTANT_String_info structure is used to represent constant objects of the type String:
    CONSTANT_String_info {
        u1 tag;
        u2 string_index;
    }
 The items of the CONSTANT_String_info structure are as follows:
 tag
    The tag item of the CONSTANT_String_info structure has the value CONSTANT_String (8).
 string_index
    The value of the string_index item must be a valid index into the constant_pool table. The constant_pool entry at that index must be a CONSTANT_Utf8_info (4.4.7) structure representing the sequence of characters to which the String object is to be initialized.
 * @author Faisal Aslam
 * @version 1.0
 */


public class StringInfo extends RegularInfoBase {
    public StringInfo() throws TagException, Exception {
        super(TagValues.CONSTANT_String);
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tStringInfo=";
        ret = ret + super.writeSelected(buff);
        return ret;
    }

}
