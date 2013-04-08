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
package takatuka.optimizer.VSS.logic;

import takatuka.optimizer.VSS.logic.preCodeTravers.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.VSS.dataObjs.*;
import takatuka.optimizer.VSS.logic.factory.*;
import takatuka.verifier.dataObjs.Frame;
import takatuka.verifier.dataObjs.FrameElement;
import takatuka.verifier.dataObjs.Type;
import takatuka.verifier.logic.StartMeVerifier;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ReductionBasedOnBCTravers extends ChangeCodeForReducedSizedLV {

    private static final ReductionBasedOnBCTravers myObj = new ReductionBasedOnBCTravers();

    /**
     * 
     * @return
     */
    public static ReductionBasedOnBCTravers getInstanceOf() {
        return myObj;
    }

    public static void stackSizesOfMethods() {
        Oracle oracle = Oracle.getInstanceOf();
        oracle.clearMethodCodeAttAndClassFileCache();
        Vector<CodeAttCache> vector = oracle.getAllCodeAtt();
        Miscellaneous.println("***********************************");
        Miscellaneous.println("size = " + vector.size());
        int totalSize = 0;
        for (int loop = 0; loop < vector.size(); loop++) {
            CodeAttCache cAtt = vector.elementAt(loop);
            MethodInfo method = cAtt.getMethodInfo();
            CodeAtt codeAtt = (CodeAtt) cAtt.getAttribute();
            totalSize += codeAtt.getMaxStack().intValueUnsigned();
            Miscellaneous.println("----- " + oracle.getMethodOrFieldString(method) + ", "
                    + codeAtt.getMaxStack().intValueUnsigned());
        }
        Miscellaneous.println("Total = " + totalSize);
        Miscellaneous.println("***********************************");
    }

    /**
     *
     */
    public void reduceSizeWITHCodeVirtualExecution() {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        try {
            if (!StartMeVerifier.shouldVerify) {
                return;
            }

            if (true) {
                //return;
            }
            
            Oracle oracle = Oracle.getInstanceOf();
            Vector<CodeAttCache> allCodeAttCache = oracle.getAllCodeAtt();
            Iterator<CodeAttCache> codeAttIt = allCodeAttCache.iterator();
            while (codeAttIt.hasNext()) {
                CodeAttCache codeAttCache = codeAttIt.next();
                MethodInfo method = codeAttCache.getMethodInfo();
                //ReplaceConstInstructions.getInstanceOf().execute(method);
                if (method == null || method.getInstructions().size() == 0) {
                    continue;
                }
                CodeAtt codeAtt = (CodeAtt) codeAttCache.getAttribute();
                int maxStack = codeAtt.getMaxStack().intValueUnsigned();
                ClassFile.currentClassToWorkOn = codeAttCache.getClassFile();
                SSFrameFactoryForSize.getInstanceOf().
                        createDataFlowAnalyzer().execute(method, null, null);
                integerMustIndexes.clear();
                if (oracle.getMethodOrFieldString(method).contains("checkInstanceOf")) {
                    //Miscellaneous.println("Stop 123 h3re");
                }
                changeCode(method);
                codeAtt.setMaxStack(factory.createUn(maxStack).trim(2));
            }
            
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void populateMapWithMissingIndexes(MethodInfo method, TreeMap<Integer, Type> typeMapForMethod) {
        TreeMap<Integer, Type> oldIndexesToTypeMap = ChangeCodeForReducedSizedLV.getTypeMapOfMethod(method);
        Iterator<Integer> oldIndexesIt = oldIndexesToTypeMap.keySet().iterator();
        while (oldIndexesIt.hasNext()) {
            int index = oldIndexesIt.next();
            if (typeMapForMethod.get(index) == null) {
                typeMapForMethod.put(index, oldIndexesToTypeMap.get(index));
            }
        }
    }

    private void changeCode(MethodInfo method) {

        String methodStr = Oracle.getInstanceOf().getMethodOrFieldString(method);
        try {
            Vector methodInstr = method.getInstructions();
            if (methodInstr.size() == 0 || method.getCodeAtt() == null
                    || method.getCodeAtt().getMaxLocals().intValueUnsigned() == 0) {
                return;
            }

            //Miscellaneous.println("------------- " + methodStr);
            if (methodStr.contains("takatuka.routing.java.dymo.DymoRouting.sendDataOrRoutingPacket")) {
                //Miscellaneous.println("Stop here 93");
                //printDebugInfo = true;
            }
            Frame lastMethodFrame = SSFrameFactoryForSize.getInstanceOf().
                    createDataFlowAnalyzer().getLastExecutedMethodFrame();
            FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
            Oracle oracle = Oracle.getInstanceOf();
            String methodDesc = oracle.methodOrFieldDescription(method,
                    GlobalConstantPool.getInstanceOf());
            boolean isStatic = method.getAccessFlags().isStatic();
            Vector<Type> typeAtOldIndexes = getMethodParametersTypes(methodDesc, isStatic);

            CodeAtt codeAtt = method.getCodeAtt();
            /**
             * Step 1:
             * Create a typeMap of the method.
             * The key are sorted index of local variables and values are
             * corresponding types.
             */
            TreeMap<Integer, Type> typeMapForMethod =
                    createIndexToTypeMapForMethod(methodInstr, typeAtOldIndexes);
            debugMe("type map for Method 1=" + typeMapForMethod);

            reduceIndexToTypeMap(typeMapForMethod, lastMethodFrame);
            debugMe("type map for Method 2=" + typeMapForMethod);

            populateMapWithMissingIndexes(method, typeMapForMethod);

            debugMe("type map for Method 3=" + typeMapForMethod);


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
            Vector newTypes = getChangedTypesAtOldIndexes(typeMapForMethod);
            debugMe("type for method 4=" + newTypes);
            changeBytecode(method, oldToNewIndexMapForMethod, newTypes);

            int newMaxLocals = calculateNewMaxLocal(oldToNewIndexMapForMethod, typeMapForMethod);
            int oldMaxLocals = codeAtt.getMaxLocals().intValueUnsigned();
            if (newMaxLocals > oldMaxLocals) {
                Miscellaneous.printlnErr("Error # 12312 with values =["
                        + newMaxLocals + ", " + oldMaxLocals + "]");
                Miscellaneous.exit();
            } else if (newMaxLocals < oldMaxLocals) {
                /*Miscellaneous.println("abc = " + oracle.getMethodString(method)
                + " with values =["
                + newMaxLocals + ", " + oldMaxLocals + "]");*/
            }
            codeAtt.setMaxLocals(factory.createUn(newMaxLocals).trim(2));
            codeAtt.updateCodeLength();

        } catch (Exception d) {
            Miscellaneous.printlnErr("Error at method " + methodStr);
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    private Vector<Type> getChangedTypesAtOldIndexes(TreeMap<Integer, Type> typeMapForMethod) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();

        Iterator<Integer> it = typeMapForMethod.keySet().iterator();
        Vector<Type> ret = new Vector();
        int count = -1;
        while (it.hasNext()) {
            int index = it.next();
            while (index != count + 1) {
                ret.addElement(frameFactory.createType(Type.SPECIAL_TAIL));
                count++;
            }
            Type type = typeMapForMethod.get(index);
            ret.addElement(type);
            count++;
        }
        return ret;
    }

    public void reduceIndexToTypeMap(TreeMap<Integer, Type> indexToTypeMap, Frame frame) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        SSLocalVariables localVar = (SSLocalVariables) frame.getLocalVariables();
        HashMap<Integer, Type> reducedMap = localVar.getMaxBlockPerElement();
        Iterator<Integer> keyIt = reducedMap.keySet().iterator();
        while (keyIt.hasNext()) {
            int key = keyIt.next();
            Type reducedType = reducedMap.get(key);
            Type oldType = indexToTypeMap.get(key);
            if (oldType != null
                    && oldType.getBlocks() < reducedType.getBlocks()) {
                Miscellaneous.printlnErr("Error # 872");
                Miscellaneous.exit();
            } else if (oldType != null
                    && oldType.getBlocks() > reducedType.getBlocks() &&
                    oldType.isIntOrShortOrByteOrBooleanOrCharType() &&
                    reducedType.isIntOrShortOrByteOrBooleanOrCharType()) {
                indexToTypeMap.put(key, reducedType);
            }
            if (integerMustIndexes.contains(key)) {
                indexToTypeMap.put(key, frameFactory.createType(Type.INTEGER));
            }
            //LVIndex = indexToTypeMap.get(key).getBlocks();
        }
    }

    private static void addLVType(Vector<Type> vector, Type toAdd) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        vector.addElement(toAdd);
        int size = toAdd.getBlocks();
        for (int loop = 0; loop < size - 1; loop++) {
            vector.addElement(frameFactory.createType(Type.SPECIAL_TAIL));
        }
    }

    public static Vector<Type> getMethodParametersTypes(String methodDesc, boolean isStatic) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        Vector<Type> ret = new Vector<Type>();
        if (!isStatic) {
            addLVType(ret, frameFactory.createType(true));
        }
        String strParam = methodDesc.substring(methodDesc.indexOf("(") + 1, methodDesc.indexOf(")"));
        int cur = 0;
        while (cur < strParam.length()) {
            Type temp = frameFactory.createType(Type.UNUSED);
            cur = InitializeFirstInstruction.getType(strParam, cur, temp);
            addLVType(ret, temp);
        }
        return ret;
    }
}
