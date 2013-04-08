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
package takatuka.optimizer.deadCodeRemoval.logic.classAndMethod;

import java.util.*;

/**
 * 
 * Description:
 * <p> 
 *  List of methods to be examine and traverse by deadcode removal algorithm
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class ExamineList {

    private static final ExamineList myObj = new ExamineList();
    private Vector<MethodExamineListEntry> examineList = new Vector();
    private HashMap<MethodExamineListEntry, MethodExamineListEntry> oldEntries =
            new HashMap();

    private ExamineList() {
        //no one creates me but me.
    }

    public static ExamineList getInstanceOf() {
        return myObj;
    }

    public int size() {
        return examineList.size();
    }


    public boolean add(String fullyqalifClassName, String methodName, String methodDesc,
            boolean isGotMethodFromBytecodeTraverse, boolean isUserRequested) {
        MethodExamineListEntry entry = new MethodExamineListEntry(fullyqalifClassName,
                methodName, methodDesc, isGotMethodFromBytecodeTraverse, isUserRequested);
        return add(entry);
    }

    public MethodExamineListEntry getLastEntry() {
        return examineList.lastElement();
    }

    public Vector<MethodExamineListEntry> getAllEntries() {
        return (Vector<MethodExamineListEntry>) examineList.clone();
    }

    public boolean add(MethodExamineListEntry entry) {
        if (oldEntries.get(entry) == null) {
            examineList.addElement(entry);
            oldEntries.put(entry, entry);
            return true;
        }
        return false;
    }

    public MethodExamineListEntry remove() {
        if (examineList.size() == 0) {
            return null;
        }
        return examineList.remove(0);
    }

    @Override
    public String toString() {
        String ret = "";
        for (int loop = 0; loop < examineList.size(); loop++) {
            ret += examineList.elementAt(loop).toString() + "\n";
        }
        return ret;
    }
}
