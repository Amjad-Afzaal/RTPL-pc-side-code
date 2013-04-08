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
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ClassFileSizeCalculator extends SizeCalculator {

    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();

    public ClassFileSizeCalculator() {
        super();
    }

    /**
     * first calculate size of header of class file. It has meta informtion.
     * then calcuate size of fields. It has inforamtion start_fild and field_count basically only 4 bytes
     * then calculate size method.
     * It also supposed to set MethodInfo addresses. These address are later used by MethodRefInfo too.
     * @param startAddress int
     * @return Un
     * @throws Exception
     */
    public static Un cacluateAndCacheClassFileAddress(int startAddress) throws
            Exception {
        LFClassFile.startAddressForAllClasses = startAddress;
        ClassFileController cont = ClassFileController.getInstanceOf();
        LFClassFile obj = null;
        int totalSize = startAddress;
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        BufferedByteCountingOutputStream bCount = new BufferedByteCountingOutputStream(bOut);

        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            obj = (LFClassFile) cont.get(loop);
            totalSize = bCount.numberOfBytesWritten() + startAddress;
            obj.setAddress(((LFFactoryFacade) factory).createAddressUn(
                    totalSize));

            obj.writeSelected(bCount);

        }
        totalSize = bCount.numberOfBytesWritten() + startAddress;
        LFClassFile.startAddressForAllClasses = 0;
        return ((LFFactoryFacade) factory).createAddressUn(totalSize);
    }
}
