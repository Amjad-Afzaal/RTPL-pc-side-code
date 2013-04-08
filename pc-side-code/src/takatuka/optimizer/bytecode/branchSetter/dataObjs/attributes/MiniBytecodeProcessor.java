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
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class MiniBytecodeProcessor {

    private final static MiniBytecodeProcessor miniProcesser = new MiniBytecodeProcessor();
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();

    private MiniBytecodeProcessor() {
    }

    public static MiniBytecodeProcessor getInstanceOf() {
        return miniProcesser;
    }

    /**
     * 
     * @param operandOriginal
     * @param offSet
     * @return
     */
    public Vector<Integer> getTableSwitchAddreses(Un operandOriginal, int offSet) {

        Vector<Integer> ret = new Vector();
        try {
            Un operand = (Un) operandOriginal.clone();
            // Miscellaneous.println(" table swithc "+currentInstAddress);

            long padding = operand.size() % 4;
            Un.cutBytes((int) padding, operand); //padding is gone
            ret.addElement(Un.cutBytes(4, operand).intValueSigned() + offSet); //default

            int low = Un.cutBytes(4, operand).intValueSigned(); //low 
            int high = Un.cutBytes(4, operand).intValueSigned(); //high
            int size = high - low + 1;
            for (int loop = 0; loop < size; loop++) { //jump addresses - default
                ret.addElement(Un.cutBytes(4, operand).intValueSigned() + offSet); //jumpAddress
            }
            if (operand.size() != 0) {
                Miscellaneous.printlnErr("error # 879");
                Miscellaneous.exit();
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    /**
     *
     * @param operandOriginal
     * @param offSet
     * @return
     */
    public Vector<Integer> getLookupSwitchAddreses(Un operandOriginal, int offSet) {
        // Miscellaneous.println("-------> " + operandOriginal + ", " + offSet);
        Vector<Integer> ret = new Vector();
        try {
            Un operand = (Un) operandOriginal.clone();
            // Miscellaneous.println(" table swithc "+currentInstAddress);

            long padding = operand.size() % 4;
            Un.cutBytes((int) padding, operand); //padding is gone
            ret.addElement(Un.cutBytes(4, operand).intValueSigned() + offSet); //default

            int size = Un.cutBytes(4, operand).intValueSigned(); //napirs #
            // Miscellaneous.println("------> " + operand);
            for (int loop = 0; loop < size; loop++) { //jump addresses - default
                int match = Un.cutBytes(4, operand).intValueUnsigned(); //match
                ret.addElement(Un.cutBytes(4, operand).intValueSigned() + offSet); //jumpAddress
            }
            if (operand.size() != 0) {
                Miscellaneous.printlnErr("error # 879");
                Miscellaneous.exit();
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    /**
     * 
     * @param operands
     * @param jumpAddresses
     * @return
     * @throws Exception
     */
    public Un setTableSwitchAddreses(Un operands,
            Vector<Integer> jumpAddresses) throws Exception {
        Un oldOperands = (Un) operands.clone();

        // Miscellaneous.println(" table swithc "+currentInstAddress);        
        int padding = oldOperands.size() % 4;
        Un ret = factory.createUn();//.trim(padding);
        Un.cutBytes(padding + 4, oldOperands); //default address and padding is gone        
        ret.conCat(factory.createUn(jumpAddresses.remove(0))); //default
        Un low = Un.cutBytes(4, oldOperands);
        Un high = Un.cutBytes(4, oldOperands);
        long jump_count = high.intValueSigned() - low.intValueSigned() + 1;
        ret.conCat(low); //same high and low bytes
        ret.conCat(high);
        for (int loop = 0; loop < jump_count; loop++) { //jump addresses - default
            ret.conCat(factory.createUn(jumpAddresses.remove(0)));
        }
        return ret;
    }

    /**
     * 
     * @param operand
     * @param jumpAddresses
     * @return
     * @throws Exception
     */
    public Un setLookUpSwitchAddresses(Un operand,
            Vector<Integer> jumpAddresses) throws Exception {
        Un oldOperands = (Un) operand.clone();

        // Miscellaneous.println(" lookup swithc "+currentInstAddress);
        int padding = oldOperands.size() % 4;
        Un ret = factory.createUn();//.trim(padding);
        Un.cutBytes(padding + 4, oldOperands); //default address and padding is gone
        ret.conCat(factory.createUn(jumpAddresses.remove(0))); //default
        Un npair = Un.cutBytes(4, oldOperands);
        long jump_count = npair.intValueUnsigned();
        ret.conCat(npair); //set the npair
        for (int loop = 0; loop < jump_count; loop++) { //jump addresses - default
            Un match = Un.cutBytes(4, oldOperands);
            Un oldAddress = Un.cutBytes(4, oldOperands);
            ret.conCat(match);
            ret.conCat(factory.createUn(jumpAddresses.remove(0)));
        }
        return ret;
    }
}
