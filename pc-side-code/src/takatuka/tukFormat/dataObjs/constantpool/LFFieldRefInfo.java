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
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFFieldRefInfo extends FieldRefInfo implements LFBaseObject {

    protected FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private Un address = null; //phy address in the constant pool
   
    protected Un fmAddress = factory.createUn(0);

    public Un getFieldMethodInfoAddress() {
        return fmAddress.trim(3);
    }

    public void setFieldMethodInfoAddress(Un fmAddress) {
        if (fmAddress == null) {
            return;
        }
        this.fmAddress = fmAddress;
    }

    public void setAddress(Un address) {
        this.address = address;
    }

    public Un getAddress() {
        return address;
    }

    public LFFieldRefInfo() throws TagException, Exception {
        super();
    }

    public String getClassName() throws Exception {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        if (!GlobalConstantPool.isExecuted) {
            return "CANNOT_INDEX_CLASS";
        }
        BaseObject obj =(BaseObject) pOne.get(getIndex().
                intValueUnsigned(), TagValues.CONSTANT_Class);
        if (obj instanceof EmptyInfo) {
            return "";
        }
        return ((LFClassInfo) obj).getClassName();
    }

    public String getMethodName() throws Exception {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        if (!GlobalConstantPool.isExecuted) {
            return "CANNOT_INDEX_METHOD_NAME";
        }
        NameAndTypeInfo nameAndType = (NameAndTypeInfo) pOne.get(
                getNameAndTypeIndex().intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        return ((UTF8Info) pOne.get(nameAndType.getIndex().intValueUnsigned(),
                TagValues.CONSTANT_Utf8)).convertBytes();
    }

    public String getMethodDescription() throws Exception {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        if (!GlobalConstantPool.isExecuted) {
            return "CANNOT_INDEX_METHOD_DESC";
        }

        NameAndTypeInfo nameAndType = (NameAndTypeInfo) pOne.get(
                getNameAndTypeIndex().intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        UTF8Info utf8 = (UTF8Info) pOne.get(nameAndType.getDescriptorIndex().
                intValueUnsigned(), TagValues.CONSTANT_Utf8);
        return utf8.convertBytes();
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        //caclulateAddress(getIndex());
        String ret = "\tFieldRefInfo=";
        ret += getFieldMethodInfoAddress().writeSelected(buff);
        if (isStatic) {
            //also show class index
            ret += getIndex().writeSelected(buff);
        }    
        ret +=  "\t//isStatic=" + isStatic +
                ", " + getClassName() +
                "->(name=" + getMethodName() + ", Description=" +
                getMethodDescription() + ")"+ ", address="+getAddress();
        return ret;
    }
}
