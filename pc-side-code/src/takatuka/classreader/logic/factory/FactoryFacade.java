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
import takatuka.classreader.dataObjs.constantPool.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * This factory provide facade design pattern for all the factories in the
 * classreader project. Hence this factory itself does not create anything
 * but uses other factory to create things.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FactoryFacade {

    private static final FactoryFacade facadeFactory =
            new FactoryFacade();
//-------------------------------------------------------------------------------------------
// Note: One can inherit only following six function otherwise, everything else is final and
// should remain final
    protected FactoryFacade() {
        super();
    }

    static FactoryFacade getInstanceOf( /*FactoryPlaceholder placeHolder*/) {
        /*        if (placeHolder != null) { */
        //todo fix it. The idea is that no once use getInstanceOf other than FactoryPlaceholder.
        //like create a "friend" relationship between this class and FactoryPlaceholder.
        //However, that todo is written because one can provide a dummy FactoryPlaceholder and use this function...
        return facadeFactory;
    /*        }
    return null;*/
    }

    protected AttributesFactory getAttFactory() {
        return AttributesFactory.getInstanceOf();
    }

    protected ControllerFactory getControllerFactory() {
        return ControllerFactory.getInstanceOf();
    }

    protected ClassFileFactory getClassFileFactory() {
        return ClassFileFactory.getInstanceOf();
    }

    protected UnFactory getUnFactory() {
        return UnFactory.getInstanceOf();
    }

    protected ConstantPoolFactory getConstantPoolFactory() {
        return ConstantPoolFactory.getInstanceOf();
    }

//ConstantPool ---------------------------------------------------------------------------
    public final MultiplePoolsFacade createConstantPool() throws Exception {
        return getConstantPoolFactory().createConstantPool();
    }

    public final ReferenceInfo createFieldRefInfo() throws Exception {
        return getConstantPoolFactory().createFieldRef();
    }

    public final ReferenceInfo createFieldRefInfo(Un classIndex, Un nameAndTypeIndex) throws Exception {
        return getConstantPoolFactory().createFieldRef(classIndex, nameAndTypeIndex);
    }

    public final ReferenceInfo createMethodRefInfo() throws Exception {
        return getConstantPoolFactory().createMethodRef();
    }

    public final ReferenceInfo createMethodRefInfo(Un classIndex, Un nameAndTypeIndex) throws Exception {
        return getConstantPoolFactory().createMethodRef(classIndex, nameAndTypeIndex);
    }

    public final ClassInfo createClassInfo() throws Exception {
        return getConstantPoolFactory().createClassInfo();
    }

    public final ReferenceInfo createInterfaceMethodRefInfo() throws
            Exception {
        return getConstantPoolFactory().createInterfaceMethodRefInfo();
    }

    public final ReferenceInfo createInterfaceMethodRefInfo(Un classIndex, Un nameAndTypeIndex) throws
            Exception {
        return getConstantPoolFactory().createInterfaceMethodRefInfo(classIndex, nameAndTypeIndex);
    }

    public final StringInfo createStringInfo() throws Exception {
        return getConstantPoolFactory().createStringInfo();
    }

    public final IntegerInfo createIntegerInfo() throws Exception {
        return getConstantPoolFactory().createIntegerInfo();
    }

    public final FloatInfo createFloatInfo() throws Exception {
        return getConstantPoolFactory().createFloatInfo();
    }

    public final LongInfo createLongInfo() throws Exception {
        return getConstantPoolFactory().createLongInfo();
    }

    public final DoubleInfo createDoubleInfo() throws Exception {
        return getConstantPoolFactory().createDoubleInfo();
    }

    public final NameAndTypeInfo createNameAndTypeInfo() throws Exception {
        return getConstantPoolFactory().createNameAndTypeInfo();
    }

    public final UTF8Info createUTF8Info() throws Exception {
        return getConstantPoolFactory().createUTF8Info();
    }

//------------------------------------------------------------------------------------------
//ClassFile
    public final AccessFlags createAccessFlag() throws Exception {
        return getClassFileFactory().createAccessFlag();
    }

    public final AccessFlags createAccessFlag(Un un) throws Exception {
        return getClassFileFactory().createAccessFlag(un);
    }

    public final AccessFlags createAccessFlag(byte[] bytes) throws Exception {
        return getClassFileFactory().createAccessFlag(bytes);
    }

    public final ClassFile createClassFile(MultiplePoolsFacade cstPool,
            String uniqueName) throws
            Exception {
        return getClassFileFactory().createClassFile(cstPool, uniqueName);
    }

    public final MethodInfo createMethodInfo(ClassFile myClass) {
        return getClassFileFactory().createMethodInfo(myClass);
    }

    public final FieldInfo createFieldInfo(ClassFile myClassFile) {
        return getClassFileFactory().createFieldInfo(myClassFile);
    }
//Un
    public final Un createUn() throws Exception {
        return getUnFactory().createUn();
    }

    public final Un createUn(byte[] bytes) throws Exception {
        return getUnFactory().createUn(bytes);
    }

    public final Un createUn(int value) throws Exception {
        return getUnFactory().createUn(value);
    }

    public final ValidateUn createValidateUn() throws Exception {
        return getUnFactory().createValidateUn();
    }
// Attributes
    public final Instruction createInstruction(int opcode, Un operands,
            CodeAtt parentCodeAtt) throws
            Exception {
        return getAttFactory().createInstruction(opcode, operands, parentCodeAtt);
    }

    public final BytecodeProcessor createBytecodeProcessor() {
        return getAttFactory().createBytecodeProcessor();
    }

    public final CodeAtt createCodeAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength,
            Un u2_maxStack,
            Un u2_maxLocals, Un u4_codeLength) throws
            Exception {
        return getAttFactory().createCodeAttribute(u2_attrNameIndex,
                u4_attributeLength, u2_maxStack,
                u2_maxLocals, u4_codeLength);

    }

    public final ConstantValueAtt createContstantValueAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength, Un u2_constantvalueIndex) throws Exception {
        return getAttFactory().createContstantValueAttribute(u2_attrNameIndex,
                u4_attributeLength, u2_constantvalueIndex);
    }

    public final DeprecatedAtt createDeprecatedAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength) throws Exception {
        return getAttFactory().createDeprecatedAttribute(u2_attrNameIndex,
                u4_attributeLength);
    }

    public final ExceptionsAtt createExceptionsAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength,
            Un u2_numberOfExceptions) throws Exception {
        return getAttFactory().createExceptionsAttribute(u2_attrNameIndex,
                u4_attributeLength, u2_numberOfExceptions);
    }

    public final GenericAtt createGenericAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength) throws
            Exception {
        return getAttFactory().createGenericAttribute(u2_attrNameIndex,
                u4_attributeLength);
    }

    public final InnerClassesAtt createInnerClassesAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength, Un numberOfClasses) throws Exception {
        return getAttFactory().createInnerClassesAttribute(u2_attrNameIndex,
                u4_attributeLength, numberOfClasses);
    }

    public final LineNumberTableAtt createLineNumberTableAttribute(Un u2_attrNameIndex, Un u4_attributeLength,
            Un u2_lineNumberTableLength) throws Exception {
        return getAttFactory().createLineNumberTableAttribute(u2_attrNameIndex,
                u4_attributeLength, u2_lineNumberTableLength);
    }

    public final LocalVariableTableAtt createLocalVaraibleTableAttribute(Un u2_attrNameIndex, Un u4_attributeLength,
            Un u2_localVariableTableLength) throws Exception {
        return getAttFactory().createLocalVaraibleTableAttribute(
                u2_attrNameIndex,
                u4_attributeLength, u2_localVariableTableLength);
    }

    public final SourceFileAtt createSourceFileAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength, Un u2_sourceFileIndex) throws Exception {
        return getAttFactory().createSourceFileAttribute(u2_attrNameIndex,
                u4_attributeLength, u2_sourceFileIndex);
    }

    public final SyntheticAtt createSyntheticAttribute(Un u2_attrNameIndex,
            Un u4_attributeLength) throws
            Exception {
        return getAttFactory().createSyntheticAttribute(u2_attrNameIndex,
                u4_attributeLength);
    }
//Controllers
    public final AttributeInfoController createAttributeInfoController(int size) throws
            Exception {
        return getControllerFactory().createAttributeInfoController(size);
    }

    public final FieldInfoController createFieldInfoController(int size) throws
            Exception {
        return getControllerFactory().createFieldInfoController(size);
    }

    public final MethodInfoController createMethodInfoController(int size) throws
            Exception {
        return getControllerFactory().createMethodInfoController(size);
    }

    public final InterfaceController createInterfaceController(int size) throws
            Exception {
        return getControllerFactory().createInterfaceController(size);
    }
}
