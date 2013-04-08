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
public abstract class RegularInfoBase extends InfoBase {

    private Un index; //(2);

    public RegularInfoBase() throws TagException, Exception {
        super();
    }

    public RegularInfoBase(byte tag) throws TagException, Exception {
        super(tag);
    }

    public RegularInfoBase(Un tag) throws TagException {
        super(tag);
    }

    public void setIndex(int index) {
        try {
            FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
            this.index = factory.createUn(index).trim(2);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
    
    public void setIndex(Un u2) throws UnSizeException, Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        factory.createValidateUn().validateConsantPoolIndex(u2);
        this.index = u2;
    }

    public Un getIndex() {
        return index;
    }

    @Override
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

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        ret = ret + super.writeSelected(buff) + ", Index=" +
                index.writeSelected(buff);
        return ret;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RegularInfoBase)) {
            return false;
        }
        RegularInfoBase regBase = (RegularInfoBase) obj;
        if (super.equals(obj) && getIndex().equals(regBase.getIndex())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(getIndex()).
                toHashCode();
    }
}
