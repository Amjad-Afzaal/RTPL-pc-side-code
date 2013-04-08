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
package takatuka.vm.autoGenerated;

import takatuka.classreader.logic.file.*;
import java.util.*;
import java.io.*;
import takatuka.vm.autoGenerated.tables.*;
import takatuka.classreader.logic.util.*;

public class ReferencesTable {

    protected String REFERENCE_TABLE_PROPERTY = "JAVA_REFERENCES_TABLE_HEADER";
    private static final ReferencesTable referencesTb = new ReferencesTable();
    private static final ConfigPropertyReader cPropReader = 
            ConfigPropertyReader.getInstanceOf();

    protected ReferencesTable() {
    }

    public static ReferencesTable getInstanceOf() {
        return referencesTb;
    }

    protected StringBuffer getTableString(StringBuffer bigString, TreeSet table, String idStr) {
        String longName = "";
        Iterator it = table.iterator();
        while (it.hasNext()) {
            ReferenceTableEntry entry = (ReferenceTableEntry) it.next();

            longName = "#define" + " " + entry.getLongName() + "_" + idStr + " 0x" +
                    Integer.toHexString(entry.getReferenceCPId() & 0xFFFFFFFF)+" /*"+
                    entry.getOriginalName()+"*/";
            bigString.append(longName + "\n");
        }
        return bigString;
    }

    public void writeTable() {
        try {
            TreeSet classTable = ReferenceTableCreator.getInstanceOf().getClassTable();
            TreeSet methodTable = ReferenceTableCreator.getInstanceOf().getMethodTable();
            TreeSet fieldTable = ReferenceTableCreator.getInstanceOf().getFieldTable();
            String fileName = cPropReader.
                    getConfigProperty(REFERENCE_TABLE_PROPERTY);
            StringBuffer bigString = new StringBuffer(HeaderFileConstants.
                    headerStart(fileName));
 
            bigString = getTableString(bigString, classTable, "classId");
            bigString = getTableString(bigString, methodTable, "methodId");
            bigString = getTableString(bigString, fieldTable, "fieldId");

            bigString.append(HeaderFileConstants.headerEnd());
            ClassFileWriter.writeFile(new File(fileName), bigString.toString());

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}