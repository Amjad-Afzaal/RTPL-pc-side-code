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
package takatuka.optimizer.deadCodeRemoval.logic.cp;

import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.*;
import java.util.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * It count how many times CP entries are referrred from other CP entries.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class CPReferenceFromCPCounter {

    private Oracle oracle = Oracle.getInstanceOf();
    private static final CPReferenceFromCPCounter cpRefCP = new CPReferenceFromCPCounter();
    private GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();

    private CPReferenceFromCPCounter() {
    //no one creates me but me
    }

    public static CPReferenceFromCPCounter getInstanceOf() {
        return cpRefCP;
    }

    /**
     * set reference count that how many times an object is referred from within 
     * CP.
     * Here is how it works.
     * - In the codeAtt find exception-table and see what ClassInfo it uses. 
     *   We inc those classInfo referrence count
     * - Go to all ReferenceInfo and see what classInfo they use. 
     *   Increment reference count of those class infos.
     * - also handle this_class and super_class
     * - also handle interfacePointer
     *  ---- Ignore ----
     *   ignore NameAndTypeInfo as it is always keep but later deleted in TUK.
     * - ignore UTF8Infos as they are always keep but deleted in TUK.
     * - ignore ExceptionAtt as it will be deleted and not part of TUK. (Globalization will 
     *   stop globalizing exceptionAtt for now).
     * - ignore ConstantValueAtt as it will be deleted too...
     * 
     * @throws java.lang.Exception
     */
    public void markCPReferencesFromCP() throws Exception {
        countReferencesFromReferenceInfo();
        countReferencesFromExceptionTable();
        countThisAndSuperPointersReferences();
    }

    /**
     * - Go to each entry of ReferenceInfo. 
     * - see what classInfo and NameAndTypeInfo it refers to. 
     * - for both of them inc. their reference count.
     */
    private void countReferencesFromReferenceInfo() throws Exception {
        countReferencesFromReferenceInfo(TagValues.CONSTANT_Methodref);
        countReferencesFromReferenceInfo(TagValues.CONSTANT_Fieldref);
        //Todo following needs to be reviewed. It might keep little extra information.
        countReferencesFromReferenceInfo(TagValues.CONSTANT_InterfaceMethodref);
    }

    private void countReferencesFromReferenceInfo(int tag) throws Exception {
        Vector<ReferenceInfo> allReferences = pOne.getAll(tag);
        for (int loop = 0; loop < allReferences.size(); loop ++) {
            ReferenceInfo rInfo = allReferences.elementAt(loop);
            if (pOne.getReferenceCount(loop, tag) == 0 &&
                    tag != TagValues.CONSTANT_InterfaceMethodref) {
                continue; //Todo fix the problem with interfaces to make it better. For the time being keep more than required.
            }
            ClassFile.currentClassToWorkOn = pOne.getClass(loop, tag);
            int nAtIndex = rInfo.getNameAndTypeIndex().intValueUnsigned();
            pOne.incReferredCountFromCPObjects_oldIndex(nAtIndex,
                    TagValues.CONSTANT_NameAndType);
            int classInfoIndex = rInfo.getIndex().intValueUnsigned();
            
            pOne.incReferredCountFromCPObjects_oldIndex(classInfoIndex,
                    TagValues.CONSTANT_Class);
        }
    }

    private void countReferencesFromExceptionTable() throws Exception {
        Vector codeAttInfoVec = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> codeAttInfoIt = codeAttInfoVec.iterator();
        CodeAtt codeAtt = null;
        while (codeAttInfoIt.hasNext()) {
            CodeAttCache codeAttInfo = codeAttInfoIt.next();
            codeAtt = (CodeAtt) codeAttInfo.getAttribute();
            int expTableLength = codeAtt.getExceptionTableLength().intValueUnsigned();
            for (int loop = 0; loop < expTableLength; loop++) {
                int thisClass = codeAtt.getCatchType(loop).intValueUnsigned();
                ClassFile.currentClassToWorkOn = codeAttInfo.getClassFile();
                if (thisClass != 0) {
                    pOne.incReferredCountFromCPObjects_oldIndex(thisClass,
                            TagValues.CONSTANT_Class);
                }
            }
        }
    }

    private void countThisAndSuperPointersReferences() throws Exception {

        //1. go to all classfiles
        ClassFileController cont = ClassFileController.getInstanceOf();
        int size = cont.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            ClassFile cFile = (ClassFile) cont.get(loop);
            ClassFile.currentClassToWorkOn = cFile;
            //2. get this and super pointers
            int thisPointer = cFile.getThisClass().intValueUnsigned();
            int superPointer = cFile.getSuperClass().intValueUnsigned();
            //3. increments those references
            pOne.incReferenceCount_oldIndex(thisPointer, TagValues.CONSTANT_Class);
            if (superPointer != 0) {
                pOne.incReferenceCount_oldIndex(superPointer, TagValues.CONSTANT_Class);
            }
            countInterfacePointerReferences(cFile);
        }
    }

    private void countInterfacePointerReferences(ClassFile cFile) throws Exception {
        InterfaceController cont = cFile.getInterfaceController();
        int size = cont.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            int classId = ((Un) cont.get(loop)).intValueUnsigned();
            ClassFile.currentClassToWorkOn = cFile;
            pOne.incReferenceCount_oldIndex(classId, TagValues.CONSTANT_Class);
        }
    }
}
