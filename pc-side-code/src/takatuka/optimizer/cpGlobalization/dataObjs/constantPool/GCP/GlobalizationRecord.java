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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP;

import java.util.*;
import takatuka.classreader.logic.util.*;
import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.io.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class GlobalizationRecord implements BaseObject {

    private BaseObject obj = null;
    //by default all could be duplicates
    private boolean mayBeDuplicate = true;
    private Vector oldIndexRecordVec = new Vector();    //count that how many times a constant pool entry has been referred
    private int referrenceCount = 0;
    private int referrenceCountWithinCP = 0;
    public GlobalizationRecord(BaseObject obj, int oldIndex,
            boolean mayBeDuplicate,
            ClassFile classFile, int poolId) {
        this.obj = obj;
        this.oldIndexRecordVec.addElement(new classNameOldIndex(classFile,
                oldIndex, poolId));
        this.mayBeDuplicate = mayBeDuplicate;
    }

    public void removeAllKeys() {
        this.oldIndexRecordVec.removeAllElements();
    }
    
    public void setObject(BaseObject obj) {
        this.obj = obj;
    }
    /**
     * 
     * @return the number of times a given CP entery has been referred from bytecode
     */
    public int getReferredCount() {
        return referrenceCount;
    }

    public void setReferredCount(int count) {
        this.referrenceCount = count;
    }

    public int getReferredCountFromCPObjects() {
        return referrenceCountWithinCP;
    }
    
    public void setReferredCountFromCPObjects(int count) {
        this.referrenceCountWithinCP = count; 
    }
    
    public static String createKey(ClassFile classFile, int oldIndex, int poolId) {
        return new classNameOldIndex(classFile, oldIndex, poolId).toString();
    }

    private static class classNameOldIndex {

        ClassFile classFile = null;
        int oldIndex = 0;
        int poolId = -1;

        public classNameOldIndex(ClassFile classFile, int oldIndex, int poolId) {
            this.classFile = classFile;
            this.oldIndex = oldIndex;
            this.poolId = poolId;
        }

        @Override
        public boolean equals(Object obj) {
            try {
                if (obj == null || !(obj instanceof classNameOldIndex)) {
                    return false;
                }
                classNameOldIndex objInput = (classNameOldIndex) obj;
                if (objInput.classFile.getSourceFileNameWithPath().equals(classFile.getSourceFileNameWithPath()) &&
                        objInput.oldIndex == oldIndex &&
                        poolId == poolId) {
                    return true;
                }
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return false;
        }

        @Override
        public int hashCode() {
            try {
                return new HashCodeBuilder().append(classFile.getSourceFileNameWithPath()).
                        append(oldIndex).toHashCode();
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return 0;
        }

        @Override
        public String toString() {
            String ret = "";
            try {
                ret = ret + "{" + classFile.getSourceFileNameWithPath() + ", " +
                        Integer.toHexString(oldIndex & 0xFFFFFF) +
                        "," + poolId + "}";
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return ret;
        }
    }

    public String getKey(int index) {
        return (String) ((classNameOldIndex) oldIndexRecordVec.elementAt(
                index)).toString();
    }

    public ClassFile getClass(int index) {
        return (ClassFile) ((classNameOldIndex) oldIndexRecordVec.elementAt(
                index)).classFile;
    }

    public int getOldIndex(int index) {
        return ((classNameOldIndex) oldIndexRecordVec.elementAt(index)).oldIndex;
    }

    public int getPoolId(int index) {
        return ((classNameOldIndex) oldIndexRecordVec.elementAt(index)).poolId;
    }
    /*  public Vector getClassNames() {
    return classNames;
    }
     */

    public void setMayBeDuplicate(boolean mayBeDuplicate) {
        this.mayBeDuplicate = mayBeDuplicate;
    }

    public boolean getMayBeDuplicate() {
        return mayBeDuplicate;
    }

    public BaseObject getObject() {
        return obj;
    }

    /*public Vector getOldIndexes() {
    return oldIndexes;
    }*/
    public void addKey(GlobalizationRecord rec, int index) {
        oldIndexRecordVec.addElement(new classNameOldIndex(
                rec.getClass(index), rec.getOldIndex(index),
                rec.getPoolId(index)));
    }

    public void addKey(int oldIndex, ClassFile classFile, int poolId) {
        oldIndexRecordVec.addElement(new classNameOldIndex(classFile,
                oldIndex, poolId));
    }

    public int size() {
        return oldIndexRecordVec.size();
    }

    public boolean contains(int oldIndex, ClassFile classFile, int poolId) {
        classNameOldIndex input = new classNameOldIndex(classFile,
                oldIndex, poolId);
        return oldIndexRecordVec.contains(input);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlobalizationRecord)) {
            return false;
        }

        if (obj instanceof GlobalizationRecord) {
            GlobalizationRecord objmap = (GlobalizationRecord) obj;
            if (objmap.obj.equals(this.obj)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(obj).toHashCode();
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
        BaseObject baseObj = (BaseObject) obj;
        String ret = baseObj.writeSelected(buff) + " referred-BC =" +
                getReferredCount()+ ", referred-CP="+getReferredCountFromCPObjects();
                /*
        ret = ret + "\n\t";
        ret = ret + takatuka.classreader.logic.util.StringUtil.getString(oldIndexRecordVec, "\n\t");
                 */
        return ret;
    }
}
