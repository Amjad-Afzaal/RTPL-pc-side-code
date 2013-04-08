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
package takatuka.classreader.dataObjs;

import java.nio.*;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: It makes byte array of different sizes. For example u2, u4, u8 objects.
 * The class name was choosen to be Un based on Java Class file format specification </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class Un implements BaseObject {

    private byte data[] = {};
    //private int size;
    private static int bufferSize = 0;
    private static int numberOfBytesWritten = 0;

    //public Un(int size) {
    //  data = new byte[size];
    //}
    public Un() throws Exception {
    }

    /**
     *
     * @param bytes byte[]
     * @throws Exception
     */
    public Un(byte bytes[]) throws Exception {
        setData(bytes);
    }

    public Un(int value) throws Exception {
        setData(value);
    }

    public Un trim(int size) {
        if (size == this.size()) {
            return this;
        }
        return cutBytesRev(size, this);
    }

    @Override
    public Object clone() {
        try {
            return new Un(data.clone());
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null; //unreachable code.
    }

    /**
     * Get a portional of data by cutting it out from the object.
     *
     * @param size int
     * @param un Un
     * @return Un
     * @throws Exception
     */
    private static Un cutBytesRev(int size, Un un) {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        if (size > un.size()) {
            byte b[] = new byte[size];
            int index = 0;
            for (int loop = 0; loop < size - un.size(); loop++) {
                b[index++] = 0;
            }
            byte data[] = un.getData();
            for (int loop = 0; loop < un.size(); loop++) {
                b[index++] = data[loop];
            }
            //  Miscellaneous.println("Invalid cutBytesRev size for Un");
            try {
                return factory.createUn(b);
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
        }
        byte ret[] = new byte[size];
        byte data[] = un.getData();
        for (int loop = 0; loop < data.length; loop++) {
            if (loop < size) {
                ret[size - loop - 1] = data[data.length - loop - 1];
            } else {
                break;
            }
        }
        try {
            return factory.createUn(ret);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

        return null;
    }

    /**
     * Get a portional of data by cutting it out from the start of existing Un.
     * Note that existing Un also changed after the end of operation.
     *
     * @param size int
     * @param un Un
     * @return Un
     * @throws Exception
     */
    public static Un cutBytes(int size, Un un) throws Exception {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        if (size > un.size()) {
            throw new Exception("Invalid cutByte size for Un");
        }
        byte ret[] = new byte[size];
        byte temp[] = new byte[un.size() - size];
        byte data[] = un.getData();
        for (int loop = 0; loop < data.length; loop++) {
            if (loop < size) {
                ret[loop] = data[loop];
            } else {
                temp[loop - size] = data[loop];
            }
        }
        un.setData(temp);
        return factory.createUn(ret);

    }

    /**
     *
     * @return byte[]
     */
    public byte[] getData() {
        return data;
    }

    /**
     *
     * @return int
     */
    public int size() {
        if (getData() == null) {
            return 0;
        }
        return this.getData().length;
    }

    /**
     *
     * @param data byte[]
     * @throws Exception
     */
    public void setData(byte data[]) throws Exception {
        this.data = data;
    }

    /**
     *
     * @param un Un
     * @throws Exception
     */
    public void setData(Un un) throws Exception {
        setData(un.getData());
    }

    /**
     *
     * @param value long
     * @throws Exception
     */
    public void setData(int value) throws Exception {
        setData(intToBytes(value));
    }

    /**
     *
     * @param required int
     * @param recieved Un
     * @throws UnSizeException
     */
    public static void validateUnSize(int required, Un recieved) throws
            UnSizeException {
        if (recieved == null || required == -1) {
            return; //it is valid to have null value and -1 means all sizes allowed
        }
        if (required != recieved.size()) {
            throw new UnSizeException(required, recieved.size());
        }
    }

    /**
     *
     * @param start
     * @param end
     * @return
     */
    public int intValueUnsigned(int start, int end) {
        try {
            /*
             * Are you thinking that why have used Long intead of Integer?
             * Answer is http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4215269
             */
            return (int) Long.parseLong(hexValue(start, end), 16);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return -1;
    }

    /**
     * return the 32 bit long value...
     * @return int
     */
    public long longValue() {
        try {
            //Miscellaneous.println(hexValue(start, end));
            return Long.parseLong(hexValue(0, data.length - 1), 16);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return 0;
    }

    public int intValueSigned() {
        int ret = 0;
        if (size() == 0) {
            return 0;
        }
        if (size() > 4) {
            throw new UnsupportedOperationException();
        }
        ret = data[0];
        for (int i = 1; i < data.length; i++) {
            ret = ret << 8 | data[i] & 0xff;
        }
        //todo use this one instead ret1 = ByteBuffer.wrap(data).getInt();
        return ret;
    }

    /**
     *
     * 
     * @return
     */
    public int intValueUnsigned() {
        try {
            if (size() == 0) {
                return 0;
            }
            if (size() > 4) {
                throw new Exception("cannot convert more than 4 bytes");
            }
            return intValueUnsigned(0, data.length - 1);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

        return 0; //unreachable code.
    }

    /**
     *
     * @return String
     * @throws Exception
     */
    public String hexValue() throws Exception {
        return hexValue(0, data.length - 1);
    }

    /**
     *
     * @param start int
     * @param end int
     * @return String
     * @throws Exception
     */
    public String hexValue(int start, int end) throws Exception {
        if (size() == 0) {
            return "";
        }
        if (size() < end || start > end) {
            Miscellaneous.println(getData());
            throw new Exception(
                    "Invalid Data! in function getRange() allowed-size=" + size()); //todo
        }
        String ret = "";
        for (int loop = start; loop <= end; loop++) {
            ret = ret + hexValue(loop);
        }

        return ret;
    }

    /**
     *
     * @param index int
     * @return byte
     */
    public byte byteValue(int index) {
        return data[index];
    }

    /**
     *
     * @param input long
     * @return byte[]
     */
    private byte[] intToBytes(int input) {
        return ByteBuffer.allocate(4).putInt(input).array();
    }

    /**
     *
     * @param index int
     * @return String
     */
    public String hexValue(int index) {
        String hex = Integer.toHexString(data[index] & 0xFF);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }

        return hex;
    }

    @Override
    public String toString() {
        String ret = "";
        try {
            return writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

        return ret;
    }

    /**
     *
     * @param buff BufferedByteCountingOutputStream
     * @throws Exception
     */
    public String writeSelected(
            BufferedByteCountingOutputStream buff) throws
            Exception {
        //System.out.print(hexValue()+" ");
        numberOfBytesWritten = numberOfBytesWritten + size();
        //Miscellaneous.println(hexValue()+"--at--"+Integer.toHexString(numberOfBytesWritten));
        if (getData() != null && buff != null) {
            buff.write(getData());
        }
        if (buff != null) {
            bufferSize = bufferSize + size();
            if (bufferSize > 500) { //too big???
                buff.flush();
                bufferSize = 0;
            }
        }
        if (getData() != null) {
            if (size() != 0) {
                return hexValue();
            } else {
                return "";
            }

        } else {
            return null;
        }

    }

    public int get(int index) {
        return data[index];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Un)) {
            return false;
        }

        Un unObj = (Un) obj;
        if (this.size() != unObj.size()) {
            return false;
        }

        for (int loop = 0; loop
                < size(); loop++) {
            if (unObj.get(loop) != get(loop)) {
                return false;
            }

        }
        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(data).toHashCode();
    }

    /**
     * It replaces/overwrites given toBeReplace Un over current Un starting from
     * from-byte of current Un. It is possible this process increase the length
     * of new Un or maintain the same length however, its length will never decrease
     * An exception is thrown if from is greater then current Un size.
     * @param toBeReplace
     * @param from
     * @throws java.lang.Exception
     */
    public void replace(Un toBeReplace, int from) throws Exception {
        int temp = from + toBeReplace.size();
        byte newData[] = new byte[(temp > size()) ? temp : size()];
        if (data.length < from) {
            throw new Exception("Current Un size is smaller than from");
        }

        for (int loop = 0; loop
                < newData.length; loop++) {
            if (loop >= from && loop < from + toBeReplace.size()) {
                newData[loop] = toBeReplace.getData()[loop];
            } else {
                newData[loop] = data[loop];
            }

        }
        this.setData(newData);
    }

    /**
     * change the order of bytes
     * @throws Exception 
     */
    public void reverseMe() throws Exception {
        byte reverseBytes[] = new byte[size()];
        int count = 0;
        for (int loop = size()-1; loop >= 0; loop--) {
            reverseBytes[count++] = this.getData()[loop];
        }
        this.setData(reverseBytes);
    }
    
    /**
     * append second Un at the end of current Un.
     * @param second
     * @throws java.lang.Exception
     */
    public void conCat(Un second) throws Exception {
        int newSize = this.getData().length + second.getData().length;
        byte conBytes[] = new byte[newSize];
        int count = 0;
        for (int loop = 0; loop
                < this.getData().length; loop++) {
            conBytes[count++] = this.getData()[loop];
        }

        for (int loop = 0; loop
                < second.getData().length; loop++) {
            conBytes[count++] = second.getData()[loop];
        }

        this.setData(conBytes);
    }

//just to test Un class. It is not the main that run the whole project
    public static void main(String args[]) throws Exception {
        byte[] bb = {1, 22, '3', 'a', 'f'};
        byte[] cc = {1, 22, '3', 'a', 'f'};
        Un b = FactoryPlaceholder.getInstanceOf().getFactory().createUn(bb);
        Un c = FactoryPlaceholder.getInstanceOf().getFactory().createUn(cc);
        if (b.equals(c)) {
            System.out.print("We are equal");
        } else {
            System.out.print("We are not equal");
        }
    }
}
