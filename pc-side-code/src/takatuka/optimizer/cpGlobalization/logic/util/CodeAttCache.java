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
package takatuka.optimizer.cpGlobalization.logic.util;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class CodeAttCache {

    private AttributeInfo codeAtt = null;
    private MethodInfo mInfo = null;
    private ClassFile cFile = null;

    CodeAttCache(AttributeInfo codeAtt, MethodInfo mInfo, ClassFile cFile) {
        this.codeAtt = codeAtt;
        this.mInfo = mInfo;
        this.cFile = cFile;
    }

    public AttributeInfo getAttribute() {
        return codeAtt;
    }

    public MethodInfo getMethodInfo() {
        return mInfo;
    }

    public ClassFile getClassFile() {
        return cFile;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CodeAttCache)) {
            return false;
        }
        CodeAttCache input = (CodeAttCache) obj;
        if (input.getAttribute().equals(getAttribute()) &&
                input.getClassFile().equals(getClassFile()) &&
                input.getMethodInfo().equals(getMethodInfo())) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (this.codeAtt != null ? this.codeAtt.hashCode() : 0);
        hash = 79 * hash + (this.mInfo != null ? this.mInfo.hashCode() : 0);
        hash = 79 * hash + (this.cFile != null ? this.cFile.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        Oracle oracle = Oracle.getInstanceOf();
        ClassFile.currentClassToWorkOn = cFile;
        MultiplePoolsFacade cp = cFile.getConstantPool();
        String mName = oracle.methodOrFieldName(mInfo, cp);
        return cFile.getFullyQualifiedClassName() + "->" + mName + "->{" + codeAtt + "}";
    }
}
