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
package takatuka.optimizer.bytecode.replacer.logic;

import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import java.util.*;
import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.logAndStats.*;

/**
 * 
 * Description:
 * <p>
 * Let say we have a JVM instraction A in any function of any class. 
 * We can convert it into B using this class.
 * Not that B will have same operand as A but different opcode.
 * We convert many instructions using this class. In short group of fload 
 * instructions are converted to equivalent iload instruction group. 
 * Example fload_1 --> iload_1.
 * Furthermore we also convert group of dload instruction to corresponding lload instructions 
 * and also store instructions.  
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class ReplaceInstrAWithB {

    private static final String OPNEM = "opcode-mnemonic.properties";
    private static final Properties opcodeMnemoicProperties =
            PropertyReader.getInstanceOf().loadProperties(OPNEM);
    private static HashSet<Integer> uniqueOpcode = new HashSet();
    private static final ReplaceInstrAWithB replacerMain = new ReplaceInstrAWithB();
    private static boolean isReplaced = false;

    public static final ReplaceInstrAWithB getInstanceOf() {
        return replacerMain;
    }

    static String getMnemonic(int opcode) {
        return opcodeMnemoicProperties.getProperty(opcode + "").trim().toUpperCase();
    }

    public static int getTotalInstructionReplaced() {
        return uniqueOpcode.size();
    }

    public static HashSet getInstructionsReplaced() {
        return uniqueOpcode;
    }

    private void convertInstAtoB(Instruction instA, int newOpCode, String mNemonic) {
        int opcode = instA.getOpCode();
        /*if (opcode == JavaInstructionsOpcodes.FLOAD ||
                opcode == JavaInstructionsOpcodes.DLOAD) {*/
            uniqueOpcode.add(instA.getOpCode());
        //}
        if (!isReplaced) {
            isReplaced = true;
            LogHolder.getInstanceOf().addLog("Replacing instruction A with B ....");
        }
        instA.setOpCode(newOpCode);
        instA.setMnemonic(mNemonic);
    }

    private int findNewOpCode(int opCode) {
        int newOpcode = -1;
        switch (opCode) { //27 instruction 204-27 = 177
            case JavaInstructionsOpcodes.FLOAD: //#1
                newOpcode = JavaInstructionsOpcodes.ILOAD;
                break;
            case JavaInstructionsOpcodes.FLOAD_0: //#2
                newOpcode = JavaInstructionsOpcodes.ILOAD_0;
                break;
            case JavaInstructionsOpcodes.FLOAD_1: //#3
                newOpcode = JavaInstructionsOpcodes.ILOAD_1;
                break;
            case JavaInstructionsOpcodes.FLOAD_2: //#4
                newOpcode = JavaInstructionsOpcodes.ILOAD_2;
                break;
            case JavaInstructionsOpcodes.FLOAD_3: //#5
                newOpcode = JavaInstructionsOpcodes.ILOAD_3;
                break;
            case JavaInstructionsOpcodes.DLOAD: //#6
                newOpcode = JavaInstructionsOpcodes.LLOAD;
                break;
            case JavaInstructionsOpcodes.DLOAD_0: //#7
                newOpcode = JavaInstructionsOpcodes.LLOAD_0;
                break;
            case JavaInstructionsOpcodes.DLOAD_1: //#8
                newOpcode = JavaInstructionsOpcodes.LLOAD_1;
                break;
            case JavaInstructionsOpcodes.DLOAD_2: //#9
                newOpcode = JavaInstructionsOpcodes.LLOAD_2;
                break;
            case JavaInstructionsOpcodes.DLOAD_3: //#10
                newOpcode = JavaInstructionsOpcodes.LLOAD_3;
                break;
            case JavaInstructionsOpcodes.DALOAD: //#11
                newOpcode = JavaInstructionsOpcodes.LALOAD;
                break;
            case JavaInstructionsOpcodes.FALOAD: //#12
                newOpcode = JavaInstructionsOpcodes.IALOAD;
                break;
            case JavaInstructionsOpcodes.FSTORE: //#13
                newOpcode = JavaInstructionsOpcodes.ISTORE;
                break;
            case JavaInstructionsOpcodes.FSTORE_0: //#14
                newOpcode = JavaInstructionsOpcodes.ISTORE_0;
                break;
            case JavaInstructionsOpcodes.FSTORE_1: //#15
                newOpcode = JavaInstructionsOpcodes.ISTORE_1;
                break;
            case JavaInstructionsOpcodes.FSTORE_2: //#16
                newOpcode = JavaInstructionsOpcodes.ISTORE_2;
                break;
            case JavaInstructionsOpcodes.FSTORE_3: //#17
                newOpcode = JavaInstructionsOpcodes.ISTORE_3;
                break;
            case JavaInstructionsOpcodes.DSTORE: //#18
                newOpcode = JavaInstructionsOpcodes.LSTORE;
                break;
            case JavaInstructionsOpcodes.DSTORE_0: //#19
                newOpcode = JavaInstructionsOpcodes.LSTORE_0;
                break;
            case JavaInstructionsOpcodes.DSTORE_1: //#20
                newOpcode = JavaInstructionsOpcodes.LSTORE_1;
                break;
            case JavaInstructionsOpcodes.DSTORE_2: //#21
                newOpcode = JavaInstructionsOpcodes.LSTORE_2;
                break;
            case JavaInstructionsOpcodes.DSTORE_3: //#22
                newOpcode = JavaInstructionsOpcodes.LSTORE_3;
                break;
            case JavaInstructionsOpcodes.DASTORE: //#23
                newOpcode = JavaInstructionsOpcodes.LASTORE;
                break;
            case JavaInstructionsOpcodes.FASTORE: //#24
                newOpcode = JavaInstructionsOpcodes.IASTORE;
                break;
            case JavaInstructionsOpcodes.DCONST_0: //#25
                newOpcode = JavaInstructionsOpcodes.LCONST_0;
                break;
            case JavaInstructionsOpcodes.FRETURN: //#26
                newOpcode = JavaInstructionsOpcodes.IRETURN;
                break;
            case JavaInstructionsOpcodes.DRETURN: //#27
                newOpcode = JavaInstructionsOpcodes.LRETURN;
                break;
        }
        return newOpcode;

    }

    public void convertInstruction(Instruction inst) {
        int newOpCode = findNewOpCode(inst.getOpCode());
        if (newOpCode != -1) {
            convertInstAtoB(inst, newOpCode, getMnemonic(newOpCode));
        }

    }

    public void convertInstructions(Vector instructions) {
        Instruction inst = null;
        for (int loop = 0; loop < instructions.size(); loop++) {
            inst = (Instruction) instructions.elementAt(loop);
            convertInstruction(inst);
        }
    }
}
