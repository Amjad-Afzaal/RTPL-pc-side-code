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
package takatuka.verifier.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 * Per documentation at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#88597
 * During linking, the verifier checks the code array of the Code attribute for each method of the class file
 * by performing data-flow analysis on each method. The verifier ensures that at any given point in the program,
 * no matter what code path is taken to reach that point, the following is true:
(1) The operand stack is always the same size and contains the same types of values.
(2) No local variable is accessed unless it is known to contain a value of an appropriate type.
(3) Methods are invoked with the appropriate arguments.
(4) Fields are assigned only using values of appropriate types.
(5) All opcodes have appropriate type arguments on the operand stack and in the local variable array.
 * @author Faisal Aslam
 * @version 1.0
 */
public class Pass3 {

    private static final Pass3 pass3 = new Pass3();

    private Pass3() {
        super();
    }

    public static final Pass3 getInstanceOf() {
        return pass3;
    }

    /**
     * It execute Pass3.
     *
     */
    public void execute() throws Exception {
        Oracle oracle = Oracle.getInstanceOf();
        Vector<CodeAttCache> codeAttCacheVec = oracle.getAllCodeAtt();
        Iterator<CodeAttCache> it = codeAttCacheVec.iterator();
        while (it.hasNext()) {
            CodeAttCache codeAttCache = it.next();
            Vector instr = codeAttCache.getMethodInfo().getInstructions();
            if (instr == null || instr.size() == 0) {
                continue;
            }
            VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
            MethodInfo method = codeAttCache.getMethodInfo();
            String methodStr = oracle.getMethodOrFieldString(method);
            //Miscellaneous.println(" ------------------> "+methodStr);
            ClassFile.currentClassToWorkOn = codeAttCache.getClassFile();
            frameFactory.createDataFlowAnalyzer().execute(method, null, null);
        }
    }

}
