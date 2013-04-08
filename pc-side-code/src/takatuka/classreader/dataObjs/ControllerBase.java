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

import java.util.*;

import org.apache.commons.lang.builder.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ControllerBase implements BaseObject {

    private Vector objs = new Vector();
    private int size = -1;
    private Class allowedclassName = BaseObject.class;
    private HashMap map = new HashMap(); //store indexes cooreponding to tag
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();

    public ControllerBase() {
    }

    public ControllerBase(int size) {
        this.size = size;
    }

    public void setAllowedClassName(Class classNamewithpackage) {
        allowedclassName = classNamewithpackage;
    }


    public Class getAllowedClass() {
        return allowedclassName;
    }

    public boolean contains(Object obj) {
        return objs.contains(obj);
    }

    public int indexOf(Object elem) {
        return objs.indexOf(elem);
    }

    public int indexOf(Object elem, int start) {
        return objs.indexOf(elem, start);
    }

    public void clear() {
        objs.removeAllElements();
        map.clear();
    }

    public void addAll(Collection collection) {
        objs.addAll(collection);
    }

    public void sort(java.util.Comparator comp, int startIndex, int endIndex) throws
            Exception {
        QuickSort.sort(objs, startIndex, endIndex, comp);
    }

    public void sort(java.util.Comparator comp) throws Exception {
        QuickSort.sort(objs, comp);
    }

    private boolean allowedType(Object obj) {
        if (allowedclassName == null) {
            return false;
        } else if (obj.getClass().isInstance(allowedclassName)) { //subclasses are allowed.
            return true;
        }

        return false;
    }

    public void validate() throws Exception {
    }

    public Object remove(int index) {
        return objs.remove(index);
    }

    public boolean remove(Object obj) {
        return objs.remove(obj);
    }
    
    public Vector getAll() {
        return (Vector) objs.clone();
    }

    public Object [] getAllArray() {
        return getAll().toArray();
    }
    
    
    public void set(int index, Object obj) {
        objs.set(index, obj);
    }

    public void add(int index, Object obj) throws Exception {
        if (objs.size() >= size && size != -1) { //-1 is infinity
            throw new ArrayIndexOutOfBoundsException();
        }
        if (allowedType(obj)) {
            throw new Exception("Invalid Class exception ");
        }
        validate(); //overwrite validate function to add other validations.

        objs.add(index, obj);

    }

    public void add(Object obj) throws Exception {
        if (objs.size() >= size && size != -1) { //-1 is infinity
            throw new ArrayIndexOutOfBoundsException();
        }
        if (allowedType(obj)) {
            throw new Exception("Invalid Class exception ");
        }
        validate(); //overwrite validate function to add other validations.

        objs.addElement(obj);
    }

    public Object get(int index) {
        return objs.elementAt(index);
    }

    public int getCurrentSize() {
        return objs.size();
    }

    /**
     * return -1 in case of infinite maximum size
     * @return int
     */
    public int getMaxSize() {
        return size;
    }

    public void setMaxSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ControllerBase)) {
            return false;
        }
        ControllerBase base = (ControllerBase) obj;
        if (base.objs.equals(objs)) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(objs).toHashCode();
    }

    @Override
    public String toString() {
        String ret = "";
        try {
            ret = ret + writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "size=" +
                factory.createUn(getCurrentSize()).trim(2).
                writeSelected(buff) + "\n";
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            ret = ret + ((BaseObject) get(loop)).writeSelected(buff);
            if (loop + 1 < getCurrentSize()) {
                ret = ret + ",";
            }
        }
        return ret;
    }
}
