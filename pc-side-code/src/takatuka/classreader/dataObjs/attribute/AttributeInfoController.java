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
package takatuka.classreader.dataObjs.attribute;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;
import takatuka.io.*;


/**
 * <p>Title: </p>
 * <p>Description:
 * Based on the structure defined at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class AttributeInfoController extends ControllerBase {

    private FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
                                    getFactory();

    /**
     *
     * @param size int
     */
    public AttributeInfoController(int size) {
        super(size);
        super.setAllowedClassName(AttributeInfo.class);
    }

    /**
     *
     * @param obj Object
     * @throws Exception
     */
    public void add(Object obj) throws Exception {
        if (obj instanceof Vector) {
            addCollection((Vector) obj);
        } else if (obj instanceof GenericAtt) {
            addGeneric((GenericAtt) obj);
        } else if (obj instanceof AttributeInfo) {
            super.add(obj);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private CodeAtt populateCodeAtt(GenericAtt gAtt) throws Exception {
        Un info = gAtt.getInfo();
        CodeAtt cAtt = factory.createCodeAttribute(gAtt.getAttributeNameIndex(),
                gAtt.getAttributeLength(),
                Un.cutBytes(2, info), //stack
                Un.cutBytes(2, info), //locals
                Un.cutBytes(4, info)); //code length

        cAtt.setCode(Un.cutBytes(cAtt.getCodeLength().intValueUnsigned(), info));
        cAtt.setExceptionTableLength(Un.cutBytes(2, info));
        cAtt.addAllExceptionTablesByCutting(info);
        cAtt.setAttributeCount(Un.cutBytes(2, info));
        for (int loop = 0; loop < cAtt.getAttributeCount(); loop++) {
            GenericAtt tempGAtt =
                    factory.createGenericAttribute(Un.cutBytes(2, info),
                    Un.cutBytes(4, info));
            tempGAtt.setInfo(Un.cutBytes(tempGAtt.getAttributeLength().intValueUnsigned(),
                                         info));
            cAtt.addAttribute(tempGAtt);
        }
        return cAtt;
    }

    /**
     *  The function will create different specific attributes depending on attribute_name_index
     *
     * @param attribute_name_index Un
     * @param attribute_length Un
     * @param info Un
     * @throws Exception
     */
    private void addGeneric(GenericAtt gAtt) throws Exception {
        if (gAtt == null) {
            return; //no exception. A field, method might not have any attribute.
        }
        boolean shouldIgnore = false;
        Un.validateUnSize(2, gAtt.getAttributeNameIndex());
        Un.validateUnSize(4, gAtt.getAttributeLength());
        String attributeName = ConstantPoolDigger.getUTF8StrFromCP(gAtt.
                getAttributeNameIndex());

        AttributeInfo attInfo = null;
        Un info = gAtt.getInfo();
        if (attributeName.equals(AttributeNameIndexValues.CONSTANT_VALUE)) {
            attInfo = factory.createContstantValueAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    info);

        } else if (attributeName.equals(AttributeNameIndexValues.CODE)) {
            attInfo = populateCodeAtt(gAtt);
//            Miscellaneous.println(attInfo);
        } else if (attributeName.equals(AttributeNameIndexValues.EXCEPTION)) {
            attInfo = factory.createExceptionsAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    Un.cutBytes(2, info));
            ((ExceptionsAtt) attInfo).setAllExceptionIndexTable(gAtt.getInfo());
        } else if (attributeName.equals(AttributeNameIndexValues.INNERCLASSES)) {
            attInfo = factory.createInnerClassesAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    Un.cutBytes(2, info));
            ((InnerClassesAtt) attInfo).addAllClasses(info);

        } else if (attributeName.equals(AttributeNameIndexValues.SYNTHETIC)) {
            attInfo = factory.createSyntheticAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength());
        } else if (attributeName.equals(AttributeNameIndexValues.SOURCE_FILE)) {
            attInfo = factory.createSourceFileAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    info);

        } else if (attributeName.equals(AttributeNameIndexValues.
                                        LINE_NUMBER_TABLE)) {
            attInfo = factory.createLineNumberTableAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    Un.cutBytes(2, info));
            ((LineNumberTableAtt) attInfo).addAllEntriesOfLineNumberTable(info);

        } else if (attributeName.equals(AttributeNameIndexValues.
                                        LOCAL_VARIABLE_TABLE)) {
            attInfo = factory.createLocalVaraibleTableAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength(),
                    Un.cutBytes(2, info));
            ((LocalVariableTableAtt) attInfo).populateLocalVariableTable(info);

        } else if (attributeName.equals(AttributeNameIndexValues.DEPRECATED)) {
            attInfo = factory.createDeprecatedAttribute(gAtt.
                    getAttributeNameIndex(),
                    gAtt.getAttributeLength());
        } else {
            //it is a VM defined generic attribute.
            //we ignore those attributes
            //attInfo = gAtt;
            // Miscellaneous.println(">>>>>>>>>>>>>>>>>> ignoring a VM defined attribute ");
            attInfo = gAtt;
            super.add(attInfo);
            shouldIgnore = true;
            //factory.create
        }
        if (!shouldIgnore) {
            add(attInfo);
        }
    }

    private void addCollection(Vector gAtt) throws Exception {

        for (int loop = 0; loop < gAtt.size(); loop++) {
            add(((GenericAtt) gAtt.elementAt(loop)));
            //super.add(gAtt.elementAt(loop)); //temp
        }
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = "size=" +
                     factory.createUn(getCurrentSize()).trim(2).
                     writeSelected(buff);
        for (int loop = 0; loop < getCurrentSize(); loop++) {
            AttributeInfo gAtt = (AttributeInfo) get(loop);
            ret = ret + "{" + gAtt.writeSelected(buff) + "}";
        }
        return ret;
    }

    public String toString() {
        try {
            return writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return null;
    }

}
