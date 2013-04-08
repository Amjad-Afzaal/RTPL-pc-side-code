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

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 *
 * <p>Description:
 *    CONSTANT_Long_info {
        u1 tag; //5
        u4 high_bytes;
        u4 low_bytes;
    }
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LongInfo extends MathInfoBase {
    private Un lowerBytes; //(4);
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    public LongInfo(byte tag) throws TagException, Exception {
        super(tag);
    }

    public void setLowerBytes(Un u4) throws Exception {
//todo        factory.createValidateUn().validateMathLowUnSize(u4);
        this.lowerBytes = u4;
    }

    public Un getLowerBytes() {
        return lowerBytes;
    }

    public int size() {
        return super.size() + lowerBytes.size();
    }

    public LongInfo() throws TagException, Exception {
        super(TagValues.CONSTANT_Long);
    }

    public String toString() {
        String ret = "";
        try {
            ret = ret + writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tLongInfo=";
        ret = ret + super.writeSelected(buff) + ", lowerBytes=" +
              lowerBytes.writeSelected(buff);
        return ret;
    }


    public boolean equals(Object obj) {

        if (obj == null || !(obj instanceof LongInfo)) {
            return false;
        }
        LongInfo longInfo = (LongInfo) obj;
        if (super.equals(obj) &&
            getLowerBytes().equals(longInfo.getLowerBytes())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(
                getLowerBytes()).toHashCode();
    }

}
