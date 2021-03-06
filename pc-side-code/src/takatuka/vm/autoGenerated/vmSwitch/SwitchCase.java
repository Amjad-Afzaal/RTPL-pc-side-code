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
package takatuka.vm.autoGenerated.vmSwitch;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class SwitchCase implements Comparable<SwitchCase> {

    private static final String CASE_START = "    case ";
    private static final String CASE_END = "\n    break;\n";
    public static final String CODE_START = "\n       ";
    private static final String PC_INC = "pc_inc=";
    String condition = null;
    int pcInc = -1;
    String casebody = null;
    int opCode = -1;

    public SwitchCase(int opCode, String condition, int pcInc, String casebody) {
        this.opCode = opCode;
        this.condition = condition;
        this.pcInc = pcInc;
        this.casebody = casebody;
    }

    @Override
    public String toString() {
        String ret = CASE_START + condition + "://" + opCode;
        ret = ret + CODE_START + PC_INC + pcInc + ";";
        ret = ret + CODE_START + casebody;
        ret = ret + CASE_END + "\n";
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + this.opCode;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SwitchCase)) {
            return false;
        }
        SwitchCase input = (SwitchCase) obj;
        if (input.opCode == opCode) {
            return true;
        }
        return false;
    }

    public int compareTo(SwitchCase o) {
        int ret = 0; //new Integer(casebody.length()).compareTo(o.casebody.length());
        if (ret == 0) {
            ret = new Integer(o.opCode).compareTo(opCode);
        }
        return ret;
    }
}
