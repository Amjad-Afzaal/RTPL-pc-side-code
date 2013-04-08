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
package takatuka.optimizer.deadCodeRemoval.logic.xml;

import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.xml.ClassFileXML;
import takatuka.optimizer.deadCodeRemoval.dataObj.xml.PackageXML;

/**
 *
 * @author aslam
 */
class CommonComparableClass {
    private Object obj = null;
    
    public CommonComparableClass(Object obj) {        
        this.obj = obj;
    }
    
    public String getPackageName() {
        if (obj instanceof ClassFile) {
            return ((ClassFile)obj).getPackageName();
        } else if (obj instanceof PackageXML) {
            PackageXML packXML = ((PackageXML)obj);
            String name = packXML.getName();
            if (packXML.isIncludeSubPackages()) {
                name = name+ "/*";
            }
            return name;
        } 
        return null;
    }
    public String getClassName() {
        if (obj instanceof ClassFile) {
            return ((ClassFile)obj).getFullyQualifiedClassName();
        } else if (obj instanceof ClassFileXML) {
            return ((ClassFileXML)obj).getName();
        } 
        return null;        
    } 
    public Object getObject() {
        return obj;
    }
    
    @Override
    public String toString() {
        return getPackageName()+", \t"+getClassName()+"\n";
    }
}
