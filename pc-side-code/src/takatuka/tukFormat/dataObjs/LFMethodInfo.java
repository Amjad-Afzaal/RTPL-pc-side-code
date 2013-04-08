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
package takatuka.tukFormat.dataObjs;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.attribute.*;
import takatuka.tukFormat.dataObjs.constantpool.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFMethodInfo extends LFFieldInfo {

    public LFMethodInfo(ClassFile myClassFile) {
        super(myClassFile);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LFMethodInfo)) {
            return false;
        }

        if (super.equals(obj) &&
                obj.getClass().getName().equals(this.getClass().getName())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(this.getClass().getName()).toHashCode();
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        LFCodeAtt codeAtt = (LFCodeAtt) getCodeAtt();
        boolean isSmall = false;
        boolean ifLuHasChangedCode = false;
        int argCount = getArgsCount();
        if (codeAtt != null && codeAtt.isSmallCodeAtt() && argCount < 255) {
            isSmall = true;
            ((LFAccessFlags) getAccessFlags()).addSmallMethodFlag();
        }

        String ret = "LFMethodInfo=[";
        if (ifLuHasChangedCode) {
            ret = ret + ", access_flag=" + getAccessFlags().writeSelected(buff);
            ret = ret + " argument_count= ";
            if (isSmall) {
                ret = ret + factory.createUn(getArgsCount()).trim(1).writeSelected(buff);
            } else {
                ret = ret + factory.createUn(getArgsCount()).trim(2).writeSelected(buff);
            }
        } else {
            ret = ret + " argument_count= ";
            ret = ret + factory.createUn(getArgsCount()).trim(2).writeSelected(buff);
            ret = ret + ", access_flag=" + getAccessFlags().writeSelected(buff);

        }
        //computeNameAndTypeInfoUsingMethodInfo(this);
        if (!getAccessFlags().isStatic()) {
            ret = ret + ", NameAndTypeIndex=" +
                    getNameAndTypeIndex().writeSelected(buff);
            ret = ret + ", this_class=" + getClassFile().getThisClass().writeSelected(buff) + ", ";
        }
        if (getAccessFlags().isVirtual() || getAccessFlags().isStatic()) {
            if (codeAtt != null) {
                ret = ret + codeAtt.writeSelected(buff);
            }

        }
        if (getAccessFlags().isNative()) {
            ret = ret + ", native_id=" +
                    factory.createUn(CPHeader.getNativeId(this)).trim(2).
                    writeSelected(buff);
        }
        return ret;
    }
}
