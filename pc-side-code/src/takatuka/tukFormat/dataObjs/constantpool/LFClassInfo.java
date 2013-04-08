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
package takatuka.tukFormat.dataObjs.constantpool;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFClassInfo extends ClassInfo implements LFBaseObject {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private Un address = null;
    private Un classFileAddress = ((LFFactoryFacade)factory).createAddressUn(0);

    public LFClassInfo() throws TagException, Exception {
        super();
    }

    public Un getClassFileAddress() {
        return classFileAddress;
    }

    public void setClassFileAddress(Un classFileAddress) {
        this.classFileAddress = classFileAddress;
    }

    @Override
    public void setAddress(Un address) {
        this.address = address;
    }

    @Override
    public Un getAddress() {
        return address;
    }

/*    @Override
    public String getClassName() {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        if (!GlobalConstantPool.isExecuted) {
            return "CANNOT_INDEX_CLASS_NAME";
        }
        UTF8Info utf8 = (UTF8Info) pOne.get(getIndex().intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        return utf8.convertBytes();
    }
*/
    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tLFClassInfo=";
        ret = ret + classFileAddress.trim(LFFactoryFacade.getTrimAddressValue()).
                writeSelected(buff) + "\t//" +
                getClassName()+ ", address="+getAddress()+ ", isInterface="+getIsInterface();
        return ret;
    }
}
