/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.OGI.FTT;

import takatuka.offlineGC.OGI.GraphUtils.ReferenceFilter;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.dataObjs.fields.RecordRefUsedByFields;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PerMethodDAGRefFilter extends ReferenceFilter {

    private static final PerMethodDAGRefFilter refFilter = new PerMethodDAGRefFilter();
    private NewInstrIdFactory newInstrIdFactory = NewInstrIdFactory.getInstanceOf();
    private Oracle oracle = Oracle.getInstanceOf();

    protected PerMethodDAGRefFilter() {
    }

    public static PerMethodDAGRefFilter getInstanceOf() {
        return refFilter;
    }

    /**
     * 
     * 1. removes references that are not created in the current method.
     * 2. removes references that are assigned to a field.
     *
     * @param refSet
     */
    @Override
    public void referencesFilter(HashSet<TTReference> refSet, InstrGraphNode graphNode) {
        MethodInfo method = graphNode.getMethod();
        if (method == null) {
            return;
        }
        String methodStr = oracle.getMethodOrFieldString(method);
        /**
         * All the references that are assinged to any field.
         */
        HashSet<TTReference> refAssignedToFields = RecordRefUsedByFields.getInstanceOf().getAllSavedRecord();
        /**
         * remove referernces that are assigned to a field.
         */
        refSet.removeAll(refAssignedToFields);
        /**
         * Following are the set of new Ids that are created in the current method.
         */
        HashSet<Integer> newIdsForCurrentMethodRef = newInstrIdFactory.getNewIdOfTheMethod(methodStr);
        if (newIdsForCurrentMethodRef == null) {
            //no ref is created in this method.
            refSet.clear();
        }
        HashSet<TTReference> refNotCreatedInCurrentMethod = new HashSet<TTReference>();
        Iterator<TTReference> refIt = refSet.iterator();
        while (refIt.hasNext()) {
            TTReference ref = refIt.next();
            if (!newIdsForCurrentMethodRef.contains(ref.getNewId())) {
                refNotCreatedInCurrentMethod.add(ref);
            }
        }
        /**
         * remove all the references that are not created in the current method.
         */
        refSet.removeAll(refNotCreatedInCurrentMethod);   
    }
}
