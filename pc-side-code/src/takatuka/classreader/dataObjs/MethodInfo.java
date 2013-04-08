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

import java.util.Vector;
import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.io.*;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.BHInstruction;
import takatuka.verifier.logic.exception.VerifyErrorExt;

/**
 * <p>Title: </p>
 * <p>Description:
 * based on section 4.6 at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#1513
 * The structure has the following format:
method_info {
u2 access_flags;
u2 name_index;
u2 descriptor_index;
u2 attributes_count;
attribute_info attributes[attributes_count];
}
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class MethodInfo extends FieldInfo {

    public MethodInfo(ClassFile myClass) {
        super(myClass);
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        return "\tMethodInfo=[" + super.writeSelected(buff);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof MethodInfo)) {
            return false;
        }

        if (super.equals(obj) &&
                obj.getClass().getName().equals(this.getClass().getName())) {
            return true;
        }
        return false;
    }

    public Instruction findInstruction(int offSet) throws Exception {
        Vector<Instruction> instrs = getInstructions();
        return findInstruction(offSet, instrs);
    }

    /**
     * return instruction based on address
     * (todo: Should use binary search for faster execution)
     * @param offSet
     * @param instrs
     * @return
     */
    public static Instruction findInstruction(int offSet, Vector<Instruction> instrs) throws Exception {
        Instruction ret = null;

        for (int loop = 0; loop < instrs.size(); loop++) {
            ret = instrs.elementAt(loop);
            if (ret.getOffSet() == offSet) {
                return ret;
            }
        }
        throw new VerifyErrorExt("Invalid instruction (or Jump) address :" + offSet);
/*        throw new VerifyErrorExt(
                "Invalid instruction (or Jump) address :" + offSet);
 */
    }

    public static Instruction findPreviousInstr(long id, Vector<Instruction> instrs) {
        BHInstruction ret = null;
        BHInstruction prevInstr = null;
        for (int loop = 0; loop < instrs.size(); loop++) {
            ret = (BHInstruction) instrs.elementAt(loop);
            if (ret.getInstructionId() == id) {
                return prevInstr;
            }
            prevInstr = ret;
        }
        return prevInstr;
    }

    public static Instruction findInstruction(long id, Vector<Instruction> instrs) {
        BHInstruction ret = null;
        for (int loop = 0; loop < instrs.size(); loop++) {
            ret = (BHInstruction) instrs.elementAt(loop);
            if (ret.getInstructionId() == id) {
                return ret;
            }
        }
        throw new VerifyErrorExt(
                "Invalid instruction (or Jump) id :" + id);
    }

    /**
     * return instruction index based on instruction offset
     * offset are not recalculated but the offset cached value in an instruction are used.
     * @param offSet
     * @param instrs
     * @return
     */
    public static int findInstructionIndex(int offSet, Vector<Instruction> instrs) {
        Instruction ret = null;
        for (int loop = 0; loop < instrs.size(); loop++) {
            ret = instrs.elementAt(loop);
            if (ret.getOffSet() == offSet) {
                return loop;
            }
        }
        throw new VerifyErrorExt(
                "Invalid instruction (or Jump) address :" + offSet);
    }

    public Vector<Instruction> getInstructions() {
        CodeAtt codeAtt = getCodeAtt();
        if (codeAtt != null) {
            return codeAtt.getInstructions();
        } else {
            return new Vector();
        }
    }

    /**
     * Returns codeAtt in case it is there. Otherwise returns null.
     * @return CodeAtt
     */
    public CodeAtt getCodeAtt() {
        AttributeInfoController attCont = getAttributeController();
        CodeAtt codeAtt = null;
        for (int loop = 0; loop < attCont.getCurrentSize(); loop++) {
            if (attCont.get(loop) instanceof CodeAtt) {
                 codeAtt = (CodeAtt) attCont.get(loop);
                 codeAtt.setMethod(this);
            }
        }
        return codeAtt;
    }

    /**
     * Return ExceptionsAtt is case it is in the method. Otherwise return null.
     * @return
     */
    public ExceptionsAtt getExceptionAtt() {
        AttributeInfoController attCont = getAttributeController();
        for (int loop = 0; loop < attCont.getCurrentSize(); loop++) {
            if (attCont.get(loop) instanceof ExceptionsAtt) {
                return (ExceptionsAtt) attCont.get(loop);
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(super.hashCode()).append(this.getClass().getName()).toHashCode();
    }
}
