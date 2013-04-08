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
package takatuka.chunkSizeCalc;

import takatuka.classreader.dataObjs.attribute.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.tukFormat.dataObjs.*;

/**
 *
 * @author Faisal Aslam
 */
class MethodAndChunkSize {

    LFMethodInfo method = null;
    int chunkSize = 0;

    public MethodAndChunkSize(LFMethodInfo method, int chunkSize) {
        this.chunkSize = chunkSize;
        this.method = method;
    }

    @Override
    public String toString() {
        try {
            Oracle oracle = Oracle.getInstanceOf();
            int methodIndex = LFMethodInfo.computeRefInfoUsingInfo(method);
            CodeAtt codeAtt = method.getCodeAtt();
            int frameSize = 0;
            if (codeAtt != null) {
                frameSize = GenerateCVCSInfo.getMethodFrameSize(method, codeAtt);
            }
            String retString = chunkSize + "/*frame Size=" + frameSize + ","
                    + " index=" + methodIndex + " methodFullName="
                    + oracle.getMethodOrFieldString(method) + "*/";
            return retString;
        } catch (Exception d) {
            d.printStackTrace();
            System.exit(1);
        }
        return null;
    }
}
