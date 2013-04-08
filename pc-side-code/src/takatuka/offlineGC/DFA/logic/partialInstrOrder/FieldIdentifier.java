/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
 * All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *
 * This code is free software; you can redistribute cpIndexesIt and/or modify
 * cpIndexesIt under the terms of the GNU General Public License version 3
 * only, as published by the Free Software Foundation.
 *
 * This code is distributed in the hope that cpIndexesIt will be useful, but
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
package takatuka.offlineGC.DFA.logic.partialInstrOrder;

/**
 * To identify a field.
 *
 * @author aslam
 */
public class FieldIdentifier {

    private int newId = -1;
    private int cpIndex = -1;

    public FieldIdentifier(int newId, int cpIndex) {
        this.newId = newId;
        this.cpIndex = cpIndex;
    }

    public int getCPIndex() {
        return cpIndex;
    }

    public int getNewId() {
        return newId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FieldIdentifier)) {
            return false;
        }
        FieldIdentifier input = (FieldIdentifier)obj;
        if (input.newId == newId && input.cpIndex == cpIndex) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.newId;
        hash = 79 * hash + this.cpIndex;
        return hash;
    }

    @Override
    public String toString() {
        return newId+", "+cpIndex;
    }


}
