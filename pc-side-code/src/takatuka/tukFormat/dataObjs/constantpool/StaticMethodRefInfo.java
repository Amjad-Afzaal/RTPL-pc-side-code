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
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.factory.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class StaticMethodRefInfo implements LFBaseObject, Comparable<StaticMethodRefInfo> {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private Un clinitAddress = factory.createUn(0).trim(LFFactoryFacade.getTrimAddressValue());
    private Un myAddress = factory.createUn(0).trim(LFFactoryFacade.getTrimAddressValue());
    private int classIndex = 0;
    private int cpIndex = 0;

    public StaticMethodRefInfo(Un address, int classIndex, int cpIndex) throws Exception {
        this.clinitAddress = address;
        this.classIndex = classIndex;
        this.cpIndex = cpIndex;
    }

    public int getConstantPoolIndex() {
        return cpIndex;
    }
    
    public Un getClinitAddress() {
        return this.clinitAddress;
    }

    public int getClassFileCPIndex() {
        return this.classIndex;
    }

    public Un getAddress() {
        return myAddress;
    }

    public void setAddress(Un address) {
        this.myAddress = address;
    }

    public String superWriteSelected(BufferedByteCountingOutputStream buff) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws Exception {
        return "at dummy index="+factory.createUn(cpIndex).trim(2)+
                ", Dummy-MethodRefInfo clinitAddress=" + clinitAddress.writeSelected(buff) +
                ", (comment-only)classIndex =" + classIndex;
    }

    public int compareTo(StaticMethodRefInfo input) {
        return ((Integer) (classIndex)).compareTo(input.classIndex);
    }
    
    @Override
    public String toString() {
        try {
        return writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }
}
