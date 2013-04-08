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
package takatuka.tukFormat.logic.factory;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.factory.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.tukFormat.dataObjs.attribute.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFAttributesFactory extends GCAttributesFactory {

    protected LFAttributesFactory() {
        super();
    }
    private static final LFAttributesFactory factory = new LFAttributesFactory();

    protected static AttributesFactory getInstanceOf() {
        return factory;
    }

    @Override
    protected CodeAtt createCodeAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength,
            Un u2_maxStack,
            Un u2_maxLocals, Un u4_codeLength) throws
            Exception {
        return new LFCodeAtt(u2_attrNameIndex, u4_attributeLength, u2_maxStack,
                u2_maxLocals, u4_codeLength);
    }

    @Override
    protected ExceptionsAtt createExceptionsAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength,
            Un u2_numberOfExceptions) throws Exception {
        return new LFExceptionsAtt(u2_attrNameIndex, u4_attributeLength,
                u2_numberOfExceptions);
    }

    @Override
    protected Instruction createInstruction(int opcode, Un operands, CodeAtt codeAtt) throws
            Exception {
        return new LFInstruction(opcode, operands, codeAtt);
    }
}
