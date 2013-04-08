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
package takatuka.optimizer.deadCodeRemoval.logic.xml;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.xml.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class DCXMLUtil {

    private Vector cacheForPackageClasses = new Vector();
    private Vector cCOClassFiles = new Vector();
    private final static DCXMLUtil xmlUtil = new DCXMLUtil();
    
    private DCXMLUtil() {
        //no one creates me but me.
    }

    
    public static DCXMLUtil getInstanceOf() {
        return xmlUtil;
    }
    
    private void createClassCache() {
        Vector classFiles = ClassFileController.getInstanceOf().getAll();
        cCOClassFiles = createCommonComparableObjects(classFiles);
        QuickSort.sort(cCOClassFiles, new PackageComparator());
    //Miscellaneous.println("----------------- " + cCOClassFiles);
    }

    public Vector<ClassFile> getAllClassesFromPackageXML() {
        if (cacheForPackageClasses.size() > 0) {
            return cacheForPackageClasses;
        }
        createClassCache();
        Vector packagesXMLv = ReadXMLForKeepReferences.getInstanceOf().
                getPackagesXML();
        //   Miscellaneous.println("------------ Can you see me ...................");
        for (int loop = 0; loop < packagesXMLv.size(); loop++) {
            PackageXML packXML = (PackageXML) packagesXMLv.elementAt(loop);
            //Miscellaneous.println(" Package == "+packXML);
            int classIndex = -2;
//            Miscellaneous.println(cCOClassFiles);
            while (classIndex != -1) { //all the classes of the package instead of just one
                classIndex = BinarySearch.binarySearch(cCOClassFiles.toArray(),
                        new CommonComparableClass(packXML), new PackageComparator());
                // Miscellaneous.println(" --------------> "+cCOClassFiles.size());
                if (classIndex != -1) {
                    CommonComparableClass classFileCommonObject = (CommonComparableClass) cCOClassFiles.get(classIndex);
                    //Miscellaneous.println("----------------- "+classFileCommonObject.getClassName());
                    cacheForPackageClasses.add(classFileCommonObject.getObject());
                    cCOClassFiles.remove(classIndex);
                }
            }
        }
        return cacheForPackageClasses;
    }

    private static Vector createCommonComparableObjects(Vector input) {
        Vector ret = new Vector();
        for (int index = 0; index < input.size(); index++) {
            ret.addElement(new CommonComparableClass(input.elementAt(index)));
        }
        return ret;
    }
}
