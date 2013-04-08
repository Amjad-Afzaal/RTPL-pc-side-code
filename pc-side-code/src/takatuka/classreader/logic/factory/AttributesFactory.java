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
package takatuka.classreader.logic.factory;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class AttributesFactory {
    public static final AttributesFactory attFactory = new AttributesFactory();

    protected AttributesFactory() {
        super();
    }

    protected static AttributesFactory getInstanceOf() {
        return attFactory;
    }

    protected CodeAtt createCodeAttribute(Un u2_attrNameIndex,
                                          Un u4_attributeLength, Un u2_maxStack,
                                          Un u2_maxLocals, Un u4_codeLength) throws
            Exception {
        return new CodeAtt(u2_attrNameIndex, u4_attributeLength, u2_maxStack,
                           u2_maxLocals, u4_codeLength);
    }

    protected ConstantValueAtt createContstantValueAttribute(Un
            u2_attrNameIndex,
            Un u4_attributeLength, Un u2_constantvalueIndex) throws Exception {
        return new ConstantValueAtt(u2_attrNameIndex, u4_attributeLength,
                                    u2_constantvalueIndex);
    }

    protected DeprecatedAtt createDeprecatedAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength) throws Exception {
        return new DeprecatedAtt(u2_attrNameIndex, u4_attributeLength);
    }

    protected ExceptionsAtt createExceptionsAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength,
            Un u2_numberOfExceptions) throws Exception {
        return new ExceptionsAtt(u2_attrNameIndex, u4_attributeLength,
                                 u2_numberOfExceptions);
    }

    protected GenericAtt createGenericAttribute(Un u2_attrNameIndex,
                                                Un u4_attributeLength) throws
            Exception {
        return new GenericAtt(u2_attrNameIndex, u4_attributeLength);
    }

    protected InnerClassesAtt createInnerClassesAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength, Un numberOfClasses) throws Exception {
        return new InnerClassesAtt(u2_attrNameIndex, u4_attributeLength,
                                   numberOfClasses);
    }

    protected LineNumberTableAtt createLineNumberTableAttribute(Un
            u2_attrNameIndex, Un u4_attributeLength,
            Un u2_lineNumberTableLength) throws Exception {
        return new LineNumberTableAtt(u2_attrNameIndex, u4_attributeLength,
                                      u2_lineNumberTableLength);
    }

    protected LocalVariableTableAtt createLocalVaraibleTableAttribute(Un
            u2_attrNameIndex, Un u4_attributeLength,
            Un u2_localVariableTableLength) throws Exception {
        return new LocalVariableTableAtt(u2_attrNameIndex, u4_attributeLength,
                                         u2_localVariableTableLength);
    }

    protected SourceFileAtt createSourceFileAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength, Un u2_sourceFileIndex) throws Exception {
        return new SourceFileAtt(u2_attrNameIndex, u4_attributeLength,
                                 u2_sourceFileIndex);
    }

    protected SyntheticAtt createSyntheticAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength) throws
            Exception {
        return new SyntheticAtt(u2_attrNameIndex, u4_attributeLength);
    }

    protected Instruction createInstruction(int opcode, Un operands,
            CodeAtt parentCodeAtt) throws
            Exception {
        return new Instruction(opcode, operands, parentCodeAtt);
    }
    
    protected BytecodeProcessor createBytecodeProcessor() {
        return new BytecodeProcessor();
    }
}
