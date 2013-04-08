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
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.dataObjs.constantpool.LFMethodRefInfo;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 *
 * 1. Verify method table of class files.
 * 2. Verify Field. If the non-static and static fields
 * start from the indexes mentioned in each class or not.
 * 
 *
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
class VerifyCPIndexes {

    private GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
    private Oracle oracle = Oracle.getInstanceOf();

    public VerifyCPIndexes() {
    }

    /**
     * from here we start
     */
    public void execute() {
        ClassFileController cFileContr = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileContr.getCurrentSize(); loop++) {
            LFClassFile cFile = (LFClassFile) cFileContr.get(loop);
            verifyMethodTableOfClassFile(cFile);
        }
    }

    private void verifyMethodTableOfClassFile(LFClassFile cFile) {
        if (cFile.getAccessFlags().isInterface()) {
            return;
        }
        MethodTable methodTable = cFile.getMyMethodTable();
        if (methodTable == null) {
            Miscellaneous.println("why method table is null " + cFile.getFullyQualifiedClassName());
            Miscellaneous.exit();
        }
        Vector<Un> methodCPIndexes = methodTable.getMethodCPIndeses();
        Vector<Un> methodNaTIndexes = methodTable.getMethodsNameAndTypeInfoIndex();

        int loop = 0;
        try {
            for (loop = 0; loop < methodCPIndexes.size(); loop++) {
                int cpIndex = methodCPIndexes.elementAt(loop).intValueUnsigned();
                int nAtIndex = methodNaTIndexes.elementAt(loop).intValueUnsigned();
                LFMethodRefInfo mRefInfo = (LFMethodRefInfo) gcp.get(cpIndex, TagValues.CONSTANT_Methodref);
                //NameAndTypeInfo nameAndTypeInfo = (NameAndTypeInfo) gcp.get(nAtIndex, TagValues.CONSTANT_NameAndType);
                if (mRefInfo.getNameAndTypeIndex().intValueUnsigned() != nAtIndex) {
                    TukFileVerifier.addException("#1: Method table is"
                            + " invalid of class file= " + cFile.getFullyQualifiedClassName()
                            + ", at index=" + loop);
                }
                LFClassFile methodClass = (LFClassFile) oracle.getClass(mRefInfo.getIndex().intValueUnsigned(), gcp);
                if (methodClass.getThisClass().intValueUnsigned() != cFile.getThisClass().intValueUnsigned()) {
                    TukFileVerifier.addException("#2: Method table is"
                            + " invalid of class file= " + cFile.getFullyQualifiedClassName()
                            + ", at index=" + loop);
                }
                String mName = oracle.methodOrFieldName(mRefInfo, gcp);
                if (mName.equals("<clinit>")) {
                    TukFileVerifier.addException("#3: Method table is"
                            + " invalid of class file= " + cFile.getFullyQualifiedClassName()
                            + ", at index=" + loop);
                }
            }
        } catch (Exception d) {
            Miscellaneous.println("error  " + d);
            TukFileVerifier.addException("#4: Method table is"
                    + " invalid of class file= " + cFile.getFullyQualifiedClassName()
                    + ", at index=" + loop);
        }
    }
}
