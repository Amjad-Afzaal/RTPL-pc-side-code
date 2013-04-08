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
 * <p>Description:
 * Section 4.4.4 http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 CONSTANT_Integer_info {
        u1 tag; //3
        u4 bytes; //The bytes
        // item of the CONSTANT_Integer_info
        // structure represents the value of the int constant. The bytes of the
       // value are stored in big-endian (high byte first) order.
    }

 *  </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class IntegerInfo extends MathInfoBase {
    public IntegerInfo() throws TagException, Exception {
        super(TagValues.CONSTANT_Integer);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tIntegerInfo=";
        ret = ret + super.writeSelected(buff) +// "\t\\\\ value="+ super.getUpperBytes().intValue()+
                ", size="+super.size();
        return ret;
    }


}
