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
package takatuka.tukFormat.logic.util;

import java.io.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import java.util.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * It cacluates the size of LFBaseObject
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SizeCalculator {

    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private final static SizeCalculator sizeCalc = new SizeCalculator();

    protected SizeCalculator() {
        super();
    }

    public static SizeCalculator getInstanceOf() {
        return sizeCalc;
    }

    public void setAddress(int address, LFBaseObject obj) throws
            Exception {
        obj.setAddress(((LFFactoryFacade) factory).createAddressUn(
                address));
    }

    public Un cacluateAndCacheCPAddress(int startAddress) throws Exception {

        LFBaseObject obj = null;
        //two bytes represents size of controllerBase added at the top
        int totalSize = startAddress + 2;
        int cpIndex = 0;
        int poolId = 0;
        try {
            TreeSet poolIds = pOne.getPoolIds();
            Iterator poolIdsIt = poolIds.iterator();
            while (poolIdsIt.hasNext()) {
                poolId = (Integer) poolIdsIt.next();
                for (cpIndex = 0; cpIndex < pOne.getCurrentSize(poolId); cpIndex++) {
                    if (pOne.get(cpIndex, poolId) instanceof EmptyInfo) {
                        continue;
                    }
                    obj = (LFBaseObject) pOne.get(cpIndex, poolId);
                    setAddress(totalSize, obj);
                    totalSize += getObjectSize(obj);
                }
            }
        } catch (Exception d) {
            Miscellaneous.printlnErr("**** Error at CP index=" + cpIndex + ", pool-Id=" + poolId);
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ((LFFactoryFacade) factory).createAddressUn(totalSize);
    }

    public static int getObjectSuperSize(LFBaseObject obj) {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            BufferedByteCountingOutputStream bCount = new BufferedByteCountingOutputStream(
                    bOut);
            obj.superWriteSelected(bCount);
            return bCount.numberOfBytesWritten();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return 0;
    }

    public static int getObjectSize(BaseObject obj) {
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            BufferedByteCountingOutputStream bCount = new BufferedByteCountingOutputStream(
                    bOut);
            if (obj instanceof EmptyInfo) {
                return 0;
            }
            obj.writeSelected(bCount);
            return bCount.numberOfBytesWritten();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return 0;
    }

    public static int getNumberOfInfo(Un key) {
        return pOne.getCurrentSize(key.intValueUnsigned());
    }

    public static Un getCPAddress(int index, int tag) throws Exception {
        if (pOne.get(index, tag) instanceof EmptyInfo) {
            return factory.createUn(0).trim(2);
        }
        Un ret = ((LFBaseObject) pOne.get(index, tag)).getAddress();
        if (ret == null) {
            return ((LFFactoryFacade) factory).createAddressUn(0);
        } else {
            return ret;
        }
    }

    public static int getFirstInfoIndex(Un key) throws Exception {
        int ret = -1;
        int poolId = key.intValueUnsigned();
        int size = pOne.getCurrentSize(poolId);
        if (size > 0) {
            ret = 0;
            if (poolId == TagValues.CONSTANT_Class) {
                ret = 1;
            }
        }
        return ret;
    }
}
