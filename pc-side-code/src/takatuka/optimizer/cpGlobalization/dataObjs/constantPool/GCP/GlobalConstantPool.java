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
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.LFBaseObject;
import takatuka.tukFormat.logic.util.SizeCalculator;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 * ********************** READ PHASE *********************
 * 1. We use this same static class as a constant pool for all the classes.
 * Where different pools are created based on tags of constant pool objects. 
 * 
 * 2. Object will not be at indexes as in original constant pools because all classes
 * will now have this common pool however, old constant pools values are saved in
 * Globalization record.
 *  
 * 3. During class file reading the get function will return values based on old 
 * values stored. 
 * 
 * ***************** Unique Values Creating Phase **********************
 * 4. After ending the read Phase, unique values phase starts. 
 * 5. In this phase values are made uniques and their reference are updated. 
 * Based on the dependency graph we divide this Phase into four phase. 
 * In GlobalConstantPool following are updated. 
 *          UTF8Info, LongInfo, IntegerInfo, FloatInfo and DoubleInfo
 * 
 * We update globalization record as duplicate values are deleted. In order to 
 * make deleting process fast we divide it into two steps. 1) Identify 2) Delete.
 * In the Identify-phase we traverse values once. If they are unique we copy them 
 * in a vector. 
 * In case they are not unique (i.e. already a unique value exists in the copied vector) 
 * then we update copied vector Globalization record. At the end of this process we
 * replace copied vector with old constant Pool Vector.
 * 6. Finally we sort elements of Phase and create a HashMap with 
 * key=old-index+classname+tag, value=new-index so that other Phases can 
 * quickly access any value of this phase. We call that function Quick Access function.
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 *
 */
public class GlobalConstantPool extends MultiplePoolsFacade implements Phase {

    private HashMap uniques = new HashMap();
    public static final int constantPoolIndexSizeInBytes = 2;
    private static final FactoryFacade factory =
            FactoryPlaceholder.getInstanceOf().getFactory();
    public static boolean isExecuted = false;
    public HashMap nonUniqueEnteries = new HashMap();
    private static GlobalConstantPool pOne = null;
    private TreeMap mathMap = new TreeMap();    //one should not use it during execute
    //all the constant are used by ldc or constantValue_attribute. Hence we need to find tag base on old index
    private static HashMap<String, Integer> constantsTags = new HashMap();
    //following variables are used in adding
    private static String currentClassName = null;
    private static int cpCount = 1;
    private static HashMap groupsReferCount = new HashMap();
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    private int totalEntriesCountOfOriginalConstantPool = 0;
    private long totalOriginalConstantPoolSizeInBytes = 0;
    private HashMap cacheForGet = new HashMap();
    public static final byte STATIC_FIELD_GROUP_ID = -1;
    private boolean isGlobalizationStarted = false;

    /**
     * constructor is private to make sure that no one create object of it other
     * than the class itself. The class is singleton.
     * @param poolIds
     * @param maxSize
     */
    private GlobalConstantPool(TreeSet poolIds, int maxSize) {
        super(poolIds, maxSize);
        super.setAllAllowedClassName(GlobalizationRecord.class);
    }

    /** 
     * create singleton object if not already created otherwise, returns existing
     * object
     * @param keys
     * @param maxSize
     * @return
     */
    public static GlobalConstantPool getInstanceOf(TreeSet keys, int maxSize) {
        if (pOne == null) {
            pOne = new GlobalConstantPool(keys, maxSize);
        }
        return pOne;
    }

    /**
     * returns an already created object using other getInstanceOf(TreeSet, int)
     * This function will return null if getInstanceOf(TreeSet, int) is not already
     * called once.
     * @return
     */
    public static GlobalConstantPool getInstanceOf() {
        if (pOne == null) {
            try {
                pOne = (GlobalConstantPool) factory.createConstantPool();
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
        }
        return pOne;
    }

    public void printPhase(int tag) {
        Miscellaneous.println(getPool(tag));
    }

    public Vector getAllforDebugging(int poolId) {
        return getPool(poolId).getAll();
    }

    @Override
    public Object[] getAllArray(int poolId) {
        return getAll(poolId).toArray();
    }

    @Override
    public Vector getAll(int poolId) {
        Vector originalVec = getPool(poolId).getAll();
        Vector retVec = new Vector();
        for (int loop = 0; loop < originalVec.size(); loop++) {
            GlobalizationRecord gRec = (GlobalizationRecord) originalVec.elementAt(loop);
            retVec.addElement(gRec.getObject());
        }
        return retVec;
    }

    public boolean isPhaseTag(int tag) {
        if (tag == TagValues.CONSTANT_Utf8 || tag == TagValues.CONSTANT_Long ||
                tag == TagValues.CONSTANT_Integer ||
                tag == TagValues.CONSTANT_Float || tag == TagValues.CONSTANT_Double) {
            return true;
        }
        return false;
    }

    public boolean isPhaseElm(InfoBase obj) {
        try {
            int tag = obj.getTag().intValueUnsigned();
            return isPhaseTag(tag);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return false;
    }

    public int getTotalEntriesIncludingDuplicates() {
        return this.totalEntriesCountOfOriginalConstantPool;
    }

    public int getTotalEntriesWithoutCountCategoryTwoTwice() {
        int currentSize = super.getCurrentSize();
        ControllerBase pool = getPool(TagValues.CONSTANT_Class);
        if (pool.getCurrentSize() != 0) {
            currentSize--; //as one object at the top is empty object and should not be counted.
        }
        return currentSize;

    }

    /**
     * replace a constant pool entry with empty info and return the size of original entry
     * @param index
     * @param tag
     * @return
     * @throws java.lang.Exception
     */
    public int replaceWithEmptyInfo(int index, int tag) throws Exception {

        int ret = 0;
        GlobalizationRecord gRec = (GlobalizationRecord) super.get(index, tag);
        ret = SizeCalculator.getObjectSize(gRec.getObject());
        EmptyInfo emptyInfo = new EmptyInfo();
        emptyInfo.setDebugInfo(gRec.getObject().writeSelected(null));
        gRec.setObject(emptyInfo);
        //Miscellaneous.println(index+", "+tag+"see me "+gRec);
        gRec.removeAllKeys();
        return ret;
        //gRec.setMayBeDuplicate(false);
    }

    public int getReferredCountFromCPObjects(int index, int tag) throws Exception {
        GlobalizationRecord gRec = (GlobalizationRecord) super.get(index, tag);
        return gRec.getReferredCountFromCPObjects();
    }

    public void incReferredCountFromCPObjects_oldIndex(int oldIndex, int tag) throws Exception {
        ClassFile cFile = ClassFile.currentClassToWorkOn;
        oldIndex = getGlobalIndex(oldIndex, cFile, tag);
        GlobalizationRecord gRec = (GlobalizationRecord) super.get(oldIndex, tag);
        gRec.setReferredCountFromCPObjects(gRec.getReferredCountFromCPObjects() + 1);
    }

    public int getReferenceCount(int index, int tag) throws Exception {
        GlobalizationRecord gRec = (GlobalizationRecord) super.get(index, tag);
        return gRec.getReferredCount();
    }

    public void incReferenceCount_oldIndex(int oldIndex, int tag) {
        try {
            ClassFile cFile = ClassFile.currentClassToWorkOn;
            oldIndex = getGlobalIndex(oldIndex, cFile, tag);
            GlobalizationRecord gRec = (GlobalizationRecord) super.get(oldIndex, tag);
            gRec.setReferredCount(gRec.getReferredCount() + 1);
        } catch (Exception d) {
            Miscellaneous.printlnErr(tag + " " + oldIndex + " " +
                    ClassFile.currentClassToWorkOn.getSourceFileNameWithPath());
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    /**
     * In case of execute is not yet called then it return older index value
     * otherwise, it return newer indexed values
     * Before globalization starts the parameter index is taken as oldIndex
     * During and after globalization the index is take as current global index
     * @param index
     * @param tag
     * @return
     */
    @Override
    public Object get(int index, int tag) {

        ClassFile cFile = ClassFile.currentClassToWorkOn;
        if (isExecuted) { //return older index value
            return ((GlobalizationRecord) super.get(index, tag)).getObject();
        }
        String key = GlobalizationRecord.createKey(cFile, index, tag);
        if (cacheForGet.get(key) != null) {
            return cacheForGet.get(key);
        }
        int currentIndex = -1;
        try {
            currentIndex = getGlobalIndex(index, cFile, tag);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        GlobalizationRecord value = (GlobalizationRecord) super.get(currentIndex, tag);
        Object ret = (InfoBase) value.getObject();
        cacheForGet.put(key, ret);
        return ret;

    }

    /**
     * for each classinfo it is represent an interface then a boolean values is 
     * made true.
     * Step 1: Go to each class file and set it to current class
     * STep 2; Get from it each classinfo
     * Step 3:
     */
    void markClassInfoWithInterfaces() {
        int tag = TagValues.CONSTANT_Class;
        int size = getCurrentSize(tag);
        Oracle oracle = Oracle.getInstanceOf();
        ClassFileController cCont = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < size; loop++) {
            GlobalizationRecord gRec = (GlobalizationRecord) getPool(tag).get(loop);
            if (gRec.getObject() instanceof EmptyInfo) {
                continue;
            }
            ClassFile cFile = getClass(loop, tag);
            ClassFile.currentClassToWorkOn = cFile;
            ClassInfo cInfo = (ClassInfo) gRec.getObject();
            UTF8Info utf8CName = (UTF8Info) get(cInfo.getIndex().intValueUnsigned(), TagValues.CONSTANT_Utf8);
            String cName = utf8CName.convertBytes();
            cInfo.setClassName(cName);
            ClassFile cFileUsed = oracle.getClass(cName);
            if (cFileUsed == null) {
                continue; //may be removed by dead code removal.
            }
            cInfo.setIsInterface(cFileUsed.getAccessFlags().isInterface());
        }
    }

    public Object removeSimple(int index, int tag) {
        cacheForGet.clear();
        GlobalizationRecord ret = (GlobalizationRecord) super.remove(index, tag);
        if (ret != null) {
            return ((GlobalizationRecord) ret).getObject();
        } else {
            return null;
        }
    }

    /**
     * This remove only remove elements which are marked. That means elements
     * with myBeDuplicate flag set to true. 
     * @param index
     * @param tag
     * @return
     */
    @Override
    public Object remove(int index, int tag) {
        if (tag == TagValues.CONSTANT_Class && index == 0) {
            return null;
        }
        cacheForGet.clear();
        GlobalizationRecord ret = (GlobalizationRecord) super.remove(index, tag);
        try {
            if (!ret.getMayBeDuplicate() || ret.getObject() instanceof EmptyInfo) {
                return ret;
            }
            int temp = -1;
            int oldTemp = -1;
            while (true) {
                temp = indexOf(ret, temp + 1, tag);
                if (temp == -1 || oldTemp > temp) {
                    throw new Exception(
                            "Cannot remove or infinite loop while" +
                            " globalization " + ret + ", " +
                            ret.getMayBeDuplicate());
                }
                oldTemp = temp;
                GlobalizationRecord value =
                        (GlobalizationRecord) super.get(temp, tag);
                if (!value.getMayBeDuplicate()) {
                    //add the information regarding globlization information here
                    value.addKey(ret, 0);
                    int removeRefCount = ret.getReferredCount();
                    //add the deleted reference count
                    value.setReferredCount(value.getReferredCount() +
                            removeRefCount);
                    return ret.getObject();
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        if (ret != null) {
            return ((GlobalizationRecord) ret).getObject();
        } else {
            return null;
        }
    }

    /**
     * find constant pools for a given Phase  
     * return hashMap of with key as poolId 
     * 
     * @param phase
     * @return
     */
    public HashMap getPhaseValues(Phase phase) {
        TreeSet ids = super.getPoolIds();
        Iterator it = ids.iterator();
        HashMap map = new HashMap();
        while (it.hasNext()) {
            int poolId = (Integer) it.next();
            if (phase.isPhaseTag(poolId)) {
                map.put(poolId, super.getPool(poolId));
            }
        }
        return map;
    }

    public SortedSet getPhaseKeys(Phase phase) {
        TreeSet ids = super.getPoolIds();
        Iterator it = ids.iterator();
        Object startKey = null;
        Object endKey = null;
        while (it.hasNext()) {
            int poolId = (Integer) it.next();
            if (phase.isPhaseTag(poolId) && startKey == null) { //enter the phase range
                startKey = poolId;
            } else if (!phase.isPhaseTag(poolId) && startKey != null) {//exiting the phase
                endKey = poolId;
                break;
            }
        }
        if (startKey != null && endKey != null) {
            return ids.subSet(startKey, endKey);
        } else if (startKey != null && endKey == null) {
            return ids.tailSet(startKey);
        } else {
            return null;
        }
    }

    public int getCountOfOriginalCPEntries() {
        return totalEntriesCountOfOriginalConstantPool;
    }

    public static int getConstantValueTag(String className, int oldIndex) {
        Object obj = constantsTags.get(constantTagsKey(className, oldIndex));
        if (obj == null) {
            return -1;
        }
        return (Integer) obj;
    }

    private static String constantTagsKey(String classNameAndPath, int cpIndex) {
        return classNameAndPath + "," + cpIndex;
    }

    private static void putConstantTags(String key, int poolId) {
        if (poolId != TagValues.CONSTANT_Float && poolId != TagValues.CONSTANT_Integer &&
                poolId != TagValues.CONSTANT_String && poolId != TagValues.CONSTANT_Double &&
                poolId != TagValues.CONSTANT_Long && poolId != TagValues.CONSTANT_Class) {
            return;
        }
        constantsTags.put(key, poolId);
    }

    private int addSpecial(BaseObject obj, int poolId) {
        int newIndex = 0;
        try {
            newIndex = getCurrentSize(poolId);
            //in this case old index is equalent to new index
            GlobalizationRecord rec = new GlobalizationRecord(obj, newIndex,
                    true, ClassFile.currentClassToWorkOn, poolId);
            return super.add(rec, poolId);
        } catch (Exception d) {
            d.printStackTrace();
        }
        return newIndex;
    }

    @Override
    public int add(Object obj, int poolId) {
        if (isGlobalizationStarted) {
            return addSpecial((BaseObject) obj, poolId);
        }
        int index = 0;
        try {
            ClassFile cfile = ClassFile.currentClassToWorkOn;
            String name = cfile.getSourceFileNameWithPath();
            //find out if the current Class is same as old class
            if (currentClassName == null || !currentClassName.equals(name)) {
                currentClassName = name;
                cpCount = 1; //we start from 1 instead of zero
            } else {
                cpCount++;
            }
            //as constants are basically used as single pool by ldc (and other places) 
            putConstantTags(constantTagsKey(name, cpCount), poolId);
            GlobalizationRecord rec = new GlobalizationRecord((BaseObject) obj,
                    cpCount, true, cfile, poolId);
            ControllerBase pool = getPool(poolId);
            if (poolId == TagValues.CONSTANT_Class && pool.getCurrentSize() == 0) {
                super.add(new GlobalizationRecord(new EmptyInfo(), 0, false,
                        cfile, poolId), poolId);
            }
            index = super.add(rec, poolId);
            if (obj instanceof LongInfo || obj instanceof DoubleInfo) {
                cpCount++; //cout them twice
            }
            //statsCalc(obj, poolId);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return index;
    }

    public ClassFile getClass(int index, int poolId) {
        GlobalizationRecord rec = (GlobalizationRecord) getPool(poolId).get(index);
        return rec.getClass(0);
    }

    @Override
    public int getCurrentSize() {
        int currentSize = super.getCurrentSize();
        ControllerBase pool = getPool(TagValues.CONSTANT_Class);
        if (pool.getCurrentSize() != 0) {
            currentSize--; //as one object at the top is empty object and should not be counted.
        }
        pool = getPool(TagValues.CONSTANT_Double);
        currentSize += pool.getCurrentSize(); //count double twice
        pool = getPool(TagValues.CONSTANT_Long);
        currentSize += pool.getCurrentSize();
        return currentSize;
    }

    /**
     * execute
     * Step 5 identifying phase is implemented using this function. 
     * Hence it identify unique elements. Furthermore, 
     * it calls removeallduplicates and also sort functions. 
     * @throws Exception
     */
    public void execute() throws Exception {
        isGlobalizationStarted = true;
        //do not print the tags
        this.totalEntriesCountOfOriginalConstantPool = GlobalConstantPool.getInstanceOf().getCurrentSize();
        cacheTotalSizeInBytes();
        //count the references
        //CPReferrenceCounter.getInstanceOf().markCPReferences(false);

        markClassInfoWithInterfaces();
        InfoBase.printTag(false);
        HashMap contMap = getPhaseValues(this);
        Set keySet = contMap.keySet();
        Iterator keyIt = keySet.iterator();

        GlobalizationRecord value = null;
        int cpSize = 0;
        BaseObject cpObj = null;
        logHolder.addLog("Creating unique values....");
        //get all the class files

        while (keyIt.hasNext()) {
            int poolId = (Integer) keyIt.next();
            ControllerBase base = getPool(poolId);
            cpSize = base.getCurrentSize();
            for (int index = 0; index < cpSize; index++) {
                value = (GlobalizationRecord) base.get(index);
                cpObj = value.getObject();
                if (cpObj instanceof EmptyInfo) {
                    continue; //they are added in the index after long and double.
                }
                if (isPhaseElm((InfoBase) cpObj)) {
                    if (uniques.get(value) == null) {
                        value.setMayBeDuplicate(false); //it is not a duplicate
                        uniques.put(value, "");
                    } else {
                        value.setMayBeDuplicate(true);
                    }
                }
                //Miscellaneous.println(totalEntriesCountOfOriginalConstantPool);
            }
        }
        uniques.clear();
        logHolder.addLog("\n\nCreated uniques....");
        removeAllDuplicates(this);
        logHolder.addLog("Duplicates removed....");
        logHolder.addLog("Math-infos trimed....");

        trimMathInfo();

        sort(PhaseValuesComparator.getInstanceOf(), this);
        logHolder.addLog("Sorted ....");

        createMathMap();
        isExecuted = true;
        logHolder.addLog("Done with Phase One....");
    }

    private void cacheTotalSizeInBytes() {
        TreeSet poolIdsLocal = getPoolIds();
        Iterator<Integer> poolIdIt = poolIdsLocal.iterator();
        int poolId = 0;
        while (poolIdIt.hasNext()) {
            poolId = poolIdIt.next();
            ControllerBase poolCont = getPool(poolId);
            for (int loop = 0; loop < poolCont.getCurrentSize(); loop++) {
                GlobalizationRecord gRec = (GlobalizationRecord) poolCont.get(loop);
                BaseObject obj = gRec.getObject();
                if (obj instanceof EmptyInfo) {
                    continue;
                }
                if (obj instanceof LFBaseObject) {
                    LFBaseObject lfBaseObj = (LFBaseObject) obj;
                    this.totalOriginalConstantPoolSizeInBytes +=
                            SizeCalculator.getObjectSuperSize(lfBaseObj);
                } else {
                    this.totalOriginalConstantPoolSizeInBytes +=
                            SizeCalculator.getObjectSize(obj);

                }
            }
        }
    }

    public long totalOriginalSizeOfCPInBytes() {
        return totalOriginalConstantPoolSizeInBytes;
    }

    public void sort(java.util.Comparator comp, Phase phase) {

        try {
            SortedSet phasePoolIds = getPhaseKeys(phase);
            Iterator it = phasePoolIds.iterator();
            int poolId = -1;
            while (it.hasNext()) {
                poolId = (Integer) it.next();
                if (poolId == TagValues.CONSTANT_Class) {
                    super.sort(comp, 1, getPool(poolId).getCurrentSize() - 1,
                            poolId);
                } else {
                    super.sort(comp, poolId);
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    public void sort(java.util.Comparator comp, int startIndex, int endIndex) {
        throw new UnsupportedOperationException();
    }

    public void removeAllDuplicates(Phase phase) throws Exception {
        cacheForGet.clear();
        HashMap contMap = getPhaseValues(phase);
        Set keySet = contMap.keySet();
        Iterator keyIt = keySet.iterator();
        GlobalizationRecord rec = null;
        while (keyIt.hasNext()) {
            int poolId = (Integer) keyIt.next();
            ControllerBase base = getPool(poolId);
            //Miscellaneous.println("Before removing duplicates = "+base.getCurrentSize());
            for (int index = 0; index < base.getCurrentSize(); index++) {
                rec = (GlobalizationRecord) base.get(index);
                if (rec.getMayBeDuplicate()) {
                    if (remove(index, poolId) != null) {
                        index--;
                    }

                }
            }
            //Miscellaneous.println("After removing duplicates = "+base.getCurrentSize());
        }
        setGroupReferenceCount(phase);
    }

    /**
     * return reference count for RegularInfoBase and MathInfoBase object 
     * @param groupId
     * @return
     */
    public int getElmGroupReferenceCount(int index, int tag) {
        GlobalizationRecord gRec = (GlobalizationRecord) super.get(index, tag);
        return gRec.getReferredCount();
    }

    /**
     * return average reference count for RegularInfoBase and MathInfoBase object
     * of a given group 
     * @param groupId id of a group
     * @param objectClass
     * @return
     */
    public static int getGroupReferenceCount(int groupId, Class objectClass) {
        String sGroupId = groupId + objectClass.getName();
        Object ret = groupsReferCount.get(sGroupId);
        if (ret == null) {
            return 0;
        }
        GroupRefRecord refRec = (GroupRefRecord) ret;
        int groupRefCount = refRec.refCount;
        if (groupId == STATIC_FIELD_GROUP_ID) {
            //so that statics are always at the last.
            groupRefCount = Integer.MIN_VALUE;
        }
        int average = groupRefCount / (refRec.numberOfElements == 0 ? 1 : refRec.numberOfElements);
        return average;
    }

    public void setGroupReferenceCount(int poolId) {
        try {
            GlobalizationRecord rec = null;
            //key is groupId and value is sum of reference count
            String groupId = "";
            int refCount = 0;
            int groupElementsCount = 0;
            ControllerBase base = getPool(poolId);
            int size = base.getCurrentSize();
            //Miscellaneous.println("Before removing duplicates = "+base.getCurrentSize());
            for (int index = 0; index < size; index++) {
                rec = (GlobalizationRecord) base.get(index);
                refCount = 0;
                groupElementsCount = 0;
                //get the group id
                if (rec.getObject() instanceof RegularInfoBase) {
                    RegularInfoBase rBase = (RegularInfoBase) rec.getObject();
                    groupId = rBase.getIndex().intValueUnsigned() + rBase.getClass().getName();
                    if (poolId == TagValues.CONSTANT_Fieldref) {
                        if (((FieldRefInfo) rBase).isStatic) {
                            groupId = -1 + rBase.getClass().getName();
                        }
                    }

                } else if (rec.getObject() instanceof MathInfoBase) {
                    MathInfoBase rBase = (MathInfoBase) rec.getObject();
                    groupId = rBase.size() + rBase.getClass().getName();
                }
                //set right ref Count
                Object value = groupsReferCount.get(groupId);
                if (value != null) {
                    GroupRefRecord refRec = (GroupRefRecord) value;
                    groupElementsCount = refRec.numberOfElements;
                    refCount = refRec.refCount;
                }
                refCount += rec.getReferredCount();
                groupElementsCount++;
                groupsReferCount.put(groupId, new GroupRefRecord(refCount,
                        groupElementsCount));
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private class GroupRefRecord {

        int refCount = 0;
        int numberOfElements = 0;

        public GroupRefRecord(int refCount, int numberOfElements) {
            this.refCount = refCount;
            this.numberOfElements = numberOfElements;
        }
    }

    private void setGroupReferenceCount(Phase phase) {
        SortedSet set = getPhaseKeys(phase);
        Iterator it = set.iterator();
        while (it.hasNext()) {
            int poolId = (Integer) it.next();
            if (poolId == TagValues.CONSTANT_Utf8 ||
                    poolId == TagValues.CONSTANT_NameAndType) {
                continue;
            }
            setGroupReferenceCount(poolId);
        }

    }

    public int getGlobalIndex(Un oldIndex, ClassFile classFile, int tag) throws Exception {
        return getGlobalIndex(oldIndex.intValueUnsigned(), classFile, tag);

    }

    /**
     * set the duplicate flag of an object at index of a pool with given poolId
     * @param index
     * @param poolId
     * @param mayBeDuplicate
     */
    public void setMayBeDuplicate(int index, int poolId,
            boolean mayBeDuplicate) {
        GlobalizationRecord rec = (GlobalizationRecord) super.get(index, poolId);
        rec.setMayBeDuplicate(mayBeDuplicate);
    }

    public boolean getMayBeDuplicate(int index, int poolId) {
        GlobalizationRecord rec = (GlobalizationRecord) super.get(index, poolId);
        return rec.getMayBeDuplicate();
    }

    public Un getGlobalIndexUn(Un oldIndex, ClassFile classFile, int tag) throws Exception {
        return factory.createUn(getGlobalIndex(oldIndex, classFile, tag)).trim(2);
    }

    public Un getGlobalIndexUn(int oldIndex, ClassFile classFile, int tag) throws Exception {
        return factory.createUn(getGlobalIndex(oldIndex, classFile, tag)).trim(2);
    }

    /**
     * Given oldIndex (original-Index) it returns back the new global index. 
     * You need classFile and tag as different classfiles could be using same oldIndex
     * 
     * @param oldIndex
     * @param classFile
     * @param tag
     * @return
     * @throws java.lang.Exception
     */
    public int getGlobalIndex(int oldIndex, ClassFile classFile, int tag) throws Exception {
        GlobalizationRecord value = null;
        ControllerBase base = getPool(tag);
        for (int loop = 0; loop < base.getCurrentSize(); loop++) {
            value = (GlobalizationRecord) super.get(loop, tag);
            /*if (tag == TagValues.CONSTANT_Class)
            Miscellaneous.println("In function getGlobalIndex "+
            value.getKey(0)+ ", "+value.getObject());
             */
            if (value.contains(oldIndex, classFile, tag)) {
                return loop;
            }
        }
        throw new Exception("PhaseOne: Invalid constant pool Index =" +
                oldIndex + ", className=" + classFile.getSourceFileNameWithPath());
    }

    @Override
    public String toString() {
        String ret = "";
        try {
            ret = writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    @Override
    public void setMaxSize(int maxSize) {
        //do nothing
    }

    @Override
    public int getMaxSize() {
        throw new UnsupportedOperationException("get Max Size is not implemented");
        //return getCurrentSize();
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        TreeSet ids = super.getPoolIds();
        Iterator it = ids.iterator();
        String ret = "size=" +
                factory.createUn(getCurrentSize()).trim(2).
                writeSelected(buff) + "\n";
        while (it.hasNext()) {
            int poolId = (Integer) it.next();
            ControllerBase base = super.getPool(poolId);
            for (int loop = 0; loop < base.getCurrentSize(); loop++) {
                ret = ret + "\n(" + loop +
                        ", " + poolId + ")" + ((BaseObject) base.get(loop)).writeSelected(buff);
                if (!ConfigPropertyReader.getInstanceOf().isGenerateTukTextForDebuging()) {
                    ret = "";
                }
            }
        }
        return ret;
    }

    private int trimedSize(byte[] input, boolean isFloatDouble) throws
            Exception {
        if (isFloatDouble) {
            return input.length; //no trimming in that case. todo later might change.
        }

        int size = input.length;
        int index = input.length;
        for (int loop = 0; loop <
                input.length; loop++) {
            if (!isFloatDouble) {
                index = loop;
            } else {
                index--;
            }

            if (input[index] == 0) {
                size--;
            } else {
                break;
            }

        }
        //hence now allowed sizes are 0, 1, 2, 4, 8
        if ((size % 2 != 0) && (size != 1)) {
            size++;
        }

        return size;
    }

    private byte[] trimedByteArray(byte[] input, int size,
            boolean isFloatDouble) {
        if (size == 0) {
            return null;
        }

        if (size == input.length) {
            return input;
        }

        byte ret[] = new byte[size];
        int inputSize = input.length;
        for (int loop = 0; loop <
                size; loop++) {
            if (!isFloatDouble) {
                ret[size - loop - 1] = input[inputSize - loop - 1];
            } else {
                ret[loop] = input[loop];
            }

        }
        return ret;
    }

    private void trimMathInfo(int tag) throws Exception {
        ControllerBase pool = getPool(tag);

        int size = pool.getCurrentSize();
        GlobalizationRecord value = null;
        MathInfoBase mBase = null;
        byte[] upper = null;
        byte[] lower = null;
        int sizeAfterTrim = 0;

        for (int loop = 0; loop < size; loop++) {
            value = (GlobalizationRecord) pool.get(loop);
            if (value.getObject() instanceof EmptyInfo) {
                continue;
            }
            sizeAfterTrim = 0;
            mBase = (MathInfoBase) value.getObject();
            boolean isLongDouble = (mBase instanceof FloatInfo) ||
                    (mBase instanceof DoubleInfo);

            upper = mBase.getUpperBytes().getData();

            sizeAfterTrim = trimedSize(upper, isLongDouble);

            if (!(mBase instanceof LongInfo) && sizeAfterTrim == 0) {
                sizeAfterTrim = 1; //to make sure we never end up with a empty constant
            }

            if (mBase instanceof LongInfo && sizeAfterTrim != 0 && sizeAfterTrim % 2 != 0) {
                sizeAfterTrim += 1;
            }

            mBase.setUpperBytes(factory.createUn(trimedByteArray(upper,
                    sizeAfterTrim, isLongDouble)));

            if (mBase instanceof LongInfo) {
                if (sizeAfterTrim == 0) {
                    LongInfo linfo = (LongInfo) mBase;
                    lower = linfo.getLowerBytes().getData();
                    sizeAfterTrim = trimedSize(lower, isLongDouble);
                    if (sizeAfterTrim == 0 || (sizeAfterTrim % 2 != 0 &&
                            sizeAfterTrim != 1)) {
                        sizeAfterTrim += 1; //should have a zero in that case.
                    }

                    linfo.setLowerBytes(factory.createUn(trimedByteArray(lower,
                            sizeAfterTrim, isLongDouble)));
                } else {
                    sizeAfterTrim = sizeAfterTrim + 4;
                }

            }
        }

    }

    private void createMathMap() {
        createMathMap(TagValues.CONSTANT_Integer);
        createMathMap(TagValues.CONSTANT_Float);
        createMathMap(TagValues.CONSTANT_Long);
        createMathMap(TagValues.CONSTANT_Double);
    }

    /**
     * create a tables which should be writen in the header to find from where 
     * a specific size of element is started
     * assumption: Math values are already sorted w.r.t. their sizes
     * @param tag
     */
    private void createMathMap(int tag) {
        //get all the values of tag pool
        ControllerBase base = getPool(tag);
        int poolSize = base.getCurrentSize();
        int valueSize = -1;
        int groupValuesCount = 0;
        MathInfoBase mathBase = null;
        MathInfoSizeAndNumber mathSizeNo = null;
        for (int index = 0; index < poolSize; index++) {
            GlobalizationRecord grec = (GlobalizationRecord) base.get(index);
            if (grec.getObject() instanceof EmptyInfo) {
                continue;
            }
            mathBase = (MathInfoBase) grec.getObject();
            int newSize = mathBase.size();
            if (newSize != valueSize) {
                valueSize = newSize;
                if (mathSizeNo != null) {
                    mathSizeNo.setNumberOfElements(groupValuesCount);
                }
                groupValuesCount = 0;
                mathSizeNo = new MathInfoSizeAndNumber(tag, newSize, index); //assumption: Values are already sorted w.r.t. their sizes
                mathMap.put(new MathKey(tag, newSize, mathSizeNo.getStartIndex()),
                        mathSizeNo);
            }
            groupValuesCount++;
        }
        if (mathSizeNo != null) {
            mathSizeNo.setNumberOfElements(groupValuesCount);
        }

    }

    private void trimMathInfo() throws Exception {
        trimMathInfo(TagValues.CONSTANT_Float);
        trimMathInfo(TagValues.CONSTANT_Integer);
        trimMathInfo(TagValues.CONSTANT_Double);
        trimMathInfo(TagValues.CONSTANT_Long);
    }

    public int getMathNumberOfElements(int tag, int size) {
        MathInfoSizeAndNumber mathSizeNo = (MathInfoSizeAndNumber) mathMap.get(new MathKey(tag, size));
        if (mathSizeNo == null) {
            return 0;
        }

        return mathSizeNo.getNumberOfElements();
    }

    public int getMathStartIndex(int tag, int size) {
        MathInfoSizeAndNumber mathSizeNo = (MathInfoSizeAndNumber) mathMap.get(new MathKey(tag, size));
        if (mathSizeNo == null) {
            return 0;
        }

        return mathSizeNo.getStartIndex();
    }

    public Vector getSortedMathSizeNumber() {
        Vector values = new Vector(mathMap.values());
        //QuickSort.sort(values, new MathInfoSizeNoComparator());
        return values;
    }
}
