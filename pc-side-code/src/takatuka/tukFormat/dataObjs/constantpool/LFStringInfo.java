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
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFStringInfo extends StringInfo implements LFBaseObject {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private Un address = null;
    private Un utf8Address = ((LFFactoryFacade)factory).createAddressUn(0);

    public LFStringInfo() throws TagException, Exception {
        super();
    }

    public Un getUTF8Address() {
        return utf8Address;
    }

    public void setUTF8Address(Un utf8Address) {
        this.utf8Address = utf8Address;
    }

    public Un getAddress() {
        return address;
    }

    public void setAddress(Un address) {
        this.address = address;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        
        return "\tLFStringInfo=" + utf8Address.trim(LFFactoryFacade.getTrimAddressValue()).
                writeSelected(buff)+ "//, address="+getAddress()+" utf8 index="+
                getIndex().intValueUnsigned();
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff);
    }
}
