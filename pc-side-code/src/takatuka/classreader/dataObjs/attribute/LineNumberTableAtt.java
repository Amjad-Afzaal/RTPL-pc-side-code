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
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Based on section 4.7.8
 *
 *  LineNumberTable_attribute {
        u2 attribute_name_index; //super
        u4 attribute_length; //super
        u2 line_number_table_length;
        {  u2 start_pc;
           u2 line_number;
        } line_number_table[line_number_table_length];
    }
 </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class LineNumberTableAtt extends AttributeInfo {

    private Un line_number_table_length; //(2);
    private Vector line_number_table = new Vector();
    //line_number_table[line_number_table_length];

    public LineNumberTableAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                              Un u2_lineNumberTableLength) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);

        /*        ClassFileController cont = ClassFileController.getInstanceOf();
                ConstantPool cp = ((ClassFile)cont.get(cont.getCurrentSize())).getConstantPool();
         String name = Indexer.getUTF8StrFromCP(u2_attrNameIndex, cp);

                if (!name.equals(AttributeNameIndexValues.LINE_NUMBER_TABLE)) {
                    throw new Exception("What the hell");
                }
         */
        setLineNumberTableLength(u2_lineNumberTableLength);

    }

    public void setLineNumberTableLength(Un u2) throws UnSizeException,
            Exception {
        Un.validateUnSize(2, u2);
        this.line_number_table_length = u2;
        //line_number_table //= new LineNumberTable[u2.intValueUnsigned()];
    }

    public int getLineNumberTableLength() throws Exception {
        return line_number_table_length.intValueUnsigned();
    }

    public void addEntryInLineNumberTable(Un u2_start_pc, Un u2_line_number) throws
            Exception {
        line_number_table.addElement(new LineNumberTable(u2_start_pc,
                u2_line_number));
        if (line_number_table.size() > line_number_table_length.intValueUnsigned()) {
            throw new Exception(
                    " Number of enetries in LineNumberTable are not equals to LineNumberCount");
        }
    }

    public void addAllEntriesOfLineNumberTable(Un bytes) throws Exception {
        if (bytes.size() != line_number_table_length.intValueUnsigned() * 4) {
            throw new Exception(
                    " Number of enetries in LineNumberTable are not equals to LineNumberCount");
        }
        for (int loop = 0; loop < line_number_table_length.intValueUnsigned(); loop++) {
            addEntryInLineNumberTable(Un.cutBytes(2, bytes),
                                      Un.cutBytes(2, bytes));
        }
    }

    public Un getStartPC(int index) {
        return ((LineNumberTable) line_number_table.elementAt(index)).start_pc;
    }

    public int getLineNumber(int index) throws Exception {
        return ((LineNumberTable) line_number_table.elementAt(index)).
                line_number.intValueUnsigned();
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
        String ret = " LineNumberAttribute=" + super.writeSelected(buff);
        ret = ret + ", line_number_table_length =" +
              line_number_table_length.writeSelected(buff);
        for (int loop = 0; loop < getLineNumberTableLength(); loop++) {
            ret = ret + "{" +
                  ((LineNumberTable) line_number_table.elementAt(loop)).
                  writeSelected(
                          buff);
            if (loop + 1 < getLineNumberTableLength()) {
                ret = ret + "}";
            }
        }
        return ret;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof LineNumberTableAtt)) {
            return false;
        }
        LineNumberTableAtt att = (LineNumberTableAtt) obj;
        if (super.equals(att) &&
            line_number_table_length.equals(att.line_number_table_length) &&
            line_number_table.equals(att.line_number_table)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(line_number_table).
                append(line_number_table_length).
                append(super.hashCode()).toHashCode();
    }


    private class LineNumberTable {
        Un start_pc; //(2);
        Un line_number; //(2);
        public LineNumberTable(Un start_pc, Un line_number) {
            this.start_pc = start_pc;
            this.line_number = line_number;
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
                ret = ret + "start_pc =" + start_pc.writeSelected(buff);
                ret = ret + ", line_number =" + line_number.writeSelected(buff);
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return ret;
        }

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof LineNumberTable)) {
                return false;
            }
            LineNumberTable att = (LineNumberTable) obj;
            if (super.equals(att) &&
                start_pc.equals(att.start_pc) &&
                line_number.equals(att.line_number)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return new HashCodeBuilder().append(line_number).
                    append(start_pc).
                    append(super.hashCode()).toHashCode();
        }

    }
}
