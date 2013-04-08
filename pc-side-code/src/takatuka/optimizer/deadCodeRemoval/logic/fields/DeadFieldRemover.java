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
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam, Christoph Gonsior, Sebastian Wagner, Natascha Widder
 * @version 1.0
 */
public class DeadFieldRemover {

    private boolean isAnyFieldOrCodeRemoved = false;
    private int totalFieldsRemoved = 0;
    private static final DeadFieldRemover deadFRemover = new DeadFieldRemover();
    private Oracle oracle = Oracle.getInstanceOf();
    private LogHolder logHolder = LogHolder.getInstanceOf();

    private DeadFieldRemover() {
    //no one creates me but me
    }

    public static DeadFieldRemover getInstanceOf() {
        return deadFRemover;
    }

    /**
     * 1. Go to all the functions and travers the bytecode. Depending upon the
     * bytecode instruction found mark filds as FIELD_NOT_REFERRED, FIELD_ONLY_PUT,and  FIELD_GET.
     * depending upon bytecode instructions getfield, putfield, getstatic, putstatic.
     * 
     * 2. Go to all the fields and check if they are unmarked then remove them. 
     */
    public void execute() {
        
        MarkFieldsKeepPerUserRequest.getInstanceOf().execute();
        RemoveFieldBytecode rmFieldBC = RemoveFieldBytecode.getInstanceOf();
        while (true) {
            //Miscellaneous.println("\n---------------- Start of iteration ---------------");
            isAnyFieldOrCodeRemoved = false;
            //PrintFieldsStatus.getInstanceOf().printAllFields();

            MarkReferredFields.getInstanceOf().markFields();

            //PrintFieldsStatus.getInstanceOf().printAllFields();
            removeUnMarkedFields();

            //PrintFieldsStatus.getInstanceOf().printAllFields();

            //Todo still have some errors rmFieldBC.execute();
            if (isAnyFieldOrCodeRemoved || rmFieldBC.isRemoveBC()) {
                InitialiseAllFields.getInstanceOf().initAllFields();
                continue;
            } else {
                break;
            }
        /*//Miscellaneous.println("...... next round .....");
        PrintFieldsStatus.getInstanceOf().printAllFields();*/
        }
        logHolder.addLog("Dead-code-Removal: Total fields removed=" + totalFieldsRemoved, true);
    }

    /**
     * 1. Go to all the classfiles (using class file controller).
     * 2. Get all the fields of a classfile. 
     *    -- If the field is marked as unreferred then remove it. go to next field
     *    -- otherwise go to next field
     */
    private void removeUnMarkedFields() {
        ClassFileController cfController = ClassFileController.getInstanceOf();
        int numOfClasses = cfController.getCurrentSize();
        for (int classIndex = 0; classIndex < numOfClasses; classIndex++) {
            ClassFile cFile = (ClassFile) cfController.get(classIndex);
            removeUnMarkedFields(cFile);
        }
    }

    private void removeUnMarkedFields(ClassFile cFile) {
        FieldInfoController fieldContr = cFile.getFieldInfoController();
        MultiplePoolsFacade cp = cFile.getConstantPool();
        for (int fieldIndex = 0; fieldIndex < fieldContr.getCurrentSize(); fieldIndex++) {
            DCFieldInfo field = (DCFieldInfo) fieldContr.get(fieldIndex);
            /* //Miscellaneous.println("checking field: " + oracle.methodOrFieldName(field, cp)+
            ", with status ="+field.getFieldStatus());*/
            if (field.getFieldStatus() == DCFieldInfo.FIELD_NOT_REFERRED &&
                    !field.isFMKeepPerUserRequest() &&
                    !field.isKeepWithMayBe()) {
                ClassFile.currentClassToWorkOn = cFile;
                if (fieldContr.remove(fieldIndex) != null /*&& !field.getAccessFlags().isFinal()*/) {
//                    //Miscellaneous.println("----------------------");
                    //Miscellaneous.println(totalFieldsRemoved + ": Removed Field -NameIndex: " +
//                            oracle.methodOrFieldName(field, cp) + " === " +
//                            cFile.getFullyQualifiedClassName());
                    totalFieldsRemoved++;    //counts how many fields we removed
                    isAnyFieldOrCodeRemoved = true;
                    fieldIndex--;
                }
            }
        }
    }
}
