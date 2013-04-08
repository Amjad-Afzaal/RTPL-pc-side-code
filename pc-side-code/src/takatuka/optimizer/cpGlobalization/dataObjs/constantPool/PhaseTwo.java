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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * In this phase we globalize
 * ClassInfo, StringInfo, FieldInfo, MethodInfo, NameandTypeInfo
 *
 * Here we also remove ClassInfo and StringInfo objects.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PhaseTwo implements Phase {
    private GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    //private GlobalConstantPool GlobalConstantPool = GlobalConstantPool.getInstanceOf();
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    private HashMap uniques = new HashMap();
    private static final PhaseTwo phaseTwo = new PhaseTwo();
    private LogHolder logHolder = LogHolder.getInstanceOf();

    protected PhaseTwo() {
        super();
    }

    public static PhaseTwo getInstanceOf() throws Exception {
        return phaseTwo;
    }

    public void execute() throws Exception {

        logHolder.addLog("Phase two creating uniques ...");
        //Miscellaneous.println(loop+", "+cfCont.getCurrentSize());
        //PhaseThree.checkCPs();
        globalizeString_Class_NaT(TagValues.CONSTANT_Class);

        globalizeString_Class_NaT(TagValues.CONSTANT_String);

        globalizeString_Class_NaT(TagValues.CONSTANT_NameAndType);


        //remove classInfo and StringInfos. They are useless -:)
        //todo later removeAllClassStringInfo();

        logHolder.addLog("Removing All duplicates ...");
        removeAllDuplicates(this);
        uniques.clear();
        pOne.sort(PhaseValuesComparator.getInstanceOf(), this);
    //should not do sorting here. It create problems. Hence no uncommenting :)
    //pOne.sort(new PhaseValuesComparator());

    }
    
    public boolean isPhaseTag(int tag) {
        if (tag == TagValues.CONSTANT_String || tag == TagValues.CONSTANT_Class
            || tag == TagValues.CONSTANT_NameAndType) {
            return true;
        }
        return false;
    }
    
    public boolean isPhaseElm(InfoBase obj) {
        int tag = obj.getTag().intValueUnsigned();
        return isPhaseTag(tag);
        //ClassInfo, StringInfo, FieldInfo, MethodInfo, NameandTypeInfo
    }


    public void removeAllDuplicates(Phase phase) throws Exception {
        pOne.removeAllDuplicates(phase);
    }

    /* //todo do NOT remove following code. Useable in future....
        private void removeAllClassStringInfo() throws Exception {
             Miscellaneous.println("Removing all ClassInfo and StringInfo.... ");
             for (int loop = 0; loop < pOne.getCurrentSize(); loop ++) {
                 Object obj = pOne.get(loop);
                 if (obj instanceof ClassInfo || obj instanceof StringInfo) {
                     if (removeClassStringInfo(loop) != null) {
                         loop--; //because an element is deleted.
                     }
                 }
             }
             pOne.reCreateIndexes();
          }

          public Object removeClassStringInfo(int index) throws Exception {
     GlobalizationRecord ret = (GlobalizationRecord) pOne.removeSimple(index);
            try {
     int temp = ((RegularInfoBase)ret.getObject()).getIndex().intValueUnsigned();
     GlobalizationRecord value = (GlobalizationRecord) pOne.get(temp);
                value.add((Integer) ret.getOldIndex(0),ret.getClassName(0));
                return ret;
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
           return null; //should never come here. Unreachable.
      }
     */
    /**
     * The function will change local StringInfo and ClassInfo object to Global Objects
     * @param localVector Vector
     * @param globalMap HashMap
     * The map is either a unique values map for ClassInfo or StringInfo. In either case
     * the key of the map is UTF8String
     * @throws Exception
     */
    private void globalizeString_Class_NaT(int tag) throws
            Exception {
        RegularInfoBase rBase = null;
        int oldIndex;
        String key = null;
        
        String className = null;
        if (tag != TagValues.CONSTANT_String && tag != TagValues.CONSTANT_Class
            && tag != TagValues.CONSTANT_NameAndType) {
            throw new Exception("Invalid tag ");
        }
        
        for (int loop = 0; loop < pOne.getCurrentSize(tag); loop++) {
            if (tag == TagValues.CONSTANT_Class && loop == 0) {
                loop = 1;
            }
            if (pOne.get(loop, tag) instanceof EmptyInfo) {
                continue;
            }
            ClassFile.currentClassToWorkOn = pOne.getClass(loop, tag);
            rBase = (RegularInfoBase) pOne.get(loop, tag);
//          Miscellaneous.println("oldClass "+ClassFile.currentClassToWorkOn.getClassName());
            oldIndex = rBase.getIndex().intValueUnsigned();
            rBase.setIndex(pOne.getGlobalIndex(oldIndex,
                    ClassFile.currentClassToWorkOn, TagValues.CONSTANT_Utf8));
            key = KeyUtil.keymaker(rBase.getIndex(), null, tag);
            if (rBase instanceof NameAndTypeInfo) {
                NameAndTypeInfo nATInfo = (NameAndTypeInfo) rBase;
                nATInfo.setDescriptorIndex(pOne.getGlobalIndexUn(
                        nATInfo.getDescriptorIndex(), 
                        ClassFile.currentClassToWorkOn, 
                        TagValues.CONSTANT_Utf8));
                key = KeyUtil.keymaker(nATInfo.getIndex(),
                                       nATInfo.getDescriptorIndex(), tag);
            }
            if (uniques.get(key) == null) {
                uniques.put(key, "");
                pOne.setMayBeDuplicate(loop, tag, false);
            } else {
                // Miscellaneous.println("already have it " + key);
            }
            //           Miscellaneous.println("newClass "+rBase);
        }
    }


    public String toString() {
        /*        String ret = "";
                ret = ret +"ClassInfo:\t"+uniqueClasses.toString()+"\n\n";
                ret = ret +"StringInfo:\t"+uniqueString.toString()+"\n\n";
                ret = ret +"FieldInfo:\t"+uniqueFieldInfo.toString()+"\n\n";
                ret = ret +"MethodInfo:\t"+uniqueMethodInfo.toString()+"\n\n";
         ret = ret +"NameAndTypeInfo:\t"+uniqueNameAndType.toString()+"\n\n";
                return ret;*/
        return null;
    }


}
