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
package takatuka.offlineGC.DFA.dataObjs.fields;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;

/**
 * 
 * Description:
 * <p>
 * GCHeapStatic contains all the static fields.
 * During globalization we create unique names+description for all those fields. Hence each
 * field has unique Name and type index. 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public interface GCHeapInterface {

    public static RecordRefUsedByFields gpRec = RecordRefUsedByFields.getInstanceOf();

    /**
     * 
     * @return
     */
    public int getNewInstrId();

    /**
     * 
     * 
     * @param nameAndTypeValueOrIndex
     * @param method
     * @param callingParams
     * @param instruction
     * @return
     */
    public GCField getField(int nameAndTypeValueOrIndex,
            MethodInfo method, Vector callingParams,
            GCInstruction instruction);

    /**
     * 
     *
     * @param nATIndexOrArrayIndex
     * @param value
     * @param method
     * @param callingParams
     * @param instr
     * @throws java.lang.Exception
     */
    public void putField(int nATIndexOrArrayIndex, GCType value,
            MethodInfo method, Vector callingParams,
            GCInstruction instr) throws Exception;
}
