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
package takatuka.tukFormat.dataObjs.attribute;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.factory.*;
import takatuka.io.*;
import takatuka.tukFormat.StartMeLF;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFCodeAtt extends BHCodeAtt {

    private static FactoryFacade factory = FactoryPlaceholder.getInstanceOf().
            getFactory();

    public LFCodeAtt() throws Exception {
        
    }

    public LFCodeAtt(Un u2_attrNameIndex, Un u4_attributeLength,
            Un u2_maxStack, Un u2_maxLocals,
            Un u4_codeLength) throws Exception {
        super(u2_attrNameIndex, u4_attributeLength, u2_maxStack, u2_maxLocals,
                u4_codeLength);
    }

    /**
     * small method has one byte long stack, one byte long local variables
     * two byte long code length and one byte long exceptionatt length
     * @return
     */
    public boolean isSmallCodeAtt() {
        if (StartMeLF.shouldAlwaysBigMethods()) {
            return false;
        }
        try {
            if (getCodeLength().intValueUnsigned() > 65535 ||
                    getMaxLocals().intValueUnsigned() > 255 ||
                    getMaxStack().intValueUnsigned() > 255 ||
                    getExceptions().size() > 255) {
                return false;
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return true;
    }

    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        boolean isSmall = isSmallCodeAtt();
        String ret = " CodeAtt="; // + super.writeSelected(buff);
        ret += ", max_stack =";
        if (isSmall) {
            ret += super.getMaxStack().trim(1).writeSelected(buff);
        } else {
            ret += super.getMaxStack().writeSelected(buff);
        }
        if (isSmall) {
            ret += ", max_locals =" + super.getMaxLocals().trim(1).writeSelected(buff);
        } else {
            ret += ", max_locals =" + super.getMaxLocals().writeSelected(buff);
        }
        if (isSmall) {
            ret += ", code_length =" +
                    super.getCodeLength().trim(2).writeSelected(buff);
        } else {
            ret += ", code_length =" +
                    super.getCodeLength().writeSelected(buff);
        }
        ret += ", " + super.writeInsructions(buff);

        if (isSmall) {
            ret += ", exception_table_length =" +
                    super.getExceptionTableLength().trim(1).writeSelected(buff);
        } else {
            ret += ", exception_table_length =" +
                    super.getExceptionTableLength().writeSelected(buff);
        }
        ret += ", exception_table={";
        Vector exception_table = getExceptions();
        for (int loop = 0; loop < getExceptionTableLength().intValueUnsigned(); loop++) {
            ret +=
                    ((BaseObject) exception_table.elementAt(loop)).writeSelected(
                    buff) + ", ";
        }
        ret += "} ";
        return ret;
    }
}
