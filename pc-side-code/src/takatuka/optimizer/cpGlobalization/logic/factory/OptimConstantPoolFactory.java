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
package takatuka.optimizer.cpGlobalization.logic.factory;

import takatuka.classreader.logic.factory.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import java.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class OptimConstantPoolFactory extends ConstantPoolFactory {

    private static final OptimConstantPoolFactory optimCPFact =
            new OptimConstantPoolFactory();

    static ConstantPoolFactory getInstanceOf() {
        return optimCPFact;
    }

    private void processPoolIds(Vector poolIds) {
        for (int loop = 0; loop < poolIds.size(); loop++) {
            Integer poolId = (Integer) poolIds.elementAt(loop);
            if (poolId == TagValues.CONSTANT_String ||
                    poolId == TagValues.CONSTANT_Float ||
                    poolId == TagValues.CONSTANT_Double ||
                    poolId == TagValues.CONSTANT_Integer ||
                    poolId == TagValues.CONSTANT_Long) {
                    poolIds.remove(loop);
                    loop --;
            }
        }
    }

    
    @Override
    protected MultiplePoolsFacade createConstantPool() throws Exception {
        TreeSet poolIds = new TreeSet();
        Vector allTags = TagValues.getAllTagValues();
        //processPoolIds(allTags);
        poolIds.addAll(allTags);
        return GlobalConstantPool.getInstanceOf(poolIds, -1); //-1 means infinite number of elements
    }
}