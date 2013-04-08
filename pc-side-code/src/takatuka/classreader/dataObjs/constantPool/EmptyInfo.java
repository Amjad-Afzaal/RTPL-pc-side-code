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

import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 *
 * <p>Description: Per documentation, a DoubleInfo and LongInfo takes two spaces in the constant pool.
 * The second space has this EmptyInfo. It should not print or do anything useful. </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class EmptyInfo extends InfoBase {
    private static byte myTag[] = {TagValues.CONSTANT_Empty};
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    private String debugInfo = null;

    public EmptyInfo() throws Exception {
        super(TagValues.CONSTANT_Empty);
    }

    public void setDebugInfo(String debugInfo) {
        this.debugInfo = debugInfo;
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


    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "\tEmptyInfo";
        if (debugInfo != null) {
            ret += debugInfo;
        }
        return ret;
    }
}
