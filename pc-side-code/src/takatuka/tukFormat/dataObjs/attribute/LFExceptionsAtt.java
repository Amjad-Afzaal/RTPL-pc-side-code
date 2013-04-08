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
package takatuka.tukFormat.dataObjs.attribute;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFExceptionsAtt extends ExceptionsAtt {
    public LFExceptionsAtt() {
        super();
    }

    public LFExceptionsAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                           Un u2_numberOfExceptions) throws Exception {
        super(u2_attrNameIndex, u4_attributeLength, u2_numberOfExceptions);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        if (true) {
            return ""; //Gidon says they do not need this one. It is "boring". Do you know what boring means? :)
        }
        String ret = "\tExceptionsAtt=";
        ret = ret + ", number_of_exceptions (int)=" +
              getNumberOfExceptions().writeSelected(buff);
        ret = ret + ", Indexes{";
        int size = getNumberOfExceptions().intValueUnsigned();
        for (int loop = 0; loop < size; loop++) {
            ret = ret + ((Un) getExceptionIndexAt(loop)).
                  writeSelected(buff);
            if (loop != (size - 1)) {
                ret = ret + ", ";
            }
        }
        ret = ret + "}";
        return ret;
    }

}
