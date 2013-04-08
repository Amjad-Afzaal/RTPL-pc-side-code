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
package takatuka.verifier.logic;

import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.logic.constants.*;
import takatuka.verifier.logic.exception.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Here we verify all the static constraints of constant pool
 * Following are the constraints that we check here.
 * 1. In MethodRefInfo, FieldRefInfo and InterfaceRefInfo each
 *   class_index point to a classInfo, each nameAndTypeIndex points to nameAndTypeIndex.
 * 2. Each staring_index in a StringInfo point to a valid UTF8Info
 * 3. Each NameAndTypeInfo's name_index and descriptor_index point to UTF8Infos
 * 4. In UTF8Info length is equal to bytes array size.
 * 5. ClassInfo's name_index point to a UTF8Info.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ConstantPoolStaticConstraints {

    private static final ConstantPoolStaticConstraints cpSC =
            new ConstantPoolStaticConstraints();

    private static GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
    private ConstantPoolStaticConstraints() {
    }

    public static ConstantPoolStaticConstraints getInstanceOf() {
        return cpSC;
    }


    /**
     * It executes all the constaints on the given Constant Pool
     *
     * @param cp ConstantPool
     */
    public void execute() throws Exception { //1
        InfoBase cpValue = null;
        TreeSet poolIds = pOne.getPoolIds();
        Iterator poolIdsIt = poolIds.iterator();
        while (poolIdsIt.hasNext()) {
            int poolId = (Integer)poolIdsIt.next();
            
        for (int loop = 1; loop < pOne.getCurrentSize(poolId); loop++) {
            cpValue = (InfoBase)pOne.get(loop, poolId);
            if ((getTag(cpValue) == TagValues.CONSTANT_Methodref ||
                 getTag(cpValue) == TagValues.CONSTANT_Fieldref ||
                 getTag(cpValue) == TagValues.CONSTANT_InterfaceMethodref) &&
                !verifyReferences((ReferenceInfo) cpValue)) {
                throw new VerifyErrorExt(Messages.INVALID_CONSTANT_POOL + loop, false);
            } else if (getTag(cpValue) == TagValues.CONSTANT_String &&
                       !verifyStringInfo((StringInfo) cpValue)) {
                throw new VerifyErrorExt(Messages.INVALID_CONSTANT_POOL + loop, false);
            } else if (getTag(cpValue) == TagValues.CONSTANT_NameAndType &&
                       !verifyNameAndType((NameAndTypeInfo) cpValue)) {
                throw new VerifyErrorExt(Messages.INVALID_CONSTANT_POOL + loop, false);
            } else if (getTag(cpValue) == TagValues.CONSTANT_Utf8 &&
                       !verifyUTF8((UTF8Info) cpValue)) {
                throw new VerifyErrorExt(Messages.INVALID_CONSTANT_POOL + loop, false);
            } else if (getTag(cpValue) == TagValues.CONSTANT_Class &&
                       !verifyClassInfo((ClassInfo) cpValue)) {
                throw new VerifyErrorExt(Messages.INVALID_CONSTANT_POOL + loop, false);
            }
        }
        }
    }

    private int getTag(InfoBase base) throws Exception {
        return base.getTag().intValueUnsigned();
    }

    private InfoBase get(int index, int poolId) throws Exception {
        return (InfoBase) pOne.get(index, poolId);
    }

    private InfoBase get(Un index, int poolId) throws Exception {
        return get(index.intValueUnsigned(), poolId);
    }

    //step 1
    private boolean verifyReferences(ReferenceInfo rInfo) throws Exception {
        if (get(rInfo.getIndex(), TagValues.CONSTANT_Class) instanceof ClassInfo &&
            get(rInfo.getNameAndTypeIndex(), TagValues.CONSTANT_NameAndType)
            instanceof NameAndTypeInfo) {
            return true;
        }
        return false;
    }

    //step 2
    private boolean verifyStringInfo(StringInfo rInfo) throws Exception {
        if (get(rInfo.getIndex(), TagValues.CONSTANT_Utf8) instanceof UTF8Info) {
            return true;
        }
        return false;
    }

    //step 3
    private boolean verifyNameAndType(NameAndTypeInfo nAt) throws Exception {
        if (get(nAt.getIndex(), TagValues.CONSTANT_Utf8) instanceof UTF8Info &&
            get(nAt.getDescriptorIndex(), TagValues.CONSTANT_Utf8) instanceof UTF8Info) {
            return true;
        }
        return false;
    }

    //step 4
    private boolean verifyUTF8(UTF8Info utf8) throws Exception {
        if (utf8.getLength().intValueUnsigned() == utf8.getBytes().size()) {
            return true;
        }
        return false;
    }

    //step 5
    private boolean verifyClassInfo(ClassInfo classInfo) throws Exception {
        if (get(classInfo.getIndex(), TagValues.CONSTANT_Utf8) instanceof UTF8Info) {
            return true;
        }
        return false;
    }

}
