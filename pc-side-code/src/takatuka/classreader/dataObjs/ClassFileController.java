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
package takatuka.classreader.dataObjs;

import java.util.HashMap;
import takatuka.classreader.logic.StartMeAbstract;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class ClassFileController extends ControllerBase {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();
    private static final ClassFileController controller = new ClassFileController();
    private HashMap<String, ClassFile> cacheMap = new HashMap();
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    public static ClassFileController getInstanceOf() {
        return controller;
    }

    private ClassFileController(int size) {
        super(size);
    }

    @Override
    public int getMaxSize() {
        return getCurrentSize();
    }

    private void cacheClassesByName() {
        if (cacheMap.size() != 0) {
            return; //already cached
        }
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            ClassFile cFile = (ClassFile) get(loop);
            String myName = cFile.getFullyQualifiedClassName();
            cacheMap.put(myName, cFile);
        }

    }

    public ClassFile getClassByFullyQualifiedName(String className) {
        if (StartMeAbstract.getCurrentState() >= StartMeAbstract.STATE_READ_FILES) {
            cacheClassesByName();
            return cacheMap.get(className);
        }
        //I know a map could be fast but difficult to keep upto date...
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            ClassFile cFile = (ClassFile) get(loop);
            String myName = cFile.getFullyQualifiedClassName();
            if (myName.equals(className)) {
                return cFile;
            }
        }
        return null;
    }

    @Override
    public boolean remove(Object obj) {
        cacheMap = new HashMap();
        return super.remove(obj);
    }

    @Override
    public Object remove(int index) {
        cacheMap = new HashMap();
        return super.remove(index);
    }

    @Override
    public void add(Object obj) throws Exception {
        cacheMap = new HashMap();
        ClassFile cFile = (ClassFile) obj;
        if (contains(obj)) {
            logHolder.addLog("WARNING:  not adding already added class-file=" +
                    cFile.getFullyQualifiedClassName());
            return;
        }
        ClassFile.currentClassToWorkOn = cFile;
        super.add(obj);
    }

    private ClassFileController() {
        super(-1);
        super.setAllowedClassName(ClassFile.class);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "size=" +
                factory.createUn(getCurrentSize()).trim(2).
                writeSelected(buff) + "\n";
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            ret = ret + "\nClassFile {\n";
            ret = ret + ((ClassFile) get(loop)).writeSelected(buff);
            ret = ret + "}\n";
        }
        return ret;
    }

    @Override
    public String toString() {
        try {
            return writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }
}
