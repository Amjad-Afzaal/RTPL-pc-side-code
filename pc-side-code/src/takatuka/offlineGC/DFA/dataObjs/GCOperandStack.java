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
package takatuka.offlineGC.DFA.dataObjs;

import takatuka.optimizer.VSS.dataObjs.*;
import takatuka.verifier.dataObjs.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCOperandStack extends SSOperandStack {

    public GCOperandStack(int maxStack) {
        super(maxStack);
    }

    @Override
    public Type mergeReferences(Type mineType, Type inputType) throws Exception {
        //Miscellaneous.println("Stack: Merging references " + mineType + ", " + inputType + ", stack before merge=" + this);
        GCType mineTypeGC = (GCType) mineType;
        GCType inputTypeGC = (GCType) inputType;
        if (mineTypeGC.getReferences().equals(inputTypeGC.getReferences())) {
            return null;
        }
        return GCType.addReferences(mineTypeGC, inputTypeGC);
    }

    @Override
    public boolean merge(FrameElement localVars) {
        return super.merge(localVars);
    }
}
