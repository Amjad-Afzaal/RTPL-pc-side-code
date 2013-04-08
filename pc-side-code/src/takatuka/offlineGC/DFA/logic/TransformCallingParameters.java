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
package takatuka.offlineGC.DFA.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.verifier.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * To convert calling parameters in one unified format when using them to store
 * some information.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class TransformCallingParameters {

    public static Vector transformCallingParameters(MethodInfo method, Vector originalCallingParms) {
        if (originalCallingParms == null) {
            return null;
        }
        Vector transforedParams = (Vector) originalCallingParms.clone();
        transforedParams = populateCallingParameters(transforedParams);
        return transforedParams;
    }

    private static Vector populateCallingParameters(Vector<GCType> callingParams) {
        Vector ret = new Vector();
        Iterator<GCType> it = callingParams.iterator();
        while (it.hasNext()) {
            GCType type = it.next();
            if (!type.isReference() && (type.getType() == Type.SPECIAL_TAIL ||
                    type.getType() == Type.UNUSED)) {
                continue;
            }
            ret.addAll(FrameElement.createEntry(type));
        }
        return ret;
    }
}
