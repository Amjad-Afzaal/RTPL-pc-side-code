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
package takatuka.optimizer.bytecode.changer.logic.freq;

import java.util.*;

/**
 * 
 * Description:
 * <p>
 * 
 * To sort Multiple instructions combined based on their cost
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FreqMapValueComparator implements Comparator {

    private static final BCCompactionCost bcCompCost = BCCompactionCost.getInstanceOf();
    private FreqMap freq = null;

    public FreqMapValueComparator(FreqMap freq) {
        this.freq = freq;
    }

    public FreqMapValueComparator() {
        this.freq = FreqMap.getInstanceOf();
    }

    @Override
    public int compare(Object obj1, Object obj2) {
        String key1 = (String) obj1;
        String key2 = (String) obj2;

        FreqMapValue mapValue1 = freq.getValue(key1);
        FreqMapValue mapValue2 = freq.getValue(key2);

        Integer value1 = bcCompCost.getTotalReduction(mapValue1);
        Integer value2 = bcCompCost.getTotalReduction(mapValue2);

        return value2.compareTo(value1);
    }
}
