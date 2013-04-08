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

import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ConstantPoolFactory {

    protected ConstantPoolFactory() {
        super();
    }
    private static final ConstantPoolFactory cpFactory = new ConstantPoolFactory();

    static ConstantPoolFactory getInstanceOf() {
        return cpFactory;
    }

    protected MultiplePoolsFacade createConstantPool() throws Exception {
        return new ConstantPool();
    }

    protected ReferenceInfo createMethodRef() throws Exception {
        return new MethodRefInfo();
    }

    protected ReferenceInfo createMethodRef(Un classIndex, Un nameAndTypeIndex) throws Exception {
        ReferenceInfo field = createMethodRef();
        field.setIndex(classIndex);
        field.setNameAndType(nameAndTypeIndex);
        return field;
    }

    protected ClassInfo createClassInfo() throws Exception {
        return new ClassInfo();
    }

    protected ReferenceInfo createFieldRef() throws Exception {
        return new FieldRefInfo();
    }

    protected ReferenceInfo createFieldRef(Un classIndex, Un nameAndTypeIndex) throws Exception {
        ReferenceInfo field = createFieldRef();
        field.setIndex(classIndex);
        field.setNameAndType(nameAndTypeIndex);
        return field;
    }

    protected ReferenceInfo createInterfaceMethodRefInfo() throws
            Exception {
        return new InterfaceMethodRefInfo();
    }

    protected ReferenceInfo createInterfaceMethodRefInfo(Un classIndex, Un nameAndTypeIndex) throws Exception {
        ReferenceInfo field = createInterfaceMethodRefInfo();
        field.setIndex(classIndex);
        field.setNameAndType(nameAndTypeIndex);
        return field;
    }

    protected StringInfo createStringInfo() throws Exception {
        return new StringInfo();
    }

    protected IntegerInfo createIntegerInfo() throws Exception {
        return new IntegerInfo();
    }

    protected FloatInfo createFloatInfo() throws Exception {
        return new FloatInfo();
    }

    protected LongInfo createLongInfo() throws Exception {
        return new LongInfo();
    }

    protected DoubleInfo createDoubleInfo() throws Exception {
        return new DoubleInfo();
    }

    protected NameAndTypeInfo createNameAndTypeInfo() throws Exception {
        return new NameAndTypeInfo();
    }

    protected UTF8Info createUTF8Info() throws Exception {
        return new UTF8Info();
    }
}
