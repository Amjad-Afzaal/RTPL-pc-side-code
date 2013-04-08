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

import takatuka.offlineGC.DFA.dataObjs.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
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
public class GCHeapStatic {

    /**
     * Key of the map is NameAndTypeIndex and value is the GCField
     */
    private HashMap<Integer, GCField> fields = new HashMap<Integer, GCField>();
    private static final GCHeapStatic heapStatic = new GCHeapStatic();
    private static RecordRefUsedByFields gpRec = RecordRefUsedByFields.getInstanceOf();

    private GCHeapStatic() {
    }

    public static GCHeapStatic getInstanceOf() {
        return heapStatic;
    }

    /**
     * The type will have the newId in it if it is initialized.
     * 
     * @param nAtIndex
     * @param value
     * @throws java.lang.Exception
     */
    public void putField(int nAtIndex, GCType value, MethodInfo method,
            Vector callingParams, GCInstruction instr) throws Exception {
        GCField field = fields.get(nAtIndex);
        if (field == null) {
            field = new GCField(nAtIndex);
            fields.put(nAtIndex, field);
        }
        //GCField dummy = new GCField(nAtIndex);
        //dummy.add(value.getReferences());
        //gpRec.addForPutField(method, callingParams, dummy);

        field.add(value.getReferences());
        //gpRec.record(field);
    }

    /**
     * same function as other putField but only with different arguments.
     * @param field
     * @param value
     */
    public void putField(FieldRefInfo field, GCType value, MethodInfo method,
            Vector callingParams, GCInstruction instr) throws Exception {
        int nAtypes = field.getNameAndTypeIndex().intValueUnsigned();
        putField(nAtypes, value, method, callingParams, instr);
    }

    public GCField getField(int nameAndTypeIndex, MethodInfo method,
            Vector callingParams, GCInstruction isntr) {

        GCField field = fields.get(nameAndTypeIndex);
        gpRec.recordRefIsGetFromAField(field);

        return field;
    }
}
