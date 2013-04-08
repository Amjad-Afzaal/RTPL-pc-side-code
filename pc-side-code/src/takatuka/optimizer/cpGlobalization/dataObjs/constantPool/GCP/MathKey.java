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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP;

public class MathKey implements Comparable<MathKey> {

    private int tag = -1;
    private int size = -1;
    private int startIndex = -1;

    MathKey(int tag, int size, int startIndex) {
        this.tag = tag;
        this.size = size;
        this.startIndex = startIndex;
    }
    MathKey(int tag, int size) {
        this.tag = tag;
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null && !(obj instanceof MathKey)) {
            return false;
        }
        MathKey input = (MathKey) obj;
        if (input.tag == tag && input.size == size) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.tag;
        hash = 59 * hash + this.size;
        return hash;
    }

    @Override
    public int compareTo(MathKey arg0) {

        int compTag = new Integer(tag).compareTo(arg0.tag);
        if (compTag != 0) {
            return compTag;
        }
        int compStartIndex = new Integer(startIndex).compareTo(arg0.startIndex);
        if (compStartIndex != 0) {
            return -compStartIndex;
        }
        return new Integer(size).compareTo(arg0.size);
    }

    @Override
    public String toString() {
        return "tag="+tag+", size="+size+", startIndex="+startIndex;
    }
}
