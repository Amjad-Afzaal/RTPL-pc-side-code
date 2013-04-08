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
 * <p>Description: Based on section 4.7.5 at http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html#43817
 *
 * InnerClasses_attribute {
        u2 attribute_name_index; //super
        u4 attribute_length; //super

        u2 number_of_classes;
        {  u2 inner_class_info_index;
           u2 outer_class_info_index;
           u2 inner_name_index;
           u2 inner_class_access_flags;
        } classes[number_of_classes];
    }
 </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class InnerClassesAtt extends AttributeInfo {
    private Un number_of_classes; //(2);
    private Vector classes = new Vector();
    public InnerClassesAtt() {
    }

    public InnerClassesAtt(Un u2_attrNameIndex, Un u4_attributeLength,
                           Un numberOfClasses) throws
            Exception {
        super(u2_attrNameIndex, u4_attributeLength);
        setNumberOfClasses(numberOfClasses);
    }


    public void setNumberOfClasses(Un u2) throws UnSizeException, Exception {
        Un.validateUnSize(2, u2);
        this.number_of_classes = u2;
        //classes = new Classes[number_of_classes.intValueUnsigned()];
    }

    public int getNumberOfClasses() throws Exception {
        return number_of_classes.intValueUnsigned();
    }

    public void addClass(Un innerClassInfoIndex, Un outerClassInfoIndex,
                         Un innerNameIndex, Un innerClassAccessFlags) throws
            Exception {
        classes.addElement(new Classes(innerClassInfoIndex, outerClassInfoIndex,
                                       innerNameIndex, innerClassAccessFlags));
    }


    public Un getInnerClassInfoIndex(int index) {
        return ((Classes) classes.elementAt(index)).inner_class_info_index;
    }

    public Un getOuterClassInfoIndex(int index) {
        return ((Classes) classes.elementAt(index)).outer_class_info_index;
    }

    public Un getInnerNameIndex(int index) {
        return ((Classes) classes.elementAt(index)).inner_name_index;
    }

    public void setInnerClassInfoIndex(int index, Un value) throws Exception {
        ((Classes) classes.elementAt(index)).setInnerClassInfoIndex(value);
    }

    public void setOuterClassInfoIndex(int index, Un value) throws Exception {
        ((Classes) classes.elementAt(index)).setOuterClassInfoIndex(value);
    }

    public void setInnerNameIndex(int index, Un value) throws Exception {
        ((Classes) classes.elementAt(index)).setInnerNameIndex(value);
    }

    public Un getInnerClassAccessFlags(int index) {
        return ((Classes) classes.elementAt(index)).inner_class_access_flags;
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
        String ret = "\tInnerClassAtt=" + super.writeSelected(buff);
        ret = ret + ", number_of_classes=" +
              number_of_classes.writeSelected(buff);
        ret = ret + ", Classes = {";
        for (int loop = 0; loop < classes.size(); loop++) {
            ret = ret + "{" +
                  ((Classes) classes.elementAt(loop)).writeSelected(buff);
            if (loop + 1 < getNumberOfClasses()) {
                ret = ret + "}";
            }
        }
        ret = ret + "}";
        return ret;
    }


    public void addAllClasses(Un bytes) throws Exception {
        if (bytes.size() != getNumberOfClasses() * 8) {
            throw new Exception("Invalid Inner class data");
        }
        for (int loop = 0; loop < number_of_classes.intValueUnsigned(); loop++) {
            classes.addElement(new Classes(Un.cutBytes(2, bytes),
                                           Un.cutBytes(2, bytes),
                                           Un.cutBytes(2, bytes),
                                           Un.cutBytes(2, bytes)));
        }
    }

    private class Classes {
        Un inner_class_info_index; //(2);
        Un outer_class_info_index; //(2);
        Un inner_name_index; //(2);
        Un inner_class_access_flags; //(2);
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();

        ValidateUn vUn = factory.createValidateUn();
        public Classes(Un inner_class_info_index, Un outer_class_info_index,
                       Un inner_name_index, Un inner_class_access_flags) throws
                UnSizeException, Exception {

            vUn.validateConsantPoolIndex(inner_class_info_index);
            vUn.validateConsantPoolIndex(outer_class_info_index);
            vUn.validateConsantPoolIndex(inner_name_index);
            this.inner_class_info_index = inner_class_info_index;
            this.outer_class_info_index = outer_class_info_index;
            this.inner_name_index = inner_name_index;
            this.inner_class_access_flags = inner_class_access_flags;
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

        public boolean equals(Object obj) {
            if (obj == null || !(obj instanceof Classes)) {
                return false;
            }
            Classes att = (Classes) obj;
            if (super.equals(att) &&
                inner_class_info_index.equals(att.inner_class_info_index) &&
                outer_class_info_index.equals(att.outer_class_info_index)
                && inner_name_index.equals(att.inner_name_index)
                && inner_class_access_flags.equals(att.inner_class_access_flags)) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return new HashCodeBuilder().append(inner_class_info_index).
                    append(outer_class_info_index).append(inner_name_index).
                    append(inner_class_access_flags).
                    append(super.hashCode()).toHashCode();
        }

        public String writeSelected(BufferedByteCountingOutputStream buff) throws
                Exception {
            String ret = "";
            try {
                ret = ret + "inner_class_info_index=" +
                      inner_class_info_index.writeSelected(buff);
                ret = ret + ", outer_class_info_index=" +
                      outer_class_info_index.writeSelected(buff);
                ret = ret + ", inner_name_index=" +
                      inner_name_index.writeSelected(buff);
                ret = ret + ", inner_class_access_flags=" +
                      inner_class_access_flags.writeSelected(buff);
            } catch (Exception d) {
                d.printStackTrace();
                Miscellaneous.exit();
            }
            return ret;
        }

        public void setInnerClassInfoIndex(Un inner_class_info_index) throws
                Exception {
            vUn.validateConsantPoolIndex(inner_class_info_index);
            this.inner_class_info_index = inner_class_info_index;
        }

        public void setOuterClassInfoIndex(Un outer_class_info_index) throws
                Exception {
            vUn.validateConsantPoolIndex(outer_class_info_index);
            this.outer_class_info_index = outer_class_info_index;
        }

        public void setInnerNameIndex(Un inner_name_index) throws Exception {
            vUn.validateConsantPoolIndex(inner_name_index);
            this.inner_name_index = inner_name_index;
        }

    }


    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof InnerClassesAtt)) {
            return false;
        }
        InnerClassesAtt att = (InnerClassesAtt) obj;
        if (super.equals(att) && number_of_classes.equals(att.number_of_classes) &&
            classes.equals(att.classes)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return new HashCodeBuilder().append(classes).
                append(number_of_classes).
                append(super.hashCode()).toHashCode();
    }


}
