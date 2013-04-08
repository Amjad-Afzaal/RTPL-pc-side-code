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
package takatuka.optimizer.deadCodeRemoval.logic.classAndMethod;

import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodExamineListEntry {

    /**
     * class fully qualified name
     */
    private String methodClassFQName = null;
    /**
     * method Name 
     */
    private String methodName = null;
    private String methodDescription = null;
    private boolean useMethodCache = false;
    private DCMethodInfo mInfo = null;
    private boolean useClassFileCache = false;
    private DCClassFile cFile = null;
    private Oracle oracle = Oracle.getInstanceOf();
    private boolean isStaticParent = false;
    private boolean isGotFromByteCodeTraverse = false;
    private boolean isUserRequest = false;

    public MethodExamineListEntry(String methodClassFQName, String methodName,
            String methodDescription, boolean isGotFromByteCodeTraverse,
            boolean isUserRequest) {
        this.methodClassFQName = methodClassFQName;
        this.methodDescription = methodDescription;
        this.methodName = methodName;
        this.isGotFromByteCodeTraverse = isGotFromByteCodeTraverse;
        this.isUserRequest = isUserRequest;
    }

    public boolean isUserRequested() {
        return isUserRequest;
    }

    public boolean isGotMethodFromBytecodeTraverse() {
        return isGotFromByteCodeTraverse;
    }

    public void setStaticParent() {
        this.isStaticParent = true;
    }

    public boolean isStaticParent() {
        return this.isStaticParent;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDescr() {
        return methodDescription;
    }

    public String getMethodFullyQualifiedClassName() {
        return methodClassFQName;
    }

    public DCClassFile getMethodClass() {
        if (useClassFileCache) {
            return cFile;
        }
        useClassFileCache = true;
        cFile = (DCClassFile) oracle.getClass(methodClassFQName);
        return cFile;
    }

    public DCMethodInfo getMethodInfo() {
        if (useMethodCache) {
            return mInfo;
        }
        useMethodCache = true;
        DCClassFile cFilelocal = getMethodClass();
        if (cFilelocal == null) {
            return null;
        }
        mInfo = (DCMethodInfo) oracle.getMethodOrField(cFilelocal, methodName, methodDescription, true);
        return mInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MethodExamineListEntry)) {
            return false;
        }
        MethodExamineListEntry input = (MethodExamineListEntry) obj;
        if (input.methodClassFQName.equals(methodClassFQName) &&
                input.methodDescription.equals(methodDescription) &&
                input.methodName.equals(methodName)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.methodClassFQName != null ? this.methodClassFQName.hashCode() : 0);
        hash = 17 * hash + (this.methodName != null ? this.methodName.hashCode() : 0);
        hash = 17 * hash + (this.methodDescription != null ? this.methodDescription.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return methodClassFQName + ", " + methodName + ", " + methodDescription;
    }
}
