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
package takatuka.optimizer.VSS.logic.preCodeTravers;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.VSS.logic.StartMeVSS;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.BHInstruction;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * 
 * It saves information for inserting casting instructions. The information includes:
 * 1: The method
 * 2: instruction id, of the method, before which a cast needed to be inserted
 * 3: cast from what
 * 4: and which stack location. (0 implies first location).
 *
 * The controller also has the method that actually insert those casting instructions.
 * 
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CastInstructionController {

    private static final CastInstructionController contr = new CastInstructionController();
    private HashMap<String, TreeSet<CastInfo>> map = new HashMap<String, TreeSet<CastInfo>>();

    /**
     * 
     */
    private CastInstructionController() {
    }

    /**
     *
     * @return
     */
    public static CastInstructionController getInstanceOf() {
        return contr;
    }

    /**
     * 
     * @return the total number of casting requests added
     */
    public int getSize() {
        Collection<TreeSet<CastInfo>> values = map.values();
        Iterator<TreeSet<CastInfo>> castInfoTreeIt = values.iterator();
        int size = 0;
        while (castInfoTreeIt.hasNext()) {
            TreeSet<CastInfo> castInfoTree = castInfoTreeIt.next();
            Iterator<CastInfo> castInfoIt = castInfoTree.iterator();
            while (castInfoIt.hasNext()) {
                CastInfo cInfo = castInfoIt.next();
                size += 3;
            }
        }
        return size;
    }

    private void checkForError(CastInfo newCastInfo) {
        TreeSet set = map.get(ChangeCodeForReducedSizedLV.createKey(newCastInfo.getMethod()));
        Iterator<CastInfo> it = set.iterator();
        while (it.hasNext()) {
            CastInfo castInfo = it.next();
            if (castInfo.equals(newCastInfo) && newCastInfo.getStackCurrentType() != castInfo.getStackCurrentType()) {
                Miscellaneous.printlnErr("tried to add a duplicated cast for a same instruction. ");
                Miscellaneous.printlnErr(Oracle.getInstanceOf().getMethodOrFieldString(newCastInfo.getMethod())
                        + ", " + newCastInfo+"\n"+castInfo);
                Miscellaneous.exit();
            }
        }
    }


    /**
     * 
     * @param method
     * @param instr
     * @param stackCurrentType
     * @param typeToConvertInto
     * @param stackLocation
     * @param forMethodDesc
     */
    public void add(MethodInfo method, BHInstruction instr, int stackCurrentType,
            int typeToConvertInto, int stackLocation, boolean forMethodDesc) {
        if (StartMeVSS.doneWithVSS) {
            return;
        }
        if (stackLocation < 0) {
            Miscellaneous.printlnErr("error # 32992");
            Miscellaneous.exit();
        }
        TreeSet set = map.get(ChangeCodeForReducedSizedLV.createKey(method));
        if (set == null) {
            set = new TreeSet();
            map.put(ChangeCodeForReducedSizedLV.createKey(method), set);
        }
        CastInfo newCastInfo = new CastInfo(instr, stackCurrentType,
                typeToConvertInto, stackLocation, method, forMethodDesc);
        checkForError(newCastInfo);
        set.add(newCastInfo);
        
        //Miscellaneous.println("123 " + toString());
    }

    public TreeSet<CastInfo> get(MethodInfo method) {
        return map.get(ChangeCodeForReducedSizedLV.createKey(method));
    }

    @Override
    public String toString() {
        String ret = "";
//    private HashMap<MethodInfo, TreeSet<CastInfo>> map = new HashMap<MethodInfo, TreeSet<CastInfo>>();
        Collection<TreeSet<CastInfo>> values = map.values();
        Iterator<TreeSet<CastInfo>> castInfoTreeIt = values.iterator();
        while (castInfoTreeIt.hasNext()) {
            boolean firstTime = true;
            TreeSet<CastInfo> castInfoTree = castInfoTreeIt.next();
            Iterator<CastInfo> castInfoIt = castInfoTree.iterator();
            while (castInfoIt.hasNext()) {
                CastInfo cInfo = castInfoIt.next();
                if (firstTime) {
                    firstTime = false;
                    ret = ret + printMethod(cInfo.getMethod()) + "\n";
                }
                ret = ret + "\t" + cInfo + "\n";
            }
        }
        return ret;
    }

    private String printMethod(MethodInfo method) {
        Oracle oracle = Oracle.getInstanceOf();
        String ret = "\n";
        ret += method.getClassFile().getFullyQualifiedClassName() + "->";
        ret += oracle.methodOrFieldName(method, GlobalConstantPool.getInstanceOf()) + "---";
        ret += oracle.methodOrFieldDescription(method, GlobalConstantPool.getInstanceOf());
        return ret;

    }
}
