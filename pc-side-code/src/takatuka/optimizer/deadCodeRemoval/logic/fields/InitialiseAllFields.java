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
package takatuka.optimizer.deadCodeRemoval.logic.fields;

import takatuka.classreader.dataObjs.ClassFile;
import takatuka.classreader.dataObjs.ClassFileController;
import takatuka.classreader.dataObjs.FieldInfoController;
import takatuka.optimizer.deadCodeRemoval.dataObj.DCFieldInfo;

/**
 *
 * @author Christoph Gonsior
 */
public class InitialiseAllFields {

    private static final InitialiseAllFields initAllFields = new InitialiseAllFields();

    //constructor
    private InitialiseAllFields() {
    }

    public static InitialiseAllFields getInstanceOf() {
        return initAllFields;
    }

    public void initAllFields() {
        ClassFileController cfController = ClassFileController.getInstanceOf();
        int numOfClasses = cfController.getCurrentSize();
        FieldInfoController fieldContr = null;
        //all class files
        for (int classIndex = 0; classIndex < numOfClasses; classIndex++) {
            ClassFile cFile = (ClassFile) cfController.get(classIndex);
            fieldContr = cFile.getFieldInfoController();
            getAllFields(fieldContr);
        }
    }

    private void getAllFields(FieldInfoController fieldContr) {
        //all fields
        for (int fieldIndex = 0; fieldIndex < fieldContr.getCurrentSize(); fieldIndex++) {
            DCFieldInfo field = (DCFieldInfo) fieldContr.get(fieldIndex);
            field.init();
        }
    }
}
