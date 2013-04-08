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
package takatuka.verifier.dataObjs;

import takatuka.verifier.dataObjs.attribute.VerificationInstruction;
import takatuka.verifier.logic.factory.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class Frame {

    private LocalVariables localVar = null;
    private OperandStack stack = null;
    private byte code[] = null;

    public Frame(LocalVariables localVar, int maxStack, byte code[]) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        stack = frameFactory.createOperandStack(maxStack);
        this.localVar = (LocalVariables) localVar.clone();
        this.code = code;
    }

    public byte[] getCode() {
        return (byte[]) this.code.clone();
    }

    public LocalVariables getLocalVariables() {
        return localVar;
    }

    public OperandStack getOperandStack() {
        return stack;
    }

    public void updateFrame(VerificationInstruction instr) {
        stack = (OperandStack) instr.getOperandStack().clone();
        localVar = (LocalVariables) instr.getLocalVariables().clone();
    }

    public void updateFrame(OperandStack stack) {
        this.stack = stack;
    }
    
    @Override
    public boolean equals(Object obj) {
        throw new java.lang.UnsupportedOperationException();
    }

    @Override
    public int hashCode() {
        throw new java.lang.UnsupportedOperationException();
    }
}
