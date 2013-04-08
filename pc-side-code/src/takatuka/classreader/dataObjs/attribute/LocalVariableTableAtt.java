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

import org.apache.commons.lang.builder.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.exception.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;
/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.7.9 of http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#5956
 *
 * The LocalVariableTable attribute is an optional variable-length attribute of a Code (4.7.3) attribute.
 * It may be used by **debuggers** to determine the value of a given local variable during the execution of a method.
 * If LocalVariableTable attributes are present in the attributes table of a given Code attribute, then they may appear
 * in any order. There may be no more than one LocalVariableTable attribute per local variable in the Code attribute.
 *
 *  LocalVariableTable_attribute {
        u2 attribute_name_index; //super
        u4 attribute_length; //super
        u2 local_variable_table_length;
        {  u2 start_pc;
            u2 length;
            u2 name_index;
            u2 descriptor_index;
            u2 index;
        } local_variable_table[local_variable_table_length];
    }
 </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LocalVariableTableAtt extends AttributeInfo {
    private Un local_variable_table_length; //(2);
    Vector local_variable_table = new Vector(); // size should be local_variable_table_length
    FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();

    ValidateUn vUn = factory.createValidateUn();
    public LocalVariableTableAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                                 Un u2_localVariableTableLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
        /*        String name = Indexer.getUTF8StrFromCP(u2_attrNameIndex, null);
         if (!name.equals(AttributeNameIndexValues.LOCAL_VARIABLE_TABLE)) {
                    throw new Exception("What the hell");
                }
         */
        setLocalVariableTableLength(u2_localVariableTableLength);
    }

    public void populateLocalVariableTable(Un bytes) throws Exception {
        if (bytes.size() < (getLocalVariableTableLength() * 10)) {
            throw new Exception("In Local-Variable-Table-Att, Local Variable Table cannot have desired number of enteries");
        }
        for (int loop = 0; loop < getLocalVariableTableLength(); loop++) {
            local_variable_table.addElement(new LocalVariableTable(Un.cutBytes(
                    2, bytes), Un.cutBytes(2, bytes), Un.cutBytes(2, bytes),
                    Un.cutBytes(2, bytes), Un.cutBytes(2, bytes)));
        }
    }


    public Un getNameIndex(int index) {
        return ((LocalVariableTable) local_variable_table.elementAt(index)).
                getNameIndex();
    }

    public void setDescriptorIndex(int index, Un value) throws Exception {
        vUn.validateConsantPoolIndex(value);
        ((LocalVariableTable) local_variable_table.elementAt(index)).
                setDescriptorIndex(value);
    }

    public void setNameIndex(int index, Un value) throws Exception {
        vUn.validateConsantPoolIndex(value);
        ((LocalVariableTable) local_variable_table.elementAt(index)).
                setNameIndex(value);
    }

    public Un getDescriptorIndex(int index) {
        return ((LocalVariableTable) local_variable_table.elementAt(index)).
                getDescriptorIndex();
    }

    public void setLocalVariableTableLength(Un u2) throws UnSizeException,
            Exception {
        Un.validateUnSize(2, u2);
        local_variable_table_length = u2;
        //local_variable_table = new LocalVariableTable[u2.intValueUnsigned()];
    }

    public int getLocalVariableTableLength() throws Exception {
        return local_variable_table_length.intValueUnsigned();
    }

    public String toString() {
        String ret = "";
        try {
            ret = ret + writeSelected(null);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        String ret = " LocalVariableTableAtt=" + super.writeSelected(buff);
        ret = ret + ", localVariableTablelength =" +
              local_variable_table_length.writeSelected(buff);
        ret = ret + ", Table= ";
        for (int loop = 0; loop < getLocalVariableTableLength(); loop++) {
            ret = ret + "{" +
                  ((LocalVariableTable) local_variable_table.elementAt(loop)).
                  writeSelected(buff);
            if (loop + 1 < getLocalVariableTableLength()) {
                ret = ret + "}";
            }
        }
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LocalVariableTableAtt)) {
            return false;
        }
        LocalVariableTableAtt att = (LocalVariableTableAtt) obj;
        if (super.equals(att) &&
            local_variable_table_length.equals(att.local_variable_table_length) &&
            local_variable_table.equals(att.local_variable_table)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(local_variable_table).
                append(local_variable_table_length).
                append(super.hashCode()).toHashCode();
    }

    private class LocalVariableTable {
        Un start_pc;
        Un length;
        Un name_index;
        Un descriptor_index;
        Un index;
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();

        ValidateUn vUn = factory.createValidateUn();
        public LocalVariableTable(Un start_pc, Un length, Un name_index,
                                  Un descriptor_index, Un index) throws
                UnSizeException, Exception {
            Un.validateUnSize(2, start_pc);
            Un.validateUnSize(2, length);
            vUn.validateConsantPoolIndex(descriptor_index);
            vUn.validateConsantPoolIndex(name_index);

            Un.validateUnSize(2, index);
            this.start_pc = start_pc;
            this.length = length;
            this.name_index = name_index;
            this.descriptor_index = descriptor_index;
            this.index = index;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof LocalVariableTable)) {
                return false;
            }
            LocalVariableTable att = (LocalVariableTable) obj;
            if (super.equals(att) &&
                start_pc.equals(att.start_pc) && length.equals(att.length) &&
                name_index.equals(att.name_index) &&
                descriptor_index.equals(att.descriptor_index) &&
                index.equals(att.index)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return new HashCodeBuilder().append(length).
                    append(start_pc).append(name_index).
                    append(descriptor_index).
                    append(index).append(super.hashCode()).toHashCode();
        }

        public void setNameIndex(Un name_index) throws Exception {

            vUn.validateConsantPoolIndex(name_index);

            this.name_index = name_index;
        }

        public void setDescriptorIndex(Un descriptor_index) throws Exception {
            vUn.validateConsantPoolIndex(descriptor_index);
            this.descriptor_index = descriptor_index;
        }

        public Un getNameIndex() {
            return this.name_index;
        }

        public Un getDescriptorIndex() {
            return this.descriptor_index;
        }

        public String toString() {
            String ret = "";
            try {
                ret = ret + writeSelected(null);
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return ret;
        }

        public String writeSelected(BufferedByteCountingOutputStream buff) throws
                Exception {
            String ret = "";
            try {
                ret = ret + "start_pc =" + start_pc.writeSelected(buff) +
                      ", length =" + length.writeSelected(buff);
                ret = ret + ", name_index=" + name_index.writeSelected(buff) +
                      ", descriptor_index=" +
                      descriptor_index.writeSelected(buff);
                ret = ret + ", index=" + index.writeSelected(buff);
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return ret;
        }

    }
}
