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
package takatuka.optimizer.deadCodeRemoval.dataObj.xml;

import java.util.*;

/**
 *
 * @author aslam
 */
public class ClassFileXML {
    private String name = null;
    private boolean includeAllFunctions = false;
    private boolean includeAllFields = false;
    private Vector functions = new Vector();
    private Vector fields = new Vector();
    
    public ClassFileXML(String name, boolean includeAllFunctions, 
            boolean includeAllFields,  Vector functions, Vector fields) {
        this.name = name;
        this.includeAllFunctions = includeAllFunctions;
        this.includeAllFields = includeAllFields;
        if (!includeAllFunctions) {
            this.functions = functions;
        }
        if (!includeAllFields) {
            this.fields = fields;
        }
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isIncludeAllFunctions() {
        return includeAllFunctions;
    } 

    public boolean isIncludeAllFields() {
        return includeAllFields;
    } 
    
    public Vector getFunctions() {
        return (Vector)functions.clone();
    }
    
    public Vector getFields() {
        return (Vector)fields.clone();
    }
        
        @Override
    public String toString() {
        return name+", "+ includeAllFields+ ", "+includeAllFunctions+
                ", Function = "+functions.toString()+ ", Fields="+
                fields.toString();
    }

}
