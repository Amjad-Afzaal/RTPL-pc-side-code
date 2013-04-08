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
package takatuka.optimizer.appendCP.logic;


import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 *
 * Description:
 * <p>
 *
 * We need to add all the fields/Method in CP as our CP has addresses.
 * A program cannot access a field/Method if it is not in CP.
 * The idea of this class is to add all methods and field in CP after the
 * dead-code removal.
 * These method and fields are the one which are required by the program at run-time.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GenerateKeepReferences {

    private Oracle add2CP = Oracle.getInstanceOf();
    private static final GenerateKeepReferences genKeepRef = new GenerateKeepReferences();
    private int addedFields = 0;
    private int addedMethods = 0;
    
    public static GenerateKeepReferences getInstanceOf() {
        return genKeepRef;
    }

    public void execute() {
        keepEveryThing();
        LogHolder.getInstanceOf().addLog("... added Fields=" + addedFields +
                "... added Methods=" + addedMethods, false);

    }

    private void addFieldOrMethod(FieldInfo field, boolean isMethod, ClassFile cFile) {
        int ret = add2CP.addFieldInfoInCP(field, isMethod, cFile.getThisClass().intValueUnsigned());
        if (ret != -1) {
            if (isMethod) {
                addedMethods++;
            } else {
                addedFields++;
            }
        }
    }

    private void keepEveryThing() {
        ClassFileController controller = ClassFileController.getInstanceOf();
        int size = controller.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            ClassFile cFile = (ClassFile) controller.get(loop);
            keepWholeClass(cFile);
        }
    }

    private void keepAllFunctionsOrFields(ClassFile cFile, boolean isMethod) {
        ControllerBase base = null;
        if (isMethod) {
            base = cFile.getMethodInfoController();
        } else {
            base = cFile.getFieldInfoController();
        }
        keepAllFunctionsOrFields(cFile, base, isMethod);
    }

    private void keepAllFunctionsOrFields(ClassFile cFile, ControllerBase base,
            boolean isMethod) {

        int size = base.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            addFieldOrMethod((FieldInfo) base.get(loop), isMethod, cFile);
        }
    }

    private void keepWholeClass(ClassFile cFile) {
        keepAllFunctionsOrFields(cFile, true);
        keepAllFunctionsOrFields(cFile, false);
    }
}
