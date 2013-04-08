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
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: All the info object have similar format and some has some
 * extra infomration. Hence this based object represent similar information in
 * all the info and extended object represent extra informations. </p>
 * @author Faisal Aslam
 * @version 1.0
 */


public abstract class InfoBase implements BaseObject {
    
    private static boolean toPrintTag = true;
    private Un tag; //(1);

    // public static  byte validTag;

    public static void printTag(boolean toPrintTag) {
        InfoBase.toPrintTag = toPrintTag;
    }
    
    public InfoBase() throws TagException, Exception {
        throw new Exception("Assign a tag always");
    }

    public InfoBase(byte tag) throws TagException, Exception {
        Un temp; //(1);
        byte btag[] = {tag};
        temp = FactoryPlaceholder.getInstanceOf().getFactory().createUn(btag);
        setTag(temp);
    }

    public InfoBase(Un tag) throws TagException {

        setTag(tag);
    }

    /*
        private void validateTagNumber()
                throws TagException {
        if (validTag != tag.byteValue(0)) {
            throw new TagException(" Tag required ="+validTag+
                                          " tag recieved ="+tag.byteValue(0));
        }
     }

        private void setValidateTag(byte tag) {
            this.validTag = tag;
        }
     */
    public void setTag(Un tag) throws TagException {
        TagValues.validateTag(tag);
        this.tag = tag;
    }

    public Un getTag() {
        return tag;
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

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        if (tag == null || !InfoBase.toPrintTag) {
            return "";
        }
        return tag.writeSelected(buff);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InfoBase)) {
            return false;
        }

        if (tag.equals(((InfoBase) obj).tag)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tag).toHashCode();
    }

}
