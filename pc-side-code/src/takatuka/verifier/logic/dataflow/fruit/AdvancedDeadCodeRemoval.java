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
package takatuka.verifier.logic.dataflow.fruit;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;

/**
 *
 * Description:
 * <p>
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class AdvancedDeadCodeRemoval {

    private static final AdvancedDeadCodeRemoval myObj = new AdvancedDeadCodeRemoval();
    private Oracle oracle = Oracle.getInstanceOf();

    private AdvancedDeadCodeRemoval() {
    }

    public static AdvancedDeadCodeRemoval getInstanceOf() {
        return myObj;
    }

    private static void Debug_Me(String str) {
        Miscellaneous.println(str);
    }

    /**
     * 1. Go to each class files and if it is not marked as keepByTheUser then delete it.
     * 2. In the remaining class files.
     *    --- Go to each
     */
    public void execute() {
        if (true) {
            return;
        }
        ClassFileController cFileContr = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileContr.getCurrentSize(); loop++) {
            DCClassFile cFile = (DCClassFile) cFileContr.get(loop);
            if (!cFile.isKeepPerUserRequest() &&
                    !cFile.getAccessFlags().isInterface()
                    && !checkifSubClassHasToBeKept(cFile)) {
                Debug_Me("------------ Removing " + cFile.getFullyQualifiedClassName());
                cFileContr.remove(loop);
                loop--;
            } else {
                removeMethods(cFile);
            }
        }
    }

    private boolean checkifSubClassHasToBeKept(DCClassFile cFile) {
        try {
            HashSet subClasses = oracle.getAllSubClasses(cFile);
            Iterator<DCClassFile> it = subClasses.iterator();
            while (it.hasNext()) {
                DCClassFile subClass = it.next();
                if (subClass.isKeepPerUserRequest()) {
                    return true;
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return false;
    }

    private void removeMethods(DCClassFile cFile) {
        MethodInfoController mContr = cFile.getMethodInfoController();
        for (int loop = 0; loop < mContr.getCurrentSize(); loop++) {
            DCMethodInfo mInfo = (DCMethodInfo) mContr.get(loop);
            if (!mInfo.isFMKeepPerUserRequest()) {
                String methodName = oracle.methodOrFieldName(mInfo, GlobalConstantPool.getInstanceOf());
                if (methodName.equals("print") || methodName.contains("getId")) {
                    //continue;
                }
                Debug_Me("removing method " + cFile.getFullyQualifiedClassName() + "-->" +
                        oracle.methodOrFieldName(mInfo, GlobalConstantPool.getInstanceOf()) +
                        "-->" + oracle.methodOrFieldDescription(mInfo, GlobalConstantPool.getInstanceOf()));
                mContr.remove(loop);
                loop--;
            }
        }
    }
}
