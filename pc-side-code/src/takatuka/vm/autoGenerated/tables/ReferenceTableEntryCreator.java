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
package takatuka.vm.autoGenerated.tables;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.classreader.logic.util.*;

public class ReferenceTableEntryCreator {

    private static final String JAVA = "Java";
    private static final String SEPERATOR = "_";
    private static final ReferenceTableEntryCreator referenceTable = new ReferenceTableEntryCreator();

    private ReferenceTableEntryCreator() {
    }

    static ReferenceTableEntryCreator getInstanceOf() {
        return referenceTable;
    }

    /**
     * The function will return the native method name based on specification
     * 
     * @param className
     * @param methodName
     * @param arguments
     * @return
     */
    public static String createName(String className, String methodName,
            String arguments) {
        String ret = JAVA + SEPERATOR;
        className = NameFormater.characterChanges(className);
        ret = ret + className;
        if (methodName != null) {
            methodName = NameFormater.characterChanges(methodName);
            ret = ret + SEPERATOR + methodName;
        }
        if (arguments != null && arguments.trim().length() > 0) {
            arguments = NameFormater.characterChanges(arguments);
            ret = ret + SEPERATOR + SEPERATOR + arguments;
        }
        return ret;
    }

    ReferenceTableEntry createTableEntry(LFFieldInfo lfFieldInfo,
            ClassFile cFile) {
        String methodName = null;
        String arguments = null;
        String className = null;
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        ReferenceTableEntry entry = null;
        int cpindex = 0;
        boolean isMethod = lfFieldInfo instanceof LFMethodInfo;
        UTF8Info utf8temp = null;
        Oracle oracle = Oracle.getInstanceOf();
        String originalName = null;
        try {
            if (lfFieldInfo != null) {
                //name
                cpindex = lfFieldInfo.getNameIndex().intValueUnsigned();
                utf8temp = (UTF8Info) pOne.get(cpindex, TagValues.CONSTANT_Utf8);
                methodName = utf8temp.convertBytes();
            }

            if (lfFieldInfo != null && isMethod) {
                //desc
                cpindex = lfFieldInfo.getDescriptorIndex().intValueUnsigned();
                utf8temp = (UTF8Info) pOne.get(cpindex, TagValues.CONSTANT_Utf8);
                arguments = utf8temp.convertBytes();
                arguments = arguments.substring(arguments.indexOf("(") + 1,
                        arguments.indexOf(")"));
            }

            className = cFile.getFullyQualifiedClassName();
            String longName = createName(className, methodName, arguments);
            originalName = className+"->"+methodName+"("+arguments+")";

            int nativeId = CPHeader.getNativeId(lfFieldInfo);

            cpindex = cFile.getThisClass().intValueUnsigned();
            
            if (lfFieldInfo != null) {
                cpindex = oracle.existFieldInfoCPIndex(lfFieldInfo, isMethod,
                        cFile.getThisClass().intValueUnsigned());
                //cpindex = LFFieldInfo.computeRefInfoUsingInfo(lfFieldInfo);
                if (cpindex < 0) return null;
            } 

            entry = new ReferenceTableEntry(longName, originalName, 
                    nativeId, cpindex, NameFormater.characterChanges(className));
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return entry;
    }
    
}
