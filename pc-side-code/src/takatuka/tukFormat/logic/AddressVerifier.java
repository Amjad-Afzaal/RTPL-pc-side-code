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
package takatuka.tukFormat.logic;

import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.tukFormat.dataObjs.LFBaseObject;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.tukFormat.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * As Tuk file has addresses. The aim of this class is to verify addresses.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class AddressVerifier extends SizeCalculator {

    private static final AddressVerifier addVer = new AddressVerifier();
    
    public static AddressVerifier getInstanceOf () {
        return addVer;
    }
    
    @Override
    public void setAddress(int address, LFBaseObject obj) throws
            Exception {
        if (address != obj.getAddress().intValueUnsigned()) {
            Miscellaneous.printlnErr("address comparision, new address="+address+
                    " -- "+obj.getAddress().intValueUnsigned());
            throw new Exception("CP address ="+obj.toString());
        }
    }

    public void verifyCPAndHeaderAddresses() throws Exception {
        int totalSize = 0;
        CPHeader cpHeader = CPHeader.getInstanceof();
        //header size
        totalSize = getObjectSize(cpHeader);
        setAddress(totalSize, cpHeader);

        //cp size
        totalSize = cacluateAndCacheCPAddress(totalSize).intValueUnsigned();

        //class file size
        //it set addresses directly hence avoid it.
        //totalSize = ClassFileSizeCalculator.cacluateAndCacheClassFileAddress(totalSize).intValueUnsigned();
        
    }
}
