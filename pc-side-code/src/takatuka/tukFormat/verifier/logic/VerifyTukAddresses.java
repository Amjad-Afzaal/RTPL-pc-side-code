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
package takatuka.tukFormat.verifier.logic;

import java.io.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.io.*;
import takatuka.tukFormat.logic.file.*;
import java.util.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class VerifyTukAddresses {

    
    private RandomAccessFile tukFile = null;
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();

    
    public VerifyTukAddresses() {
    }

    
    

    private RandomAccessFile getTukFile() {
        if (tukFile == null) {
            try {
                tukFile = new RandomAccessFile(LFWriter.getTukFileNamePath(), "r");
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
        }
        return tukFile;
    }

    /**
     * it see that if an object present in a specific location in the TUK file.
     * If not then an exception is thrown.
     *
     *
     * @param startLocInTuk
     * address in the tuk file from where the object starts.
     * @param tukObject
     * the object in question.
     * @param unMap
     * tells what are the composition of different fields in terms of Un in the baseobject.
     * It does not need to represent whole baseobject but few starting field uniquely identifying that
     * base object is sufficent.
     * @throws Exception
     */
    void verifyGeneral(int startLocInTuk, BaseObject tukObject, Vector<Integer> unMap) throws Exception {
        RandomAccessFile tukFilelocal = getTukFile();
        if (startLocInTuk > tukFilelocal.length()) {
            throw new Exception("TUK file verification error." +
                    " Address is outside TUK file");
        }
        tukFilelocal.seek(startLocInTuk);

        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        BufferedByteCountingOutputStream bCount = new BufferedByteCountingOutputStream(bOut);
        tukObject.writeSelected(bCount);
        bCount.flush();
        byte baseObjectArray[] = bOut.toByteArray();
        Un allInOneUn = factory.createUn(baseObjectArray);
        //printByteArray(baseObjectArray, startLocInTuk);
        for (int loop = 0; loop < unMap.size(); loop++) {
            Un toComp = Un.cutBytes(unMap.elementAt(loop), allInOneUn);
            byte[] fromTukBytes = new byte[toComp.size()];
            tukFilelocal.read(fromTukBytes);
            Un fromTukFileUn = factory.createUn(fromTukBytes);
            //Miscellaneous.println(fromTukFileUn + "---, ----" + toComp);
            if (!toComp.equals(fromTukFileUn)) {
                throw new Exception("TUK file verification error." +
                        " Mismatch occured");
            }
        }
    }

    private void printByteArray(byte[] inputArray, int startAddress) {
        for (int address = 0; address < inputArray.length; address++) {
            System.out.print((address + startAddress) + ":" +
                    Integer.toHexString(inputArray[address] & 0xFF) + "  ");
            if (address % 20 == 0) {
                Miscellaneous.println();
            }
        }

    }

    private void printTUKFile() {
        try {
            RandomAccessFile tukFilelocal = getTukFile();
            tukFilelocal.seek(0);
            for (int address = 0; address < tukFilelocal.length(); address++) {
                System.out.print(address + ":" +
                        Integer.toHexString(tukFilelocal.readByte() & 0xFF) + "  ");
                if (address % 20 == 0) {
                    Miscellaneous.println();
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
