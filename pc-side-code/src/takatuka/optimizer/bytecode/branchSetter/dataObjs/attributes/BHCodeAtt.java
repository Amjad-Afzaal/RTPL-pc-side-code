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
package takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class BHCodeAtt extends CodeAtt {

    public static int codeAttCountDebug = 0;
    public static BHCodeAtt currentCodeAtt = null;
    private HashMap<Integer, IdInstruction> offsetAndIdInstructionMap = new HashMap();
    /**
     * Keep the map of all the sourceIds and their corresponding targetIds. 
     * It verify that if the instruction with sourceId and targetId still exist 
     * in the code.
     */
    public HashMap<Long, Vector<BranchTarget>> sourceIdVsTargetIds = new HashMap<Long, Vector<BranchTarget>>();

    public BHCodeAtt() throws Exception {
        
    }

    public BHCodeAtt(Un u2_attrNameIndex, Un u4_attributeLength, Un u2_maxStack,
            Un u2_maxLocals, Un u4_codeLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength, u2_maxStack,
                u2_maxLocals, u4_codeLength);

    }

    void putInOffSetIdMap(int offSet, IdInstruction instrId) {
        offsetAndIdInstructionMap.put(offSet, instrId);
    }

    IdInstruction getFromOffSetIdMap(int offSet) {
        return offsetAndIdInstructionMap.get(offSet);
    }
    
    @Override
    public void setCode(Un code) {
        currentCodeAtt = this;
        //Miscellaneous.println("  =" + (codeAttCountDebug++)+", "+
        //      ClassFile.currentClassToWorkOn.getFullyQualifiedClassName()); 

        super.setCode(code);

        //StartMeBranchHandler.printAllInstructionsDebug(this);
        currentCodeAtt = null;
        offsetAndIdInstructionMap.clear();
    }

    
    /**
     * The function verify if all the sources and targets can still be found in
     * the code. In case it cannot find a target or source then it exit after
     * giving an error.
     *
     * @throws java.lang.Exception
     */
    public void verifyCodeAttribute() throws Exception {
        Vector instrVec = getInstructions();
        if (true) {
            return; //Static code is moved to special class. Hence this implementation does not works there
        }
        //Miscellaneous.println("verifying instructions="+instrVec);
        int size = instrVec.size();
        BHInstruction inst = null;
        boolean error = false;
        Vector<BHInstruction> sources = new Vector();
        Vector<BranchTarget> targets = new Vector();
        BHInstruction src = null;
        for (int index = 0; index < size; index++) {
            inst = (BHInstruction) instrVec.elementAt(index);
            sources = inst.getSourcesInstructionsInMe();
            for (int srcIndex = 0; srcIndex < sources.size(); srcIndex++) {
                src = sources.elementAt(srcIndex);
                targets = sourceIdVsTargetIds.get(src.getInstructionId());
                if (targets == null || !targets.equals(src.getMyTargets())) {
                    error = true;
                    break;
                } else {
                    sourceIdVsTargetIds.remove(src.getInstructionId());
                }
            }
        }
        if (error) {
            throw new Exception("Error: illegal optimization as cannot find a branch target," +
                    " of sourceId= " + src.getInstructionId() + "class-File=");

        } else if (sourceIdVsTargetIds.size() != 0) {
            throw new Exception("Error: illegal optimization as cannot find a branch source =" +
                    sourceIdVsTargetIds.keySet());
        }
    }
}
