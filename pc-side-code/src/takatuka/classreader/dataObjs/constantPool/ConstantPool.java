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
package takatuka.classreader.dataObjs.constantPool;


import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description: As the name implies this file represents the ConstantPool
 * (see http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#20080) </p>
 * All constant_pool table entries have the following general format:
    cp_info {
       ....
    }
 * @author Faisal Aslam
 * @version 1.0
 */



public class ConstantPool extends MultiplePoolsFacade {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();
    
    public ConstantPool(TreeSet poolIds, int maxSize) {
        super(poolIds, maxSize);
    }
    public ConstantPool() {
        super.setAllowedClassName(InfoBase.class, DEFAULT_SINGLE_POOP_ID);
    }

   


    private ConstantPool(int constant_pool_count) {
        super(constant_pool_count - 1); //size is one less than constant_pool_count per documentation
        super.setAllowedClassName(InfoBase.class, DEFAULT_SINGLE_POOP_ID);
    }

    protected InfoBase getObject(Object obj) {
        return (InfoBase) obj;
    }


    @Override
    public int add(Object infoBase, int poolId) throws Exception {
        int ret = super.add(infoBase, poolId);
//        super.recordIndex(getCurr);
        if (infoBase instanceof LongInfo || infoBase instanceof DoubleInfo) {
            /* "if a CONSTANT_Long_info or CONSTANT_Double_info structure is the
             item in the constant_pool table at index n, then the next usable item
             in the pool is located at index n+2. The constant_pool index n+1 must
             be valid but is considered unusable"
             */
             super.add(new EmptyInfo(), poolId); // per above documentation
        }
        return ret;
    }

    @Override
    public int indexOf(Object elem, int start, int poolId) {
        return super.indexOf(elem, start - 1, poolId);
    }

    @Override
    public Object remove(int index, int poolId) {
        Object ret = super.remove(index - 1, poolId);
        //removeIndex(index-1);
        return ret;
    }

    @Override
    public int indexOf(Object elem, int poolId) {
        int ret = -1;
        if ((ret = super.indexOf(elem, poolId)) == -1) {
            return -1;
        }
        return ret + 1;
    }

    @Override
    public Object get(int index, int poolId) {
        if (index < 1 || index > getMaxSize()) { // per documentation
            throw new VerifyError(
                    "index cannot be less than 1 or greater than constantpoollength. You have used=" +
                    index + ", max-size was=" + getMaxSize());
        }
        return super.get(index - 1, poolId); //per documentation.
    }

    //public void setCPInfo(int index, InfoBase base) throws Exception {
    //  cp_info[index] = base;
    //}

    public void validateConstantPool(int poolId) throws Exception {
        return; //todo later
        /*
                 InfoBase base = null;
                 for (int loop = 0; loop < size; loop++) {
            base = cp_info[loop];
            if (base.getTag() == null) {
         throw new Exception ("Invalid entry in constant pool at location "+size);
            }
                 }
         */
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "size=" +
                     factory.createUn(getCurrentSize() + 1).trim(2).
                     writeSelected(buff) + "\n";
        for (int loop = 1; loop <= getMaxSize(); loop++) {
//            Miscellaneous.println((InfoBase) get(loop));
            ret = ret + "\n(" + Integer.toHexString((loop) & 0xFFFFFFFF) +
                  ")" + ((BaseObject) get(loop, DEFAULT_SINGLE_POOP_ID)).
                  writeSelected(buff);
        }
        return ret;
    }

    @Override
    public void setMaxSize(int constantPoolCount) {
        super.setMaxSize(constantPoolCount - 1); //size is one less than constant_pool_count per documentation
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


}
