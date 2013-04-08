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
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.GlobalConstantPool;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.dataObjs.Type;
import takatuka.verifier.logic.factory.*;

/**
 *
 * Description:
 * <p>
 *
 * We already have 4 byte and 8 byte load and store.
 * We now support 4 additional instructions.
 * That is 1 and 2 byte load and 1 and 2 byte store instructions.
 * We name those instructions as follows.
 * ---- for 1 byte (x is the LV index) -----
 * STORE1 x, LOAD1 x
 * ---- for two bytes (x is the LV index)-----
 * STORE2 x, LOAD2 x
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ChangeCodeForReducedSizedLV {

    private static final ChangeCodeForReducedSizedLV myObj = new ChangeCodeForReducedSizedLV();
    private static final FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    private static final int INSTRUCTION_TYPE_STORE = 1;
    private static final int INSTRUCTION_TYPE_LOAD = 2;
    private static final int INSTRUCTION_TYPE_ASTORE = 3;
    private static final int INSTRUCTION_TYPE_ALOAD = 4;
    private static final int INSTRUCTION_TYPE_OTHERS = 5;
    private static final int INSTRUCTION_TYPE_IGNORE = 6;
    private static HashMap<String, TreeMap<Integer, Type>> cachedTypeMapPerMethod = new HashMap<String, TreeMap<Integer, Type>>();
    protected static boolean printDebugInfo = false;
    protected HashSet<Integer> integerMustIndexes = new HashSet<Integer>();
    private boolean usedLoadRef = false;
    private boolean usedStoreRef = false;
    private boolean usedLoadIntFloat = false;
    private boolean usedStoreIntFloat = false;
    private boolean usedLoadLongDouble = false;
    private boolean usedStoreLongDouble = false;
    private boolean usedLoadBooleanByte = false;
    private boolean usedStoreBooleanByte = false;
    private boolean usedLoadCharShort = false;
    private boolean usedStoreCharShort = false;

    /**
     * constructor is private
     */
    protected ChangeCodeForReducedSizedLV() {
    }

    /**
     *
     * @return
     */
    public static ChangeCodeForReducedSizedLV getInstanceOf() {
        return myObj;
    }

    protected static void debugMe(String str) {
        if (printDebugInfo) {
            Miscellaneous.println(str);
        }
    }

    /**
     * 
     * @param method
     * @return
     */
    public static TreeMap<Integer, Type> getTypeMapOfMethod(MethodInfo method) {
        return cachedTypeMapPerMethod.get(createKey(method));
    }

    public static String createKey(MethodInfo method) {
        Oracle oracle = Oracle.getInstanceOf();
        String name = oracle.methodOrFieldName(method, GlobalConstantPool.getInstanceOf());
        String desc = oracle.methodOrFieldDescription(method, GlobalConstantPool.getInstanceOf());
        String className = method.getClassFile().getFullyQualifiedClassName();
        return name + ", " + desc + ", " + className;
    }

    /**
     * To save the new indexes and corresponding types. It is used for casting
     * later on.
     *
     * @param oldToNewIndexMapForMethod
     * @param typeMapForMethod
     * @param method
     */
    protected void cachedMethodLVTypesMap(TreeMap<Integer, Integer> oldToNewIndexMapForMethod,
            TreeMap<Integer, Type> typeMapForMethod, MethodInfo method) {
        Set keySet = typeMapForMethod.keySet();
        Iterator<Integer> it = keySet.iterator();
        TreeMap<Integer, Type> map = new TreeMap<Integer, Type>();
        while (it.hasNext()) {
            Integer oldOffSet = it.next();
            Type type = typeMapForMethod.get(oldOffSet);
            if (!type.isReference() && type.getType() == Type.SPECIAL_TAIL) {
                continue;
            }
            Integer newOffSet = oldToNewIndexMapForMethod.get(oldOffSet);
            map.put(newOffSet, type);
        }
        cachedTypeMapPerMethod.put(createKey(method), map);
    }

    /**
     * 
     * @param method
     * @param typeAtOldIndexes
     * @throws Exception
     */
    protected void execute(MethodInfo method, Vector<Type> typeAtOldIndexes) throws Exception {
        Vector methodInstr = method.getInstructions();
        if (methodInstr.size() == 0) {
            return;
        }

        CodeAtt codeAtt = method.getCodeAtt();
        /**
         * Step 1:
         * Create a typeMap of the method.
         * The key are sorted index of local variables and values are
         * corresponding types.
         */
        TreeMap<Integer, Type> typeMapForMethod =
                createIndexToTypeMapForMethod(methodInstr, typeAtOldIndexes);
        populateMapWithMissingIndexes(codeAtt.getMaxLocals().intValueUnsigned(),
                typeMapForMethod, typeAtOldIndexes, method.getAccessFlags().isStatic());
        debugMe("type map for Method =" + typeMapForMethod);

        /**
         * Step 2:
         * The map tells what was the old index of a local variable before reduction
         * and what will be its new index after bytecode reduction.
         */
        TreeMap<Integer, Integer> oldToNewIndexMapForMethod =
                createOldIndexesToNewIndexesMap(typeMapForMethod);

        debugMe("old to new index Map =" + oldToNewIndexMapForMethod);
        /**
         * Step 3
         * It caches local variables types. So that in future when generating
         * casting instruction those types are not required to be recalculated.
         */
        cachedMethodLVTypesMap(oldToNewIndexMapForMethod, typeMapForMethod, method);

        /**
         * Step 4:
         * Finally the bytecode is changed. Instead of big load and store instruction
         * new reduce instruction are used. Similarly the indexes are set in the LV. 
         */
        changeBytecode(method, oldToNewIndexMapForMethod, typeAtOldIndexes);
        int newMaxLocals = calculateNewMaxLocal(oldToNewIndexMapForMethod, typeMapForMethod);
        codeAtt.setMaxLocals(factory.createUn(newMaxLocals).trim(2));
        codeAtt.updateCodeLength();
    }

    protected int calculateNewMaxLocal(
            TreeMap<Integer, Integer> oldToNewIndexMapForMethod,
            TreeMap<Integer, Type> typeMapForMethod) {
        if (oldToNewIndexMapForMethod.size() == 0) {
            return 0;
        }
        int highestOldIndex = oldToNewIndexMapForMethod.lastKey();
        int highestNewIndex = oldToNewIndexMapForMethod.get(highestOldIndex);
        Type type = typeMapForMethod.get(highestOldIndex);
        return incrementIndexDepOnType(highestNewIndex, type);
    }

    protected void changeBytecode(MethodInfo method,
            TreeMap<Integer, Integer> oldToNewIndexMapForMethod,
            Vector<Type> typeAtOldIndexes) throws Exception {
        Vector instrVec = method.getInstructions();
        Iterator<Instruction> instrIt = instrVec.iterator();
        debugMe(instrVec + "");
        while (instrIt.hasNext()) {
            Instruction instr = instrIt.next();
            int index = getIndex(instr);
            if (index == -1) {
                continue;
            }
            debugMe("original instr = " + instr);
            int newIndex = oldToNewIndexMapForMethod.get(index);
            Type type = getType(instr, typeAtOldIndexes, index);
            changeInstruction(instr, type, newIndex);
            debugMe("changed instr = " + instr);
        }
        updateUsedOpcodes();
        method.getCodeAtt().setInstructions(instrVec);
        debugMe("----- final ---- " + method.getInstructions());
    }

    private void updateUsedOpcodes() {
        if (usedLoadBooleanByte) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN);
        }
        if (usedLoadIntFloat) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LOAD_INT_FLOAT);
        }
        if (usedLoadCharShort) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LOAD_SHORT_CHAR);
        }
        if (usedLoadLongDouble) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LOAD_LONG_DOUBLE);
        }
        if (usedLoadRef) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.LOAD_REFERENCE);
        }
        if (usedStoreBooleanByte) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN);
        }
        if (usedStoreIntFloat) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.STORE_INT_FLOAT);
        }
        if (usedStoreCharShort) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.STORE_SHORT_CHAR);
        }
        if (usedStoreLongDouble) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.STORE_LONG_DOUBLE);
        }
        if (usedStoreRef) {
            BytecodeProcessor.removeFromFixUnUsedInstruction(JavaInstructionsOpcodes.STORE_REFERENCE);
        }
    }

    protected int incrementIndexDepOnType(int index, Type type) {
        index += Type.getBlocks((type.isReference() ? -1 : type.getType()),
                type.isReference());
        return index;
    }

    private void error() {
        Miscellaneous.printlnErr("error # 8723");
        Miscellaneous.exit();
    }

    protected static int getIntrOperationType(Instruction instr) {

        String mnemonic = instr.getMnemonic();
        /*
         * instruction class could be store=1, load=2, others=3,
         */
        int instrClass = INSTRUCTION_TYPE_OTHERS;
        if (mnemonic.startsWith("ASTORE")
                || instr.getOpCode() == JavaInstructionsOpcodes.STORE_REFERENCE) {
            instrClass = INSTRUCTION_TYPE_ASTORE;
        } else if (mnemonic.startsWith("ALOAD")
                || instr.getOpCode() == JavaInstructionsOpcodes.LOAD_REFERENCE) {
            instrClass = INSTRUCTION_TYPE_ALOAD;
        } else if (mnemonic.substring(1).startsWith("STORE")
                || instr.getOpCode() == JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN
                || instr.getOpCode() == JavaInstructionsOpcodes.STORE_INT_FLOAT
                || instr.getOpCode() == JavaInstructionsOpcodes.STORE_LONG_DOUBLE
                || instr.getOpCode() == JavaInstructionsOpcodes.STORE_SHORT_CHAR) {
            instrClass = INSTRUCTION_TYPE_STORE;
        } else if (mnemonic.substring(1).startsWith("LOAD")
                || instr.getOpCode() == JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN
                || instr.getOpCode() == JavaInstructionsOpcodes.LOAD_INT_FLOAT
                || instr.getOpCode() == JavaInstructionsOpcodes.LOAD_LONG_DOUBLE
                || instr.getOpCode() == JavaInstructionsOpcodes.LOAD_SHORT_CHAR) {
            instrClass = INSTRUCTION_TYPE_LOAD;
        } else if (mnemonic.equals("IINC") || mnemonic.equals("RET")) {
            instrClass = INSTRUCTION_TYPE_OTHERS;
        } else {
            instrClass = INSTRUCTION_TYPE_IGNORE;
        }
        return instrClass;
    }

    protected int getNonReferenceType(Type type) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        int nonRefType = type.isReference() ? Byte.MIN_VALUE : type.getType();
        if (nonRefType != Byte.MIN_VALUE) {
            if (frameFactory.createType(Type.BOOLEAN).getBlocks()
                    == frameFactory.createType(Type.SHORT).getBlocks()
                    && (nonRefType == Type.BOOLEAN || nonRefType == Type.BYTE)) {
                nonRefType = Type.SHORT;
            }
            if (frameFactory.createType(Type.SHORT).getBlocks()
                    == frameFactory.createType(Type.INTEGER).getBlocks()
                    && (nonRefType == Type.SHORT || nonRefType == Type.CHAR)) {
                nonRefType = Type.INTEGER;
            }
        }
        return nonRefType;

    }
    /*
     * change instruction only if its type is not integer/float, reference or double/long
     * otherwise, only change instruction index.
     * 
     * @param oldInstr
     * @param type
     * @param operand
     * @throws Exception
     */

    protected void changeInstruction(Instruction oldInstr, Type type, int newIndex) throws Exception {
        int instrClass = getIntrOperationType(oldInstr);
        int nonRefType = getNonReferenceType(type);
        if (instrClass == INSTRUCTION_TYPE_ALOAD) {
            changeInstruction(oldInstr, JavaInstructionsOpcodes.LOAD_REFERENCE,
                    newIndex, 1, true);
            usedLoadRef = true;
        } else if (instrClass == INSTRUCTION_TYPE_ASTORE) {
            changeInstruction(oldInstr, JavaInstructionsOpcodes.STORE_REFERENCE, newIndex, 1, true);
            usedStoreRef = true;
        } else if (instrClass == INSTRUCTION_TYPE_LOAD) {
            if (nonRefType == Type.INTEGER || nonRefType == Type.FLOAT) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.LOAD_INT_FLOAT, newIndex, 1, true);
                usedLoadIntFloat = true;
            } else if (nonRefType == Type.LONG || nonRefType == Type.DOUBLE) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.LOAD_LONG_DOUBLE, newIndex, 1, true);
                usedLoadLongDouble = true;
            } else if (nonRefType == Type.CHAR || nonRefType == Type.SHORT) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.LOAD_SHORT_CHAR, newIndex, 1, true);
                usedLoadCharShort = true;
            } else if (nonRefType == Type.BOOLEAN || nonRefType == Type.BYTE) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN, newIndex, 1, true);
                usedLoadBooleanByte = true;
            } else {
                Miscellaneous.printlnErr("Error # 71035" + oldInstr);
                new Exception().printStackTrace();
                Miscellaneous.exit();
            }
        } else if (instrClass == INSTRUCTION_TYPE_STORE) {
            if (nonRefType == Type.INTEGER || nonRefType == Type.FLOAT) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.STORE_INT_FLOAT, newIndex, 1, true);
                usedStoreIntFloat = true;
            } else if (nonRefType == Type.LONG || nonRefType == Type.DOUBLE) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.STORE_LONG_DOUBLE, newIndex, 1, true);
                usedStoreLongDouble = true;
            } else if (nonRefType == Type.CHAR || nonRefType == Type.SHORT) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.STORE_SHORT_CHAR, newIndex, 1, true);
                usedStoreCharShort = true;
            } else if (nonRefType == Type.BOOLEAN || nonRefType == Type.BYTE) {
                changeInstruction(oldInstr, JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN, newIndex, 1, true);
                usedStoreBooleanByte = true;
            } else {
                Miscellaneous.printlnErr("Error # 71036 " + oldInstr);
                new Exception().printStackTrace();
                Miscellaneous.exit();
            }
        } else if (instrClass == INSTRUCTION_TYPE_OTHERS) {
            changeInstruction(oldInstr, oldInstr.getOpCode(), newIndex, 1, true);
        } else {
            Miscellaneous.printlnErr("Error # 7103");
            Miscellaneous.exit();
        }
    }

    public static void changeInstruction(Instruction oldInstr,
            int newOpcode, int operand, int operandSize, boolean wantOperand) throws Exception {
        //change the instruction
        int oldOpcode = oldInstr.getOpCode();
        if (oldOpcode != newOpcode) { //do not need this if condition but no harm to have it too..
            oldInstr.setOpCode(newOpcode);
            oldInstr.setMnemonic(Instruction.getMnemonic(newOpcode));
        }
        if (wantOperand) {
            oldInstr.getOperandsData().replace(factory.createUn(operand).trim(operandSize), 0);
        } else {
            oldInstr.setOperandsData(factory.createUn());
        }
        //Miscellaneous.println(oldInstr.getOperandsData().size());
    }

    public static void changeInstruction(Instruction oldInstr,
            int newOpcode, Un operand) throws Exception {
        //change the instruction
        int oldOpcode = oldInstr.getOpCode();
        if (oldOpcode != newOpcode) { //do not need this if condition but no harm to have it too..
            oldInstr.setOpCode(newOpcode);
            oldInstr.setMnemonic(Instruction.getMnemonic(newOpcode));
        }
        if (operand.size() > 0) {
            oldInstr.getOperandsData().replace(operand, 0);
        } else {
            oldInstr.setOperandsData(factory.createUn());
        }
        //Miscellaneous.println(oldInstr.getOperandsData().size());
    }
    /**
     * for each old index and given it type, it decides new index.
     *
     * @param indexToTypeMap
     * @return
     */
    protected TreeMap<Integer, Integer> createOldIndexesToNewIndexesMap(
            TreeMap<Integer, Type> indexToTypeMap) {
        //keep the count of new indexes Used.
        int indexUsedCount = 0;
        TreeMap<Integer, Integer> oldToNewIndexMapForMethod = new TreeMap<Integer, Integer>();
        Set sortedIndexes = indexToTypeMap.keySet();
        Iterator<Integer> indexIt = sortedIndexes.iterator();
        while (indexIt.hasNext()) {
            int index = indexIt.next();
            Type type = indexToTypeMap.get(index);
            if (!type.isReference() && type.getType() == Type.SPECIAL_TAIL) {
                continue;
            }
            oldToNewIndexMapForMethod.put(index, indexUsedCount);
            debugMe("old-index =" + index + ", new Index=" + indexUsedCount + ", type=" + type);
            indexUsedCount = incrementIndexDepOnType(indexUsedCount, type);
        }

        return oldToNewIndexMapForMethod;
    }

    /**
     * it creates a map. The key are sorted index of local variables and values are
     * corresponding types.
     * We traverse the code if a load or store instruction found then we first type at that
     * index in typeAtOldIndex arraylist if found the save <index, type> in the TreeMap
     * If not found then use the bytecode to estimate the type and put <index, type> in the map
     *
     * @param instrVec
     * @param typeAtOldIndexes
     * @return
     * @throws Exception
     */
    protected TreeMap<Integer, Type> createIndexToTypeMapForMethod(Vector instrVec,
            Vector<Type> typeAtOldIndexes) throws Exception {
        TreeMap<Integer, Type> typeMapForMethod = new TreeMap<Integer, Type>();
        Iterator<Instruction> instrIt = instrVec.iterator();
        while (instrIt.hasNext()) {
            Instruction instr = instrIt.next();

            int index = getIndex(instr);
            if (index == -1) {
                continue;
            } else {
                debugMe("in index to type map " + instr);
                Type type = getType(instr, typeAtOldIndexes, index);
                if (typeMapForMethod.get(index) != null
                        && typeMapForMethod.get(index).getBlocks() <= type.getBlocks()) {
                    continue;
                }
                typeMapForMethod.put(index, type);
                debugMe(typeMapForMethod + "");

            }

        }
        return typeMapForMethod;
    }

    protected void populateMapWithMissingIndexes(int maxLVSize,
            TreeMap<Integer, Type> map,
            Vector<Type> typeAtOldIndexes,
            boolean isStaticMethod) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Type lastType = null;
        boolean nonUsedLocalVariables = false;
        //Miscellaneous.println(ReduceTheSizeOfLocalVariables.currentClassName + ", " +
        //      ReduceTheSizeOfLocalVariables.currentMethodName);

        for (int loop = 0; loop < maxLVSize; loop++) {
            Type newType = null;
            if (lastType != null
                    && lastType.isDoubleOrLong()) {
                newType = frameFactory.createType(Type.SPECIAL_TAIL);
            } else {
                //just to suppress some warnings.
                if (isStaticMethod || (!isStaticMethod && loop > 0)) {
                    if (!(isStaticMethod
                            && ReduceTheSizeOfLocalVariables.currentMethodName.equals("main")
                            && ReduceTheSizeOfLocalVariables.currentMethodDesc.equals("([Ljava/lang/String;)V")
                            && loop == 0)) {
                        nonUsedLocalVariables = true;
                    }
                }
                if (loop < typeAtOldIndexes.size()) {
                    newType = typeAtOldIndexes.get(loop);
                } else {
                    newType = frameFactory.createType(Type.INTEGER);
                }
            }
            Type currentType = map.get(loop);
            if (currentType != null && currentType.getBlocks() < newType.getBlocks()) {
                map.put(loop, newType);
            } else if (currentType == null) {
                map.put(loop, newType);
            }
            lastType = map.get(loop);
        }
        if (nonUsedLocalVariables) {
            LogHolder.getInstanceOf().addLog("**** RAM-WASTAGE-WARNING: non used local"
                    + " variables found in function name="
                    + ReduceTheSizeOfLocalVariables.currentMethodName
                    + " of class name=" + ReduceTheSizeOfLocalVariables.currentClassName);
        }

    }

    /**
     * 
     * @param instr
     * @return
     */
    protected static int getIndex(Instruction instr) throws Exception {
        String mnemonic = instr.getMnemonic();
        if (getIntrOperationType(instr) == INSTRUCTION_TYPE_IGNORE) {
            return -1; //not a local variable instruction
        }

        if (mnemonic.endsWith("_0")) {
            return 0;
        } else if (mnemonic.endsWith("_1")) {
            return 1;
        } else if (mnemonic.endsWith("_2")) {
            return 2;
        } else if (mnemonic.endsWith("_3")) {
            return 3;
        } else {
            Un cloneOperand = (Un) instr.getOperandsData().clone();
            return Un.cutBytes(1, cloneOperand).intValueUnsigned();
        }

    }

    /**
     *
     *
     * @param instr
     * @param typeAtOldIndexes
     * @param index
     * @return
     */
    protected Type getType(Instruction instr,
            Vector<Type> typeAtOldIndexes, int index) throws Exception {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        String mnemonic = instr.getMnemonic();
        int type = -1;
        boolean isReference = false;
        if (getIntrOperationType(instr) == INSTRUCTION_TYPE_IGNORE) {
            return null; //not a local variable instruction
        }

        if (index < typeAtOldIndexes.size()) {
            //get type based on parameters
            Type temp = typeAtOldIndexes.get(index);
            isReference = temp.isReference();
            type = temp.isReference() ? -1 : typeAtOldIndexes.get(index).getType();
            if (type == Type.SPECIAL_TAIL) {
                throw new Exception("Error # 89675");
            }

        } else { //get type from bytecode
            int opcode = instr.getOpCode();
            if (mnemonic.startsWith("ALOAD") || mnemonic.startsWith("ASTORE")
                    || opcode == JavaInstructionsOpcodes.LOAD_REFERENCE
                    || opcode == JavaInstructionsOpcodes.STORE_REFERENCE) {
                isReference = true;
            } else if (mnemonic.startsWith("DLOAD") || mnemonic.startsWith("DSTORE")
                    || opcode == JavaInstructionsOpcodes.LOAD_LONG_DOUBLE
                    || opcode == JavaInstructionsOpcodes.STORE_LONG_DOUBLE) {
                type = Type.DOUBLE;
            } else if (mnemonic.startsWith("LLOAD") || mnemonic.startsWith("LSTORE")
                    || opcode == JavaInstructionsOpcodes.LOAD_LONG_DOUBLE
                    || opcode == JavaInstructionsOpcodes.STORE_LONG_DOUBLE) {
                type = Type.LONG;
            } else if (mnemonic.startsWith("ILOAD") || mnemonic.startsWith("ISTORE")
                    || mnemonic.equals("RET") || mnemonic.startsWith("IINC")
                    || opcode == JavaInstructionsOpcodes.LOAD_INT_FLOAT
                    || opcode == JavaInstructionsOpcodes.STORE_INT_FLOAT) {
                type = Type.INTEGER;
            } else if (mnemonic.startsWith("FLOAD") || mnemonic.startsWith("FSTORE")
                    || opcode == JavaInstructionsOpcodes.LOAD_INT_FLOAT
                    || opcode == JavaInstructionsOpcodes.STORE_INT_FLOAT) {
                type = Type.FLOAT;
            } else if (opcode == JavaInstructionsOpcodes.LOAD_BYTE_BOOLEAN
                    || opcode == JavaInstructionsOpcodes.STORE_BYTE_BOOLEAN) {
                type = Type.BYTE;
            } else if (opcode == JavaInstructionsOpcodes.LOAD_SHORT_CHAR
                    || opcode == JavaInstructionsOpcodes.STORE_SHORT_CHAR) {
                type = Type.SHORT;
            } else {
                Miscellaneous.printlnErr("Error # 8978 " + instr);
                Miscellaneous.exit();
            }
            if (instr.getMnemonic().contains("IINC")
                    || instr.getMnemonic().contains("RET")) {
                int LVindex = -1;
                if (instr.getMnemonic().contains("IINC")) {
                    LVindex = Un.cutBytes(1, (Un) (instr.getOperandsData().clone())).
                            intValueUnsigned();
                } else {
                    LVindex = instr.getOperandsData().intValueUnsigned();
                }
                integerMustIndexes.add(LVindex);
            }
        }


        return frameFactory.createType(type, isReference, -1);
    }
}
