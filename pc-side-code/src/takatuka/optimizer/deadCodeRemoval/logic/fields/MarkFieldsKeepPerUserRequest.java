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

import java.util.*;
import takatuka.classreader.dataObjs.FieldInfoController;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.xml.*;
import takatuka.optimizer.deadCodeRemoval.logic.xml.*;

/**
 * 
 * Description:
 * <p>
 * Read the XML.
 * Mark those fields as keep which are specified in the xml file by the user.
 * These fields should never be removed.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class MarkFieldsKeepPerUserRequest {

    private final static MarkFieldsKeepPerUserRequest mFKPUR = new MarkFieldsKeepPerUserRequest();
    private Oracle oracle = Oracle.getInstanceOf();

    private MarkFieldsKeepPerUserRequest() {
    //no one create me but me
    }

    public static MarkFieldsKeepPerUserRequest getInstanceOf() {
        return mFKPUR;
    }

    public void execute() {
        startFromXMLSpecifiedPackages();
        startFromXMLSpecifiedClasses();
    }

    private void startFromXMLSpecifiedPackages() {
        Vector classes = DCXMLUtil.getInstanceOf().getAllClassesFromPackageXML();
        Iterator<DCClassFile> dcIt = classes.iterator();
        while (dcIt.hasNext()) {
            DCClassFile cFile = dcIt.next();
            keepAllFields(cFile);
        }
    }

    private void startFromXMLSpecifiedClasses() {
        Vector<ClassFileXML> classFileXMLVec = ReadXMLForKeepReferences.getInstanceOf().getClassFileXML();
        Iterator<ClassFileXML> dcIt = classFileXMLVec.iterator();
        while (dcIt.hasNext()) {
            ClassFileXML xmlClass = dcIt.next();
            //Miscellaneous.println("-----------> "+xmlClass.getName()+", "+xmlClass);
            DCClassFile cFile = (DCClassFile) oracle.getClass(xmlClass.getName());
            if (cFile == null) {
                LogHolder.getInstanceOf().addLog("Warning: XML error, cannot find class-file " + xmlClass, true);
                //Miscellaneous.exit();
                continue;
            }
            if (cFile != null) {
                if (xmlClass.isIncludeAllFields()) {
                    keepAllFields(cFile);
                } 
            }
            //Miscellaneous.println(xmlClass.isIncludeAllFields()+", "+xmlClass.getFields().size());
            if (!xmlClass.isIncludeAllFields() && xmlClass.getFields().size() > 0) {
                keepSelectedFields(cFile, xmlClass.getFields());
            }
        }
    }

    private void keepAllFields(DCClassFile cFile) {
        FieldInfoController cont = cFile.getFieldInfoController();
        int size = cont.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            DCFieldInfo field = (DCFieldInfo) cont.get(loop);
            field.setKeepPerUserReqOrStatic();
        }
    }

    private void keepSelectedFields(DCClassFile cFile, Vector<FunctionAndFieldXML> fields) {
        for (int loop = 0; loop < fields.size(); loop++) {
            keepSelectedField(cFile, fields.elementAt(loop));
        }
    }

    private void keepSelectedField(DCClassFile cFile, FunctionAndFieldXML field) {
        DCFieldInfo dcField = (DCFieldInfo) oracle.getMethodOrField(cFile,
                field.getName(), field.getDescription(), false);
        if (dcField == null) {
            LogHolder.getInstanceOf().addLog("A field to keep was null", true);
            return;
        }
        dcField.setKeepPerUserReqOrStatic();
    }
}
