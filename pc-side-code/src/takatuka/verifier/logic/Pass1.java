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
package takatuka.verifier.logic;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.file.*;
import takatuka.verifier.logic.exception.*;
import takatuka.classreader.logic.util.*;


/**
 * <p>Title: </p>
 * <p>Description:
 * Follows the same steps (except changes mentioned below) as indicated at Section 4.9 at
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * 1. Check for magic numbers in classes
 * 2. Check for predefined/known attributes length. They should be as defined in specifications.
 *    This step does not need to be verify in our case because reading classes program make sure of
 *    reading only desire number of bytes. Hence in case an attribute is longer then desire then it
 *    should lead to undesire effect in class length and validity automatically....
 * 3. Class file must not be trucated or have extra bytes at the end. This is already done in DataFileInputWithValidation
 * 4. Constant pool does not have any unrecognizable information.
 *    This step is already done while reading ClassFiles hence not carried out here.
 *    See ParseConstantPool class.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class Pass1 {
    private static final Pass1 pass1 = new Pass1();

    private static final int magicNumberCafe = 0xcafe;
    private static final int magicNumberBabe = 0xbabe;
    private Pass1() {
        super();
    }

    public static Pass1 getInstanceOf() {
        return pass1;
    }

    public void execute() {
        try {
            checkMagicNumbers();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    protected void checkMagicNumbers() throws Exception {
        ClassFileController cont = ClassFileController.getInstanceOf();
        ClassFile file = null;
        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            file = (ClassFile) cont.get(loop);
            ClassFile.currentClassToWorkOn = file;
            Un magicNumberRecieved = (Un) file.getMagicNumber().clone();
            if (Un.cutBytes(2, magicNumberRecieved).intValueUnsigned() !=
                magicNumberCafe ||
                magicNumberRecieved.intValueUnsigned() != magicNumberBabe) {
                throw new VerifyErrorExt(Messages.MAGIC_NUMBER_ILLEGAL);
            }
        }
    }


}
