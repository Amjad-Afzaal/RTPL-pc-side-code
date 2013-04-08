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

import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.factory.*;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.optimizer.cpGlobalization.logic.factory.OptimConstantPoolFactory;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFConstantPoolFactory extends OptimConstantPoolFactory {
    private static final LFConstantPoolFactory constantPoolFactory = new
            LFConstantPoolFactory();
    protected LFConstantPoolFactory() {
        super();
    }

    static ConstantPoolFactory getInstanceOf() {
        return constantPoolFactory;
    }
    
    @Override
    protected ReferenceInfo createMethodRef() throws Exception {
        return new LFMethodRefInfo();
    }
    
    @Override
    protected ClassInfo createClassInfo() throws Exception {
        return new LFClassInfo();
    }

    @Override
    protected FieldRefInfo createFieldRef() throws Exception {
        return new LFFieldRefInfo();
    }

    @Override
    protected InterfaceMethodRefInfo createInterfaceMethodRefInfo() throws
            Exception {
        return new LFInterfaceMethodRefInfo();
    }

    @Override
    protected IntegerInfo createIntegerInfo() throws Exception {
        return new LFIntegerInfo();
    }

    @Override
    protected FloatInfo createFloatInfo() throws Exception {
        return new LFFloatInfo();
    }

    @Override
    protected LongInfo createLongInfo() throws Exception {
        return new LFLongInfo();
    }

    @Override
    protected DoubleInfo createDoubleInfo() throws Exception {
        return new LFDoubleInfo();
    }

    @Override
    protected StringInfo createStringInfo() throws Exception {
        return new LFStringInfo();
    }

    @Override
    protected NameAndTypeInfo createNameAndTypeInfo() throws Exception {
        return new LFNameAndTypeInfo();
    }

    @Override
    protected UTF8Info createUTF8Info() throws Exception {
        return new LFUTF8Info();
    }

}
