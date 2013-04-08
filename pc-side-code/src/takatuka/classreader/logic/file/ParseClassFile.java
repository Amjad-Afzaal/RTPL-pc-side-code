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

import java.io.*;
import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;


/**
 * <p>Title: </p>
 * <p>Description:
 * Here we will parse the .class files and extract information out from it.
 * * It read data from class files into a ClassFile object...
 *
 *
    ClassFile {
        u4 magic;
        u2 minor_version;
        u2 major_version;
        u2 constant_pool_count;
        cp_info constant_pool[constant_pool_count-1];
        u2 access_flags;
        u2 this_class;
        u2 super_class;
        u2 interfaces_count;
        u2 interfaces[interfaces_count];
        u2 fields_count;
        field_info fields[fields_count];
        u2 methods_count;
        method_info methods[methods_count];
        u2 attributes_count;
        attribute_info attributes[attributes_count];
    }

 * </p>
 * @author Faisal aslam
 * @version 1.0
 */
public class ParseClassFile {
    private static final ParseClassFile parseCFile = new ParseClassFile();

    // private ClassFile classFile = null;
    private static DataFileInputStreamWithValidation dataStream = null;
    public static int numberOfBytesParsed = 0;
    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                           getFactory();

    private ParseClassFile() {
        super();
    }

    public static ParseClassFile getInstanceOf() {
        return parseCFile;
    }


    public static Un parseBytes(int size) throws
            Exception {
        byte byteToReadIn[] = new byte[size];
        int eof = dataStream.read(byteToReadIn, 0, size);
        if (eof == -1) {
            throw new VerifyError(DataFileInputStreamWithValidation.
                                  TRUNCATED_BYTES);
        }

        Un ret = factory.createUn(byteToReadIn);
        if (ret.size() != size) {
            throw new Exception("A Bug code=" + 121);
        }
        numberOfBytesParsed = numberOfBytesParsed + size;
        return ret;
    }

    public static int getNumberOfBytesParsed() {
        return numberOfBytesParsed;
    }

    private Vector parseAttribute(int size) throws Exception {
        Vector gAtts = new Vector();
        int length = 0;
        GenericAtt temp = null;
        for (int loop = 0; loop < size; loop++) {
            temp = factory.createGenericAttribute(parseBytes(2),
                                                  parseBytes(4));
            gAtts.addElement(temp);
            length = temp.getAttributeLength().intValueUnsigned();
            temp.setInfo(parseBytes(length));
        }
        return gAtts;
    }

    private void readFieldMethodInfo(ClassFile cFile, ControllerBase cBase,
            boolean isField) throws Exception {
        int size = cBase.getMaxSize();
        FieldInfo fInfo = null;
        for (int loop = 0; loop < size; loop++) {
            if (isField) {
                fInfo = factory.createFieldInfo(cFile);
            } else {
                fInfo = factory.createMethodInfo(cFile);
            }
            fInfo.setAccessFlags(parseBytes(2));
            fInfo.setNameIndex(parseBytes(2));
//            Miscellaneous.println("I am here"+fieldName);
            fInfo.setDescriptorIndex(parseBytes(2));
            fInfo.setAttributeCount(parseBytes(2));
            fInfo.addAttribute(parseAttribute(fInfo.getAttributeCount().
                                              intValueUnsigned()));
            cBase.add(fInfo);
        }
    }

    /**
     *
     * @param dataStream DataFileInputStreamWithValidation
     * @throws IOException
     * @throws Exception
     */
    protected void parseFile(DataFileInputStreamWithValidation dataStream,
                             ClassFile classFile) throws
            IOException,
            Exception {
        int index = 0;
        numberOfBytesParsed = 0;
        int size = 0;
        ParseConstantPool parseCP = new ParseConstantPool();
        ParseClassFile.dataStream = dataStream;
        MultiplePoolsFacade cpool = classFile.getConstantPool();
        while (dataStream.available() != 0) {
            //Miscellaneous.println("CurrentFilePointer="+Integer.toHexString(ClassFileReader.getNumberOfBytesParsed()));
            switch (index++) {
            case 0: //u4 magic;
                classFile.setMagicNumber(parseBytes(
                        SectionSizes.MAGIC_NUMBER_SIZE));

                //index = 3;
                break;
            case 1: //u2 minor_version;
                classFile.setMinorVersion(parseBytes(
                        SectionSizes.MINOR_VERSION_SIZE));
                break;
            case 2: //u2 major_version;
                classFile.setMajorVersion(parseBytes(
                        SectionSizes.MAGOR_VERSION_SIZE));
                break;
            case 3: //u2 constant_pool_count;
                classFile.setConstantPoolCount(parseBytes(
                        SectionSizes.CONSTANT_POOL_COUNT_SIZE));

                break;
            case 4: //constant pool
                parseCP.parseCPInfo(cpool, classFile.getConstantPoolCount()-1);
                
                //System.out.print("{"+cpool+"}");
                
                break;
            case 5: //u2 access_flags;
                classFile.setAccessFlags(factory.createAccessFlag(parseBytes(
                        SectionSizes.
                        ACCESS_FLAG_SIZE).getData()));
                break;
            case 6: //this_class
                //Miscellaneous.println(cpool);
                classFile.setThisClass(parseBytes(SectionSizes.THIS_CLASS_SIZE));
                break;
            case 7: //super_class
                classFile.setSuperClass(parseBytes(SectionSizes.SUPER_CLASS_SIZE));
                //Miscellaneous.println(UTF8Info.convertBytes ((UTF8Info)classFile.getConstantPool().get(cinfo.getIndex().intValueUnsigned())));
                break;
            case 8: //interfaces_count
                classFile.setInterfaceCount(parseBytes(
                        SectionSizes.INTERFACES_COUNT_SIZE));
                break;
            case 9: //interfaces
                size = classFile.getInterfaceController().getMaxSize();
                if (size > 0) {
                    classFile.addInterfacesInfos(parseBytes(size * 2)); //multiplied by two because it is "u2 interfaces[interfaces_count]" (note u2);
                }
                break;
            case 10: //fields_count
                classFile.setFieldCount(parseBytes(SectionSizes.
                        FIELD_COUNT_SIZE));

///                Miscellaneous.println("Field Count"+classFile.getFieldCount());
                break;
            case 11: //fields
                FieldInfoController fcont = classFile.getFieldInfoController();
                readFieldMethodInfo(classFile, fcont, true);
                break;
            case 12: //method count
                classFile.setMethodCount(parseBytes(SectionSizes.
                        METHOD_COUNT_SIZE));
                break;
            case 13: //methods
                MethodInfoController mcont = classFile.getMethodInfoController();
                readFieldMethodInfo(classFile, mcont, false);
                break;
            case 14: //attributes_count
                classFile.setAttributesCount(parseBytes(
                        SectionSizes.ATTRIBUTES_COUNT_SIZE));
                if (classFile.getAttributeInfoController().getMaxSize() == 0) {
                    index++;
                }
                break;
            case 15: //attributes

//              Miscellaneous.println(classFile.getAttributesCount().intValueUnsigned());
                Vector gAtt = parseAttribute(classFile.
                                             getAttributeInfoController().
                                             getMaxSize());
                classFile.getAttributeInfoController().add(gAtt);
                break;
            default:
                throw new VerifyError(DataFileInputStreamWithValidation.
                                      EXTRA_BYTES);
            }
        }

    }

}
