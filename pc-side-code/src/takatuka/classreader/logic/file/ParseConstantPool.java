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
package takatuka.classreader.logic.file;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class ParseConstantPool {
    //private static final int INDEX_SIZE = 2;
    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    private static Un parseBytes(int number) throws Exception {
        return takatuka.classreader.logic.file.ParseClassFile.parseBytes(number);
    }

    public ParseConstantPool() {
    }

    private ClassInfo createClassInfo() throws Exception {
        ClassInfo classInfo = factory.createClassInfo();
        classInfo.setIndex(parseBytes(2));
        return classInfo;
    }

    private ReferenceInfo createReferenceInfo(boolean isField) throws Exception {
        ReferenceInfo fieldInfo = null;
        if (isField) {
            fieldInfo = factory.createFieldRefInfo();
        } else {
            fieldInfo = factory.createMethodRefInfo();
        }
        fieldInfo.setIndex(parseBytes(2));
        fieldInfo.setNameAndType(parseBytes(2));
        return fieldInfo;
    }


    private ReferenceInfo createInterfaceMethodReferenceInfo() throws
            Exception {
        ReferenceInfo InterInfo = factory.createInterfaceMethodRefInfo();
        InterInfo.setIndex(parseBytes(2));
        InterInfo.setNameAndType(parseBytes(2));
        return InterInfo;
    }

    private StringInfo createStringInfo() throws Exception {
        StringInfo strInfo = factory.createStringInfo();
        strInfo.setIndex(parseBytes(2));
        return strInfo;
    }

    private IntegerInfo createIntegerInfo() throws Exception {
        IntegerInfo intInfo = factory.createIntegerInfo();
        intInfo.setUpperBytes(parseBytes(4));
        return intInfo;
    }

    private FloatInfo createFloatInfo() throws Exception {
        FloatInfo floatInfo = factory.createFloatInfo();
        floatInfo.setUpperBytes(parseBytes(4));
        return floatInfo;
    }


    private LongInfo createLongInfo() throws Exception {
        LongInfo longInfo = factory.createLongInfo();
        longInfo.setUpperBytes(parseBytes(4));
        longInfo.setLowerBytes(parseBytes(4));
        return longInfo;

    }

    private DoubleInfo createDoubleInfo() throws Exception {
        DoubleInfo doubleInfo = factory.createDoubleInfo();
        doubleInfo.setUpperBytes(parseBytes(4));
        doubleInfo.setLowerBytes(parseBytes(4));
        return doubleInfo;

    }

    private NameAndTypeInfo createNameAndTypeInfo() throws Exception {
        NameAndTypeInfo nAtInfo = factory.createNameAndTypeInfo();
        nAtInfo.setIndex(parseBytes(2));
        nAtInfo.setDescriptorIndex(parseBytes(2));
        return nAtInfo;

    }

    private UTF8Info createUTF8Info() throws Exception {
        UTF8Info utf8Info = factory.createUTF8Info();
        utf8Info.setLength(parseBytes(2));
        utf8Info.setBytes(parseBytes(utf8Info.getLength().intValueUnsigned()));
        return utf8Info;
    }


    /**
     * It reads one cp_info in the constant pool
     * (see section 4.4 http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#20080)
     * then it call itself recursively to read other info, it continue until
     * whole constant pool is filled.
     * @param cpool ConstantPool
     */
    public void parseCPInfo(MultiplePoolsFacade cpool, int cpsize) throws
            TagException, Exception {
        //int cpsize = cpool.getMaxSize();
        int alreadyCapacity = cpool.getCurrentSize();
        /*Miscellaneous.println("parseCPInfo currentClass = "+ClassFile.
                currentClassToWorkOn.getClassName()+", "+alreadyCapacity);
        */
        int tag = 0;
        while ((cpool.getCurrentSize()-alreadyCapacity) < cpsize) {
            Un uTag = parseBytes(1);
            InfoBase infoBase = null;
            tag = uTag.intValueUnsigned();
            //Miscellaneous.println("CurrentFilePointer="+Integer.toHexString(ClassFileReader.getNumberOfBytesParsed()));
            switch (tag) {
            case TagValues.CONSTANT_Class: //7;
                infoBase = createClassInfo();
                break;
            case TagValues.CONSTANT_Fieldref: // 9;
                infoBase = createReferenceInfo(true);
                break;
            case TagValues.CONSTANT_Methodref: // 10;
                infoBase = createReferenceInfo(false);
                break;
            case TagValues.CONSTANT_InterfaceMethodref: //11;
                infoBase = createInterfaceMethodReferenceInfo();
                break;
            case TagValues.CONSTANT_String: // 8;
                infoBase = createStringInfo();
                break;
            case TagValues.CONSTANT_Integer: // 3;
                infoBase = createIntegerInfo();
                break;
            case TagValues.CONSTANT_Float: // 4;
                infoBase = createFloatInfo();
                break;
            case TagValues.CONSTANT_Long: // 5;
                infoBase = createLongInfo();
                break;
            case TagValues.CONSTANT_Double: // 6;
                infoBase = createDoubleInfo();
                break;
            case TagValues.CONSTANT_NameAndType: // 12;
                infoBase = createNameAndTypeInfo();
                break;
            case TagValues.CONSTANT_Utf8: // 1;
                infoBase = createUTF8Info();
                break;
                //case TagValues.CONSTANT_Empty:
                //  infoBase = new EmptyInfo();
                //break;
            default: //per section 4.9.1 it is part of verification process...
                throw new TagException(
                        "Invalid tag at constant pool at entry=" +
                        cpool.getCurrentSize() +
                        " tag=" + uTag.getData());
            }
            cpool.add(infoBase, tag);
          /*  Miscellaneous.println("current cpool "+cpool);
            Miscellaneous.println(cpsize+ ", "+ 
                    (cpool.getCurrentSize()-alreadyCapacity)+
                    " added = "+infoBase);
            */
            if (cpool.getCurrentSize() == 235) {

            }
            /*            System.out.print("Entry number =" + entry +
                                         ", number of bytes parsed=" +
             ClassFileReader.getNumberOfBytesParsed());
                        Miscellaneous.println("---------" + infoBase);
             */
        }
    }

}
