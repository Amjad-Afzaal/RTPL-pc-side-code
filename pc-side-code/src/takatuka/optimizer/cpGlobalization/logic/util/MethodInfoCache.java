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

/**
 *
 * Description:
 * <p>
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodInfoCache {

    private MethodInfo method = null;
    private ClassFile cFile = null;

    /***
     * 
     * @param method
     * @param cFile
     */
    MethodInfoCache(MethodInfo method, ClassFile cFile) {
        this.method = method;
        this.cFile = cFile;
    }

    /**
     * 
     * @return
     */
    public MethodInfo getMethod() {
        return method;
    }

    /**
     *
     * @return
     */
    public ClassFile getClassFile() {
        return cFile;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodInfoCache)) {
            return false;
        }
        MethodInfoCache input = (MethodInfoCache) obj;
        if (method.equals(input.method) && cFile.equals(input.cFile)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.method != null ? this.method.hashCode() : 0);
        hash = 79 * hash + (this.cFile != null ? this.cFile.hashCode() : 0);
        return hash;
    }

}
