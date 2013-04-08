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
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.dataObjs.Type;
import takatuka.verifier.logic.DFA.*;

/**
 * 
 * Description:
 * <p>
 * 
 * We want to create local variables of smaller sizes. In Java a byte or short takes
 * four bytes storage in local variables. Hence the aim of this program is to create
 * local variables that are smaller than four bytes.
 * 
 * Here is how it is implemented.
 * 1- We get all the methods of the code one by one.
 * 2- For each method, we record its description and see it is static or not.
 * 3- Based on this we assume what should be its local variables' indexes, if their sizes is reduced.
 * 4- We also know based on the method descirption that what are the current indexes of local variables.
 * 5- Based on information mentioned in above point 4 and 3. We convert indexes of existing
 *    local variables to the new indexes (that are representing smaller sizes).
 * 6. After the convertion we also set the maximum size of local variables int the 
 * codeAttribute of the methodInfo.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class ReduceTheSizeOfLocalVariables {

    private final static ReduceTheSizeOfLocalVariables myObj = new ReduceTheSizeOfLocalVariables();
    private Oracle oracle = Oracle.getInstanceOf();
    private static long totalOriginalSizeOfLocalVariables = 0;
    private static long totalNewSizeOfLocalVariables = 0;
    public static boolean doNotOptim = true;
    public static int BLOCK_SIZE_IN_BYTES = 4;
    static String currentMethodName = null;
    static String currentClassName = null;
    static String currentMethodDesc = null;
    private ChangeMaxStackSize changeStack = ChangeMaxStackSize.getInstanceOf();

    /**
     * constructor is private
     */
    private ReduceTheSizeOfLocalVariables() {
        //no one creates me but me.
    }

    /**
     * 
     * @return
     * the instance of ReduceTheSizeOfLocalVariable Class. This is a Singleton object.
     */
    public static ReduceTheSizeOfLocalVariables getInstanceOf() {
        return myObj;
    }

    /**
     * perform local variables size reduction on all the methods of all the classes.
     */
    public void reduceSizeWithoutCodeVirtualExecution() {
        if (doNotOptim) {
            return;
        }
        /**
         * The valid max block sizes are 1, 2, 4.
         */
        if (BLOCK_SIZE_IN_BYTES != 1 && BLOCK_SIZE_IN_BYTES != 2 && BLOCK_SIZE_IN_BYTES != 4) {
            Miscellaneous.printlnErr("Error! invalid block size set for local varaibles. Allowed values are 1,2 and 4");
            Miscellaneous.exit();
        }
        Vector<CodeAttCache> vectCodeAttInfo = oracle.getAllCodeAtt();
        int size = vectCodeAttInfo.size();
        for (int loop = 0; loop < size; loop++) {
            CodeAttCache codeAttInfo = vectCodeAttInfo.elementAt(loop);
            MethodInfo method = codeAttInfo.getMethodInfo();
            CodeAtt codeAtt = method.getCodeAtt();
            changeStack.execute(codeAtt);
            if (codeAtt == null || codeAtt.getMaxLocals().intValueUnsigned() == 0) {
                continue;
            }
            currentMethodDesc = oracle.methodOrFieldDescription(codeAttInfo.getMethodInfo(),
                    GlobalConstantPool.getInstanceOf());
            currentMethodName = oracle.methodOrFieldName(codeAttInfo.getMethodInfo(),
                    GlobalConstantPool.getInstanceOf());
            currentClassName = codeAttInfo.getClassFile().getFullyQualifiedClassName();

            if (currentMethodName.equals("testStaticSync") /*&& currentClassName.
                    equals("java/lang/String") && currentMethodDesc.contains("II[CI")*/) {
                ChangeCodeForReducedSizedLV.debugMe("Stop here " + currentMethodName);
            } else {
                //continue;
            }
            totalOriginalSizeOfLocalVariables += (codeAtt.getMaxLocals().intValueUnsigned() * 4);

            int oldLVMAx = codeAtt.getMaxLocals().intValueUnsigned();
            execute(currentMethodDesc, method, method.getAccessFlags().isStatic());
            ChangeCodeForReducedSizedLV.debugMe("old, new max-local-var=" + oldLVMAx + ", " + codeAtt.getMaxLocals().intValueUnsigned());
            totalNewSizeOfLocalVariables += (codeAtt.getMaxLocals().intValueUnsigned() * BLOCK_SIZE_IN_BYTES);
            if (totalNewSizeOfLocalVariables != totalOriginalSizeOfLocalVariables) {
                //Miscellaneous.println("Stop here");
            }
            if (totalNewSizeOfLocalVariables > totalOriginalSizeOfLocalVariables) {
                Miscellaneous.printlnErr("error # 842"); //error came in Sun Spot demos.
                Miscellaneous.exit();
            }
            Instruction.setAllOffsets(codeAtt.getInstructions());
            ChangeCodeForReducedSizedLV.debugMe("for method =" + currentClassName
                    + ", desc = " + currentMethodDesc + ", className="
                    + codeAttInfo.getClassFile().getFullyQualifiedClassName()
                    + " --- " + totalOriginalSizeOfLocalVariables
                    + ", " + totalNewSizeOfLocalVariables);
        }
    }

    private void execute(String methodDesc, MethodInfo method, boolean isStatic) {
        ChangeCodeForReducedSizedLV changeCode = ChangeCodeForReducedSizedLV.getInstanceOf();
        try {
            ChangeCodeForReducedSizedLV.debugMe("\n\n\n************************" + Oracle.getInstanceOf().getMethodOrFieldString(method));
            if (Oracle.getInstanceOf().getMethodOrFieldString(method).contains("checkInstanceOf")) {
                ChangeCodeForReducedSizedLV.debugMe("Stop here 124");
                //ChangeCodeForReducedSizedLV.printDebugInfo = true;
            }
            Vector<Type> typeAtOldIndexes = InitializeFirstInstruction.getMethodParametersTypes(methodDesc, isStatic);
            ChangeCodeForReducedSizedLV.debugMe("types at old indexes =" + typeAtOldIndexes);
            changeCode.execute(method, typeAtOldIndexes);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }
}
