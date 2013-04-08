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
import takatuka.classreader.logic.util.*;
/**
 *
 * @author aslam
 */
class PackageComparator implements Comparator<CommonComparableClass> {

    PackageComparator() {
    }

    @Override
    public int compare(CommonComparableClass commonClass1, CommonComparableClass commonClass2) {
        String package1 = commonClass1.getPackageName();
        String package2 = commonClass2.getPackageName();
        
        int ret = 0;
        if (!package1.equals(package2)) {
           ret =  compareGroups(new StringTokenizer(package1, "/"),
                new StringTokenizer(package2, "/"));
        }
        //Miscellaneous.println("p1="+package1+", p2="+ package2+ ", ret = "+ret);
        return ret;
    }

    private int compareGroups(StringTokenizer token1, StringTokenizer token2) {
        int ret = 0;
        try {
            while (token1.hasMoreTokens() || token2.hasMoreTokens()) {
                String package1 = "_";
                String package2 = "_";
                if (token1.hasMoreElements()) {
                    package1 = (String) token1.nextToken();
                }
                if (token2.hasMoreElements()) {
                    package2 = (String) token2.nextToken();
                }
                ret = package1.compareToIgnoreCase(package2);
                if (ret != 0) {
                    if (package1.equals("*") || package2.equals("*")) {
                        return 0;
                    }
                    return ret;
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        //Miscellaneous.println(token1Count + ", " + token2Count);
        return ret;
    }
}
