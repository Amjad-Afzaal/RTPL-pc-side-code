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
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFUTF8Info extends UTF8Info implements LFBaseObject {

    private boolean keep = false;
    private Un address = null;

    public Un getAddress() {
        return address;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }

    public void setAddress(Un address) {
        this.address = address;
    }

    public LFUTF8Info() throws TagException, Exception {
        super();
    }

    public LFUTF8Info(Un tag) throws TagException {
        super(tag);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        if (!keep) {
            return "\tUTF8Info= //" + convertBytes()+ ", address="+ getAddress();
        }
        String ret = "\tUTF8Info=";
        ret = ret + ", length =" + getLength().writeSelected(buff) + ", bytes=";
        LFRevUn.bigEndian = true;
        ret = ret + getBytes().writeSelected(buff) + " \t//" + convertBytes() +
                "";
        LFRevUn.bigEndian = false;
        ret = ret +" address="+getAddress();
        return ret;
    }

    @Override
    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return super.writeSelected(buff)+ ",// address="+getAddress();
    }
}
