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
import takatuka.offlineGC.DFA.dataObjs.*;

/**
 * 
 * Description:
 * <p>
 * Unlike fieldinfos this field is use in GCHeap to indentify the type of a field.
 * A field is used only to store references. 
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCField {

    private int fieldNATIndex = -1;
    private HashSet referenceSet = new HashSet();

    public GCField(int fieldNATIndex) {
        this.fieldNATIndex = fieldNATIndex;
    }

    @Override
    public Object clone() {
        GCField newField = new GCField(fieldNATIndex);
        newField.add(referenceSet);
        return newField;
    }

    /**
     * 
     * @return
     */
    public int getFieldNATIndex() {
        return fieldNATIndex;
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> get() {
        return (HashSet<TTReference>) referenceSet.clone();
    }

    /**
     * 
     * @param reference
     */
    public void add(TTReference reference) {
        referenceSet.add(reference);
    }

    /**
     * 
     * @param references
     */
    public void add(HashSet<TTReference> references) {
        referenceSet.addAll(references);
    }
    
    @Override
    public String toString() {
        return " GCField=[NaT="+fieldNATIndex+", References="+referenceSet+"]";
    }
}
