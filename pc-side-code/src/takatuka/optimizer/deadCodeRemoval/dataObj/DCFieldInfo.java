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
package takatuka.optimizer.deadCodeRemoval.dataObj;

import java.util.*;
import takatuka.classreader.dataObjs.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam, Christoph Gonsior
 */
public class DCFieldInfo extends MethodInfo {

    public static final int FIELD_NOT_REFERRED = -1;
    public static final int FIELD_ONLY_PUT = 0;
    public static final int FIELD_GET = 1;
    /**
     * if fieldStatus is FIELD_NOT_REFERRED that means not referred (no put & get)
     * if fieldStatus is FIELD_ONLY_PUT that means only put
     * if fieldStatus is FIELD_GET that means get (may or may not be put)
     */
    private int fieldStatus = FIELD_NOT_REFERRED;
    private static HashMap<String, DCFieldInfo> putOnlyFieldsMap = new HashMap();
    private boolean keepMethod = false;
    private boolean keepMethodWithMayBe = false;

    public boolean isFMKeepPerUserRequest() {
        return keepMethod;
    }

    public void setKeepPerUserReqOrStatic() {
        this.keepMethod = true;
    }

    public void setKeepWithMayBe(boolean value) {
        this.keepMethodWithMayBe = value;
    }

    public boolean isKeepWithMayBe() {
        return this.keepMethodWithMayBe;
    }

    public DCFieldInfo(ClassFile myClass) {
        super(myClass);
    }

    public void init() {
        fieldStatus = FIELD_NOT_REFERRED;
        putOnlyFieldsMap.clear();
    }

    private void setFieldGet(int cpRefIndex, String fQClassName) {
        fieldStatus = FIELD_GET;
        putOnlyFieldsMap.remove(createKey(cpRefIndex, fQClassName));
    }

    /**
     * sets a field in the CP as only-put
     * 
     * @param cpRefIndex
     * @param fQClassName
     */
    private void setFieldPut(int cpRefIndex, String fQClassName) {
        if (fieldStatus != FIELD_GET) {
            fieldStatus = FIELD_ONLY_PUT;
            putOnlyFieldsMap.put(createKey(cpRefIndex, fQClassName), this);
        }
    }

    /** 
     * sets fieldstatus of a field in the CP as put-only or get
     * 
     * @param isPut
     * @param cpRefIndex
     * @param fQClassName
     */
    public void setFieldStatus(boolean isPut, int cpRefIndex, String fQClassName) {
        if (isPut) {
            setFieldPut(cpRefIndex, fQClassName);
        } else {
            setFieldGet(cpRefIndex, fQClassName);
        }
    }

    public int getFieldStatus() {
        return fieldStatus;
    }

    private static String createKey(int cpIndex, String fQClassName) {
        return cpIndex + ", " + fQClassName;
    }

    public static boolean isPutOnlyField(int cpIndex, String fullyQulifiedClassName) {
        return putOnlyFieldsMap.get(createKey(cpIndex, fullyQulifiedClassName)) != null;
    }
}
