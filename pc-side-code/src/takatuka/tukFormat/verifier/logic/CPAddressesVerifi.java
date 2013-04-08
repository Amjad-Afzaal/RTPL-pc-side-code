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
package takatuka.tukFormat.verifier.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.EmptyInfo;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.tukFormat.dataObjs.constantpool.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: 
 * It verifies that each class file is stored in the correct addresse
 * in the tuk file. For that it check that each CP entry has the right 
 * address that points to the start of a right classfile.
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
class CPAddressesVerifi {

    private GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
    private Oracle oracle = Oracle.getInstanceOf();

   
    public CPAddressesVerifi() {
    }

   

    /**
     * from here we start
     */
    public void execute() {
        classFileAddressVerifi();
        methodInfoAddressVerifi();
    }

    /**
     * all classes have same map
     *
     * @param cFile
     * @return
     */
    private Vector createUnMapForClassFiles(ClassFile cFile) {
        Vector<Integer> ret = new Vector<Integer>();
        int end = 7;
        if (cFile.getAccessFlags().isInterface()) {
            end = 2;
        }
        for (int loop = 0; loop < end; loop++) {
            ret.addElement(2);
        }
        return ret;
    }

    /**
     * There are two kinds of method. A Big method has different map then a small method.
     * @param method
     * @return
     */
    private Vector createUnMapForMethodInfo(MethodInfo method) {
        Vector<Integer> ret = new Vector<Integer>();
        ret.addElement(2);
        ret.addElement(1);
        ret.addElement(2);
        if (!method.getAccessFlags().isNative()) {
            ret.addElement(2);
        }
        //for the time being do not compare codeAttribute.
        //Hence no worries about small and big method thingy.
        return ret;
    }

    /**
     * Go to each CP entry of classinfo and then check if the address in it is
     * the right address at which classfile exist.
     *
     * Step 1: get the classfiles from the constant pool
     *
     * Step 2: Use oracle to get class file corresponding to the CP Entry
     *
     * Step 3: Check if the class file exist in the right address
     * (i.e. CP address of the class files points to right location)
     */
    private void classFileAddressVerifi() {
        int size = gcp.getCurrentSize(TagValues.CONSTANT_Class);
        for (int loop = 1; loop < size; loop++) {
            if (gcp.get(loop, TagValues.CONSTANT_Class) instanceof EmptyInfo) {
                continue;
            }
            LFClassInfo cInfo = (LFClassInfo) gcp.get(loop, TagValues.CONSTANT_Class);
            int classAddress = cInfo.getClassFileAddress().intValueUnsigned();
            if (classAddress == 0) {
                LogHolder.getInstanceOf().addLog("wastage of flash or address space" +
                        " for classfile at cp index=" + loop);
                continue;
            }
            ClassFile cFile = oracle.getClass(loop, gcp);
            if (cFile == null) {
                TukFileVerifier.addException("Classfile at wrong address (#1)" + ", CP entry=" + loop);
                continue;
            }
            try {
                new VerifyTukAddresses().verifyGeneral(classAddress, cFile, createUnMapForClassFiles(cFile));
            } catch (Exception d) {
                TukFileVerifier.addException("Classfile at wrong address (#2)" +
                        cFile.getFullyQualifiedClassName() + ", CP entry=" + loop + ", address=" +
                        classAddress);
            }
        }
    }

    /**
     * Go to each CP entry of methodRefInfo and see if the method is exaclty the place
     * where it should be according to the entry.
     */
    private void methodInfoAddressVerifi() {
        int size = gcp.getCurrentSize(TagValues.CONSTANT_Methodref);
        for (int loop = 0; loop < size; loop++) {
            if (gcp.get(loop, TagValues.CONSTANT_Methodref) instanceof EmptyInfo) {
                continue;
            }
            LFMethodRefInfo mInfo = (LFMethodRefInfo) gcp.get(loop, TagValues.CONSTANT_Methodref);
            int methodAddress = mInfo.getFieldMethodInfoAddress().intValueUnsigned();
            if (methodAddress == 0) {
                LogHolder.getInstanceOf().addLog("wastage of flash or address space" +
                        " for method at cp index=" + loop);
                continue;
            }
            ClassFile cFile = oracle.getClass(mInfo.getIndex(), gcp);
            if (cFile == null) {
                TukFileVerifier.addException("A classFile does not exist but method does. " +
                        "At CP index=" + loop);
                continue;
            }
            int methodIndex = oracle.getReferenceFieldFromClassFile(gcp, mInfo, cFile, true);
            if (methodIndex == -1) {
                TukFileVerifier.addException("The classFile does not has the method. " +
                        "At CP index=" + loop + ", address=" + methodAddress);
                continue;
            }
            MethodInfo method = (MethodInfo) cFile.getMethodInfoController().get(methodIndex);
            try {
                new VerifyTukAddresses().verifyGeneral(methodAddress, method,
                        createUnMapForMethodInfo(method));
            } catch (Exception d) {
                TukFileVerifier.addException("MethodInfo at wrong address " +
                        cFile.getFullyQualifiedClassName() + "-->" + oracle.methodOrFieldName(method, gcp) +
                        ", CP entry =" + loop + ", address=" + methodAddress);
            }
        }

    }
}
