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
package takatuka.optimizer.bytecode.changer.logic;

import takatuka.classreader.dataObjs.attribute.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.factory.*;
import takatuka.optimizer.bytecode.changer.logic.freq.*;
import takatuka.classreader.logic.util.*;
/**
 * 
 * Description:
 * <p>
 * Cache all the instructions of all the methods. 
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class InstructionsController extends ControllerBase {

    private static final InstructionsController stepInstCont =
            new InstructionsController();
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
    
    private int instructionSize = 0;
    private InstructionsController() {
        super();
    }

    private InstructionsController(int size) {
        super(size);
    }

    public void reCacheInstructions() {
        super.clear();
        stepInstCont.cacheAllInstructions();
    }
    public static final InstructionsController getInstanceOf() {
        stepInstCont.cacheAllInstructions();
        return stepInstCont;
    }

    public void generateFrequencies() {
        
        InstructionFrequency instFreq = InstructionFrequency.getInstanceOf();
        for (int loop = 0; loop < getCurrentSize(); loop ++) {
            Instruction inst = (Instruction) get(loop);
            instFreq.execute(inst);           
        }
      
    }
    public int getSizeOfInstructions() {
        return instructionSize;
    }
    public void cacheAllInstructions() {
        //caching for allCachedStephanInstructions
        if (super.getCurrentSize() != 0) {
            return;
        }
        try {
            //get all the classes
            ClassFileController cfController = ClassFileController.getInstanceOf();
            int size = cfController.getCurrentSize();
            ClassFile file = null;
            MethodInfoController methods = null;
            MethodInfo methodInfo = null;
            CodeAtt codeAtt = null;
            for (int loop = 0; loop < size; loop++) {
                file = (ClassFile) cfController.get(loop);
                methods = file.getMethodInfoController();
                for (int methodIndex = 0; methodIndex < methods.getCurrentSize();
                        methodIndex++) {
                    methodInfo = (MethodInfo) methods.get(methodIndex);
                    codeAtt = methodInfo.getCodeAtt();
                    if (codeAtt == null || codeAtt.getInstructions().size() == 0) {
                        continue;
                    }
                    saveInstructions(codeAtt.getInstructions());
                    //just for the breaker marker. 
                    //no need return will do it add(specialBreakStephInst());
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        //Miscellaneous.println("Stephan Insructions created =" + getCurrentSize());
    }

    private Instruction specialBreakStephInst() {
        try {            
            return factory.createInstruction(-1, new Un(), null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }

    private void saveInstructions(Vector instructions) throws Exception {
        Instruction inst = null;
        for (int loop = 0; loop < instructions.size(); loop++) {
            inst = (Instruction) instructions.elementAt(loop);
            instructionSize += inst.length();
            add(inst);
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            Instruction stepInst = (Instruction) get(loop);
            if (stepInst.getOpCode() == -1) {
                ret = ret + "-1\n";
                continue;
            }
            ret = ret + stepInst.getMnemonic() + "\n";
        }
        return ret;

    }
}
