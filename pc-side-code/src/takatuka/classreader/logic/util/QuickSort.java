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
package takatuka.classreader.logic.util;

import java.util.*;


/**
 * <p>Title: </p>
 * <p>Description: This class implement a generic quick sort. 
 * It depends on the Comparator implentation
 *  That who and what you want to sort</p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class QuickSort {

    private static Comparator comp = null;

    public static void sort(Vector array, int startIndex, int endIndex,
                            Comparator comp1) {
        comp = comp1;
        if (comp1 != null) {
            sort(array, startIndex, endIndex);
        }
    }

    public static void sort(Vector array, Comparator comp1) {
        comp = comp1;
        if (comp1 != null) { //if null then we do not sort.
            sort(array, 0, array.size() - 1);
        }
    }

    private static void sort(Vector array, int start, int end) {
        int p;
        if (end > start) {
            p = partition(array, start, end);
            sort(array, start, p - 1);
            sort(array, p + 1, end);
        }
    }

    private static int partition(Vector array, int start, int end) {
        int left, right;

        // Arbitrary partition start...there are better ways...
        Object partitionElement = array.elementAt(end);

        left = start - 1;
        right = end;
        for (; ; ) {
            //Miscellaneous.println(partitionElement+" : "+left);
            while (comp.compare(partitionElement, array.elementAt(++left)) > 0) {
                if (left == end) {
                    break;
                }
            } while (comp.compare(partitionElement, array.elementAt(--right)) <
                     0) {
                if (right == start) {
                    break;
                }
            }

            if (left >= right) {
                break;
            }

            swap(array, left, right);
        }
        swap(array, left, end);

        return left;
    }

    private static void swap(Vector array, int i, int j) {
        Object temp;
        temp = array.elementAt(i);
        array.setElementAt(array.elementAt(j), i);
        array.setElementAt(temp, j);
    }

    public static void main(String arg[]) {
        Vector temp = new Vector();
        temp.addElement(new Integer(15));
        temp.addElement(new Integer(13));
        temp.addElement(new Integer(1));
        temp.addElement(new Integer(4));
        temp.addElement(new Integer(2));
        temp.addElement(new Integer(9));
        temp.addElement(new Integer(12));
        //sort(temp, new util.BST.IntegerComparator());
        //Miscellaneous.println(util.Miscellaneous.printVector(temp, 0));
    }
}
