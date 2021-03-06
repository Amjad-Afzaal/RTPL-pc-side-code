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
package takatuka.vm.autoGenerated.forExceptionPrettyPrint;

import java.io.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.tukFormat.logic.file.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * It generates method names property file.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class FieldNamesPropertyFile {

    private static final FieldNamesPropertyFile myObj = new FieldNamesPropertyFile();
    private static StringBuffer classFieldBuffer = new StringBuffer("#classID=fieldID");
    private static final String CLASS_FIELD_PROPERTY_FILE_NAME = "classField.properties";
    private static StringBuffer fieldBuffer = new StringBuffer("#fieldID=fieldName,dataTypeSig,fieldTypeSig\n");
    private static final String FIELD_PROPERTY_FILE_NAME = "field.properties";

    /**
     * the constructor is private
     */
    private FieldNamesPropertyFile() {
    }

    public static FieldNamesPropertyFile getInstanceOf() {
        return myObj;
    }
    /**
     * Go to all the class files and generate corresponding methods names.
     * The Syntex of method names is as followed.
     * fullyQualifiedClassName.methodName(sourceFile.java)
     *
     * @param method
     * @param cFile
     */
    ClassFile lastcFile;

    public void execute(LFFieldInfo field, ClassFile cFile) {
        Oracle oracle = Oracle.getInstanceOf();
        GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
        //String fullyQualifiedClassName = cFile.getFullyQualifiedClassName();
        String fieldName = oracle.methodOrFieldName(field, gcp);
        int fieldID = field.getReferenceIndex();

        if (lastcFile != cFile) {
            classFieldBuffer.append("\n" + cFile.getThisClass().intValueUnsigned() + "=" + fieldID);
        } else {
            classFieldBuffer.append("," + fieldID);
        }
        lastcFile = cFile;

        if (field.getAccessFlags().isStatic()) {
            //static field
            fieldBuffer.append(fieldID + "=" + fieldName + "," + field.getRawTypeSignature() + ",S\n");
        } else {
            //regular field
            fieldBuffer.append(fieldID + "=" + fieldName + "," + field.getRawTypeSignature() + ",R\n");
        }
    }

    /**
     *
     * @param propertyFileName
     * @param buffer
     */
    public static void writeInPropertyFile(String propertyFileName, StringBuffer buffer) {
        try {
            String fileNamePath = LFWriter.getOutputDirectory() + "/" + propertyFileName;
            File file = new File(fileNamePath);
            if (file.exists()) {
                file.delete();
            }
            RandomAccessFile rm = new RandomAccessFile(file, "rw");
            rm.writeBytes(buffer.toString());
            rm.close();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    public void writeInPropertyFile() {
        writeInPropertyFile(FIELD_PROPERTY_FILE_NAME, fieldBuffer);
        writeInPropertyFile(CLASS_FIELD_PROPERTY_FILE_NAME, classFieldBuffer);
    }
}
