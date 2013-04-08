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
package takatuka.optimizer.cpGlobalization.dataObjs;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.file.*;
import takatuka.io.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Description: It makes byte array of different sizes. For example u2, u4, u8 objects.
 * The class name was choosen to be Un based on Java Class file format specification </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class OptimizedClassFile extends DCClassFile {

    public OptimizedClassFile(MultiplePoolsFacade constantPool,
            String uniqueName) {
        super(constantPool, uniqueName);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "";
        try {
            ret = ret +
                  "\n -------------------- Class File --------------------- \n";
            ret = ret + " Magic Number = " +
                  super.getMagicNumber().writeSelected(buff);
            ret = ret + "\n Minor Version=" +
                  super.getMiniorVersion().writeSelected(buff);
            ret = ret + "\n Major version=" +
                  super.getMajorVersion().writeSelected(buff);
            //ret = ret + "\n ---------------------- \n";
            /* Remember we have now global constant pool (CP). So do not print it. It is empty but still no use to print empty CP.
                         ret = ret + "\n constant pool count (int)=" +
             super.getConstantPoolCount().writeSelected(buff);
                         ret = ret + "\n ----- Constant Pool ---- \n" +
                               super.getConstantPool().writeSelected(buff);
                         ret = ret + "\n ---------------------- \n";
             */
//            Miscellaneous.println(ret);
            ret = ret + "\n Access flags=" +
                  super.getAccessFlags().writeSelected(buff);
            ret = ret + "\n this class=" +
                  super.getThisClass().writeSelected(buff);
            ret = ret + "\n super class=" +
                  super.getSuperClass().writeSelected(buff);
            ret = ret + "\n Interfaces Info=" +
                  super.getInterfaceController().writeSelected(buff);
            ret = ret + "\n" +
                  super.getFieldInfoController().writeSelected(buff);
            ret = ret + "\n--------\n" +
                  super.getMethodInfoController().writeSelected(buff);
            ret = ret + "\n" +
                  super.getAttributeInfoController().writeSelected(buff);
            if (!ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
                return "";
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;

    }


}
