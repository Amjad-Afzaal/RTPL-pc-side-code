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
package takatuka.classreader.dataObjs.constantPool.base;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class MathInfoBase extends InfoBase {

    private Un bytes; //(4);
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    public MathInfoBase(byte tag) throws TagException, Exception {
        super(tag);
    }

    public MathInfoBase(Un tag) throws TagException {
        super(tag);
    }

    public MathInfoBase() throws TagException, Exception {
        super();
    }

    public void setUpperBytes(Un u4) throws Exception {
        //todo factory.createValidateUn().validateMathHighUnSize(u4);
        this.bytes = u4;
    }

    public Un getUpperBytes() {
        return bytes;
    }

    public int size() {
        return bytes.size();
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
        String ret = "";
        ret = ret + super.writeSelected(buff) + ", upperBytes=" +
              bytes.writeSelected(buff);
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MathInfoBase)) {
            return false;
        }
        MathInfoBase mathBase = (MathInfoBase) obj;
        if (super.equals(obj) &&
            getUpperBytes().equals(mathBase.getUpperBytes())) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(
                getUpperBytes()).toHashCode();
    }

}
