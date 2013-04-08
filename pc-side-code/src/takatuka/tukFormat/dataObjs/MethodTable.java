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
package takatuka.tukFormat.dataObjs;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.io.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodTable implements BaseObject {

    private LFClassFile cFile = null;
    private Un methodCountUn = null;
    private Vector<Un> nameAndTypeVec = new Vector<Un>();
    private Vector<Un> methodCPIndexVec = new Vector<Un>();
    private Vector<String> methodName = new Vector<String>();
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();

    /**
     * 
     * @param cFile
     */
    public MethodTable(LFClassFile cFile) {
        this.cFile = cFile;
        try {
            init();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    /**
     * 
     * @return
     */
    public Vector<Un> getMethodsNameAndTypeInfoIndex() {
        return nameAndTypeVec;
    }

    /**
     *
     * @return
     */
    public Vector<Un> getMethodCPIndeses() {
        return methodCPIndexVec;
    }

    /**
     * 
     * @throws Exception
     */
    private void init() throws Exception {
        ControllerBase mineInfos = cFile.getMethodInfoController();
        LFFieldInfo temp = null;
        mineInfos = cFile.getReferedMethodFieldAddressTables(false);
        mineInfos.sort(new FieldComparator());
        methodCountUn = factory.createUn(mineInfos.getCurrentSize()).trim(2);
        Un nameAndType = null;
        Un methodCPIndex = null;
        for (int loop = 0; loop < mineInfos.getCurrentSize(); loop++) {
            temp = (LFFieldInfo) mineInfos.get(loop);
            if (temp.getAccessFlags().isStatic()) {
                continue;
            }
            if ((nameAndType = LFFieldInfo.computeNameAndTypeInfo(temp)) == null) {
                nameAndType = factory.createUn(0).trim(2);
            }
            int index = LFFieldInfo.computeRefInfoUsingInfo(temp);
            if (index == -1) {
                continue;
            }
            methodCPIndex = factory.createUn(index).trim(2);
            nameAndTypeVec.addElement(nameAndType);
            methodCPIndexVec.addElement(methodCPIndex);
            methodName.addElement(temp.getName());
        }
    }

    /**
     *
     * @param buff
     * @return
     * @throws Exception
     */
    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws Exception {
        String methodCount = "\nmethod_count=" + factory.createUn(nameAndTypeVec.size()).trim(2).writeSelected(buff);
        String methodTable = "\nmethod_table=\n";
        for (int loop = 0; loop < nameAndTypeVec.size(); loop++) {
            methodTable = methodTable + "\t(name=" + nameAndTypeVec.elementAt(loop).writeSelected(buff);
            methodTable = methodTable + ", " + methodCPIndexVec.elementAt(loop).writeSelected(buff)
                    + ")" + "// Function-Name=" + methodName.elementAt(loop) + "\n";
        }
        return methodCount + methodTable;
    }

    @Override
    public String toString() {
        try {
            return writeSelected(null);
        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }
}
