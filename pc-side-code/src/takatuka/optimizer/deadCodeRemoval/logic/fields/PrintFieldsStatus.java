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

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Christoph Gonsior, Sebastian Wagner, Natascha Widder, Faisal Aslam
 * @version 1.0
 */
public class PrintFieldsStatus {

    private static final PrintFieldsStatus fieldsStatus = new PrintFieldsStatus();
    private GlobalConstantPool cp = GlobalConstantPool.getInstanceOf();
    //constructor
    private PrintFieldsStatus() {
    }

    public static PrintFieldsStatus getInstanceOf() {
        return fieldsStatus;
    }

    public void printAllFields() {
        Miscellaneous.println("\n\n");
        ClassFileController cfController = ClassFileController.getInstanceOf();
        int numOfClasses = cfController.getCurrentSize();
        for (int classIndex = 0; classIndex < numOfClasses; classIndex++) {
            ClassFile cFile = (ClassFile) cfController.get(classIndex);
            printAllFields(cFile);
        }
    }

    public void printAllFields(ClassFile cFile) {
        ClassFile.currentClassToWorkOn = cFile;
        FieldInfoController fieldContr = cFile.getFieldInfoController();
        for (int fieldIndex = 0; fieldIndex < fieldContr.getCurrentSize(); fieldIndex++) {
            DCFieldInfo field = (DCFieldInfo) fieldContr.get(fieldIndex);
            Un nameIndex = field.getNameIndex();
            int status = field.getFieldStatus();
            String fieldName = getFieldName(nameIndex);
            Miscellaneous.println(cFile.getFullyQualifiedClassName()+"->Field: " + fieldName + " - FieldStatus: " + analyseFieldStatus(status));
        }
    }

    private String getFieldName(Un nameIndex) {
        UTF8Info utf8 = (UTF8Info) cp.get(nameIndex.intValueUnsigned(), TagValues.CONSTANT_Utf8);
        return utf8.convertBytes();
    }

    private String analyseFieldStatus(int i) {
        String status;
        if (i == 0) {
            status = "FIELD_ONLY_PUT";
        } else if (i == 1) {
            status = "FIELD_GET";
        } else if (i == -1) {
            status = "FIELD_NOT_REFERRED";
        } else {
            status = "not defined!!!";
        }
        return status;
    }
}
