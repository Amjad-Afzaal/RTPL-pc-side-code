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

import takatuka.optimizer.bytecode.changer.logic.comb.*;
import java.util.*;
import takatuka.classreader.logic.file.*;
import java.io.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class FreqMap {

    private HashMap<String, FreqMapValue> frequency = new HashMap();
    private static FreqMap fMap = null;

    public static FreqMap getInstanceOf() {
        if (fMap == null) {
            fMap = new FreqMap();
        }
        return fMap;
    }

    public void remove(InstructionsCombined key) {
        remove(key.getMnemonic());
    }

    public void remove(String key) {
        frequency.remove(key);
    }

    public FreqMapValue getAndRemoveMaxSavingsValue() {
        Set keys = frequency.keySet();
        Iterator<String> keyIterator = keys.iterator();
        int maxSavings = Integer.MIN_VALUE;
        FreqMapValue ret = null;
        String bestKey = null;
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            FreqMapValue value = frequency.get(key);
            int savings = value.totalSavings();
            if (BCCompactionCost.getInstanceOf().getTotalReduction(value) > maxSavings) {
                ret = value;
                maxSavings = savings;
                bestKey = key;
            }
        }
        frequency.remove(bestKey);
        return ret;
    }

    /**
     * calculates average size of combinations
     * calculates maximum size of combinations
     * 
     */
    public void calculateStat() {
        Iterator<String> it = frequency.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
        }
    }

    /**
     * clear all the key, values. Now it is empty.
     */
    public void clear() {
        frequency.clear();

    }

    public void setMap(FreqMap map) {
        //FreqMap.fMap = map;
        this.frequency = map.frequency;
    }

    /**
     * inc only if it is not self overlaping. 
     * The selef overlapping is access based on arrivalTime
     * 
     * @param key
     * @param arrivalTime
     * @throws java.lang.Exception
     */
    public void inc(InstructionsCombined key, int arrivalTime) throws Exception {
        FreqMapValue value = (FreqMapValue) frequency.get(key.getMnemonic());

        //if (value.arrivalTime + key.length())
        if (value == null) {
            throw new Exception("cannot increment non existing combination");
        } else {
            int oldArrivalTime = value.arrivalTime;
            if (oldArrivalTime == -1 || arrivalTime >
                    oldArrivalTime + key.getInstructions().size()) {
                value.freq++;
            }
        }

    }

    public void resetFreq(InstructionsCombined key) {
        frequency.get(key).freq = 1;
    }

    public void put(InstructionsCombined key, int arrivalTime, Vector originalInstructions) {
        frequency.put(key.getMnemonic(), new FreqMapValue(key, 1, arrivalTime, originalInstructions));
    }

    public FreqMapValue getValue(String key) {
        Object obj = frequency.get(key);
        FreqMapValue freq = null;
        if (obj != null) {
            freq = (FreqMapValue) obj;
        }
        return freq;
    }

    /**
     * returns the freq of a specific combination. 
     * @param key
     * @return 
     */
    public FreqMapValue getValue(InstructionsCombined key) {
        return getValue(key.getMnemonic());
    }

    public int getFreq(InstructionsCombined key) {
        if (key == null) {
            return 0;
        }
        FreqMapValue value = getValue(key);
        if (value == null) {
            return 0;
        }
        return value.freq;
    }

    public int size() {
        return frequency.size();
    }

    public Vector getAllKeys() {
        return new Vector(frequency.keySet());
    }

    public Vector getAllKeysSorted(Comparator comp) {
        Vector keys = new Vector(frequency.keySet());
        QuickSort.sort(keys, new FreqMapValueComparator());
        return keys;
    }

    public Vector getAllKeysSorted() {
        return getAllKeysSorted(new FreqMapValueComparator());
    }

    public Vector getAllValues() {
        return new Vector(frequency.values());
    }

    @Override
    public String toString() {
        int maxLength = 0;
        String lengthString = "";
        String combinationString = "";
        Vector keys = getAllKeysSorted(new FreqMapValueComparator());
        for (int loop = 0; loop < keys.size(); loop++) {
            String key = (String) keys.elementAt(loop);
            FreqMapValue value = getValue(key);
            InstructionsCombined comb = value.instComb;
            if (value.instComb.getNumberOfSimpleInsturction() > maxLength) {
                maxLength = value.instComb.getNumberOfSimpleInsturction();
            }
            combinationString = combinationString + key + "\n";
        }
        lengthString = maxLength + "\n";
        return lengthString + combinationString;
    }

    public void writeInFile(String fileName) {
        try {
            String toWrite = toString();
            ClassFileWriter.writeFile(new File(fileName), toWrite);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
