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

import takatuka.classreader.logic.exception.*;

//import takatuka.classreader.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description: The class will validate size of different Un. For example
 * by default a Un for a constantPool index could be only 2 bytes long. The class
 * is not made Singleton, so that it could be created using a Factory. Hence someone can
 * easily provide new implementation by extending it and changing the place holder, facade classes.
 * However the constractor is not   </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ValidateUn {
    private static int sizeOfCPIndex = 2;
    private static int mathHighBytesSize = 4;
    private static int mathLowBytesSize = 4;
    public ValidateUn(int sizeOfCPIndex, int mathHighBytesSize,
                      int mathLowBytesSize) {
        this.sizeOfCPIndex = sizeOfCPIndex;
        this.mathHighBytesSize = mathHighBytesSize;
        this.mathLowBytesSize = mathLowBytesSize;
    }

    public ValidateUn() {
        //create with defaults values
    }

    public static void setSizeOfConsPoolIndex(int size) {
        sizeOfCPIndex = size;
    }

//    public static void

    public static int getSizeOfConsPoolIndex() {
        return sizeOfCPIndex;
    }

    public void validateConsantPoolIndex(Un yourIndex) throws UnSizeException {
        Un.validateUnSize(sizeOfCPIndex, yourIndex);
    }

    public void validateConsantPoolIndexMultiple(Un bytes) throws
            UnSizeException {
        if (bytes.size() % sizeOfCPIndex != 0) {
            throw new UnSizeException("Invalid Constant Pool Index size");
        }
    }

    public void validateMathHighUnSize(Un bytes) throws UnSizeException {
        Un.validateUnSize(mathHighBytesSize, bytes);
    }

    public void validateMathLowUnSize(Un bytes) throws UnSizeException {
        Un.validateUnSize(mathLowBytesSize, bytes);
    }


}
