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

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
    CONSTANT_Utf8_info {
        u1 tag;
        u2 length;
        u1 bytes[length];
    }
 * @author Faisal Aslam
 * @version 1.0
 */
public class UTF8Info extends InfoBase {
    private Un length; //;//(2);
    private Un bytes;
    public UTF8Info() throws TagException, Exception {
        super(TagValues.CONSTANT_Utf8);
    }

    public UTF8Info(Un tag) throws TagException {
        super(tag);
    }

    public void setLength(Un u2_length) throws Exception {
        Un.validateUnSize(2, u2_length);
        this.length = u2_length;
        //bytes ;//(length.intValueUnsigned());
    }

    public Un getLength() {
        return length;
    }

    public void setBytes(Un un) {
        this.bytes = un;
    }

    public Un getBytes() throws Exception {
        return bytes;
    }

    public String convertBytes() {
        try {
            return convertBytes(bytes.getData());
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null; //not reachable code.
    }

    /**
     *
     * Based on 4.4.7
     * @return String
     */
    public static String convertBytes(byte values[]) throws Exception {
        String ret = "";
        ret = new String(values, "UTF8");
        return ret;
    }

    /**
     *
     * @return String
     */
    public static String convertBytes(Un un) throws Exception {
        return convertBytes(un.getData());
    }

    public static String convertBytes(UTF8Info utf8) throws Exception {
        return convertBytes(utf8.getBytes());
    }

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
        String ret = "\tUTF8Info=";
        ret = ret + super.writeSelected(buff) + ", length =" +
              length.writeSelected(buff) + ", bytes=" +
              bytes.writeSelected(buff) + " \t//" + convertBytes() + "";
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof UTF8Info)) {
            return false;
        }
        UTF8Info utf8 = (UTF8Info) obj;
        try {
            if (super.equals(obj) && bytes.equals(utf8.bytes)) {
                return true;
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(bytes).
                toHashCode();
    }


}
