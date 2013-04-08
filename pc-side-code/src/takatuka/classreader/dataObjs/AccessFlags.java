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
package takatuka.classreader.dataObjs;

import java.nio.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class AccessFlags extends Un implements AccessFlagValues {

    public AccessFlags() throws Exception {
        super();
    }

    /**
     *
     * @param bytes byte[]
     * @throws Exception
     */
    public AccessFlags(byte bytes[]) throws Exception {
        setData(bytes);
    }

    public AccessFlags(Un un) throws Exception {
        setData(un.getData());
    }

    @Override
    public void setData(byte data[]) throws Exception {
        if (data.length > 2) {
            Miscellaneous.printlnErr("Access flag cannot have more than two bytes long");
        }
        super.setData(data);
    }

    /**
     *
     * @param input long
     * @return byte[]
     */
    private byte[] intToBytes(int input) {
        return ByteBuffer.allocate(4).putInt(input).array();
    }

    public boolean isACCSuper() {
        return isFlagTrue(AccessFlagValues.ACC_SUPER);
    }

    /**
     * return true if flag is Package-Private
     * PackagePrivate : neither public nor protected nor private
     * @return boolean
     */
    public boolean isPackagePrivate() {
        if (!isPublic() && !isProtected() && !isPrivate()) {
            return true;
        }
        return false;
    }

    /**
     * checks if the flag is interface
     * @return boolean
     */
    public boolean isInterface() {
        return isFlagTrue(AccessFlagValues.ACC_INTERFACE);
    }

    /**
     * checks if the flag is public
     * @return boolean
     */
    public boolean isPublic() {
        return isFlagTrue(AccessFlagValues.ACC_PUBLIC);
    }

    public boolean isSync() {
        return isFlagTrue(AccessFlagValues.ACC_SYNCHRONIZED);
    }

    /**
     * checks if the flag is protected
     * @return boolean
     */
    public boolean isProtected() {
        return isFlagTrue(AccessFlagValues.ACC_PROTECTED);
    }

    /**
     * checks if the flag is private
     * @return boolean
     */
    public boolean isPrivate() {
        return isFlagTrue(AccessFlagValues.ACC_PRIVATE);
    }

    /**
     * checks if the flag is static
     * @return boolean
     */
    public boolean isStatic() {
        return isFlagTrue(AccessFlagValues.ACC_STATIC);
    }

    /**
     * generate value for a AccessField
     * 
     * @param isStatic
     * @param isNative
     * @param isACCSuper
     * @return
     */
    public static Un createAccessFlagsValue(boolean isStatic, boolean isNative,
            boolean isACCSuper) {
        FactoryFacade factory = FactoryPlaceholder.getInstanceOf().getFactory();
        Un ret = null;
        int flags = 0;
        if (isStatic) {
            flags = flags | ACC_STATIC;
        }
        if (isNative) {
            flags = flags | ACC_NATIVE;
        }
        if (isACCSuper) {
            flags = flags | ACC_SUPER;
        }
        try {
            ret = factory.createUn(flags).trim(2);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    /**
     * 
     * @param type
     * @return
     */
    public boolean isFlagTrue(int type) {
        boolean ret = false;
        try {
            int flag = intValueUnsigned();
            if ((flag & type) == type) {
                ret = true;
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;

    }

    /**
     * checks if the flag is abstract
     * @return boolean
     */
    public boolean isAbstract() {
        return isFlagTrue(AccessFlagValues.ACC_ABSTRACT);
    }

    /**
     * checks access type is final
     * @return boolean
     */
    public boolean isFinal() {
        return isFlagTrue(AccessFlagValues.ACC_FINAL);
    }

    /**
     * checks access type is native
     * @return boolean
     */
    public boolean isNative() {
        return isFlagTrue(AccessFlagValues.ACC_NATIVE);
    }

    /**
     * return true if access is either native or abstract
     * @return boolean
     */
    public boolean isNativeOrAbstract() {
        if (isNative() || isAbstract()) {
            return true;
        }
        return false;
    }

    /**
     * we have named a method  which is non-static, non-native and non-abstract as virtual.
     * @return boolean
     */
    public boolean isVirtual() {
        if (!isNative() && !isAbstract() && !isStatic()) {
            return true;
        }
        return false;
    }
}
