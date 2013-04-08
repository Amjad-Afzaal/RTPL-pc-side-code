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
package takatuka.offlineGC.DFA.logic;

import takatuka.offlineGC.DFA.dataObjs.fields.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.offlineGC.DFA.dataObjs.GCType;
import takatuka.offlineGC.DFA.dataObjs.TTReference;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class HeapUtil {

    private static final HeapUtil myObj = new HeapUtil();
    private static Vector methodCallingParameters = null;
    private static MethodInfo currentMethod = null;
    private static VerificationInstruction currentInstr = null;
    private static final GCHeapController gcHeapContr = GCHeapController.getInstanceOf();
    private static final GCHeapStatic gcHeapStatic = GCHeapStatic.getInstanceOf();
    private static final GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();

    /**
     * 
     */
    private HeapUtil() {
    }

    /**
     * 
     * @param methodCallingParameters_
     * @param currentMethod_
     * @param currentInstr_
     * @return
     */
    public static HeapUtil getInstanceOf(Vector methodCallingParameters_,
            MethodInfo currentMethod_,
            VerificationInstruction currentInstr_) {
        currentMethod = currentMethod_;
        methodCallingParameters = methodCallingParameters_;
        currentInstr = currentInstr_;
        return myObj;
    }

    protected void getFieldAndPushOnStack(int nATOrArrayIndex, GCType objectRef,
            boolean isArray, OperandStack stack) {
        try {
            HashSet<TTReference> fieldSet = getField(nATOrArrayIndex, objectRef);
            GCType fieldToPush = new GCType(true);
            fieldToPush.addReferences(fieldSet);
            stack.push(fieldToPush);
        } catch (Exception d) {
            throw new VerifyErrorExt(Messages.STACK_INVALID+", "+d);
        }
    }

    /**
     * This function is just like non-static heap function. However, there is one
     * major difference. That is there is a special heap with all the static fields unlike
     * non-static heaps per newId of objectref.
     *
     * @param value
     * @param instr
     * @throws java.lang.Exception
     */
    protected void saveInHeapStaticField(GCType value, VerificationInstruction instr) throws Exception {
        int cpIndex = instr.getOperandsData().intValueUnsigned();
        FieldRefInfo field = (FieldRefInfo) pOne.get(cpIndex, TagValues.CONSTANT_Fieldref);
        GCHeapStatic heap = GCHeapStatic.getInstanceOf();
        heap.putField(field, value, currentMethod,
                methodCallingParameters, (GCInstruction) currentInstr);
    }

    /**
     * In case objectRef has newId equals -1 then we are done Otherwise,
     * first get GCHeap from the CGHeapController with the given newId of objectRef.
     * then set the newId of the value in the GCHeap field
     *
     * @param value
     * @param objectRef
     * @param instr
     */
    protected void saveInHeapNonStaticField(GCType value, GCType objectRef,
            VerificationInstruction instr) throws Exception {
        int cpIndex = instr.getOperandsData().intValueUnsigned();
        FieldRefInfo field = (FieldRefInfo) pOne.get(cpIndex, TagValues.CONSTANT_Fieldref);
        int nATIndex = field.getNameAndTypeIndex().intValueUnsigned();
        saveInHeap(value, objectRef, nATIndex);
    }

    protected void saveInHeap(GCType value, GCType objectRef,
            int nATOrArrayIndex) throws Exception {
        HashSet<Integer> newIds = objectRef.getAllNewIds();
        Iterator<Integer> it = newIds.iterator();
        while (it.hasNext()) {
            int newId = it.next();
            if (newId < 0) {
                continue;
            }
            //we have a heap object stored per newId.
            GCHeapInterface heap = gcHeapContr.getGCHeap(newId);
            heap.putField(nATOrArrayIndex, value,
                    currentMethod, methodCallingParameters,
                    (GCInstruction) currentInstr);
            //Miscellaneous.println("------ - -- -- Heap = " + heap);
        }

    }

    private HashSet<TTReference> getFieldStatic(int nameAndTypeIndex) {
        HashSet<TTReference> ret = new HashSet<TTReference>();
        GCField field = gcHeapStatic.getField(nameAndTypeIndex, currentMethod,
                methodCallingParameters, (GCInstruction) currentInstr);
        //Todo may be add dummy reference for the field to be replace later if it is null.
        if (field != null) {
            ret.addAll(field.get());
        }
        return ret;
    }

    /**
     *
     *
     * @param nameAndTypeIndex
     * @param objectRef
     * @return
     * returns a field. returns null if the field was not stored before.
     */
    protected HashSet<TTReference> getField(int nameAndTypeIndex, GCType objectRef) {
        if (objectRef == null) {
            return getFieldStatic(nameAndTypeIndex);
        }
        HashSet<TTReference> retSet = new HashSet<TTReference>();
        HashSet ref = objectRef.getReferences();
        Iterator<TTReference> it = ref.iterator();

        while (it.hasNext()) {
            GCHeapInterface gcHeap = gcHeapContr.getGCHeap(it.next().getNewId());
            if (gcHeap != null) {
                GCField field = gcHeap.getField(nameAndTypeIndex, currentMethod,
                        methodCallingParameters, (GCInstruction) currentInstr);
                //Todo may be add dummy reference for the field to be replace later if it is null.
                if (field != null) {
                    retSet.addAll(field.get());
                } else {
                    retSet.add(TTReference.createNULLReference());
                }
            } else {
                retSet.add(TTReference.createNULLReference());
            }
        }
        return retSet;
    }
}
