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
package takatuka.verifier.dataObjs.nativeMethods;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.file.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * It contains list of methods that are called by any given native method.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class NativeMethodCallController {

    private static final NativeMethodCallController myObj = new NativeMethodCallController();
    private HashMap<NativeMethodInfo, Vector<NativeMethodInfo>> record =
            new HashMap<NativeMethodInfo, Vector<NativeMethodInfo>>();
    private static PropertyReader propReader = PropertyReader.getInstanceOf();
    private static final String NATIVE_METHOD_PROPERTY = "native-method-calls.properties";
    private static final Properties nativeMethodProp = propReader.getProperties(NATIVE_METHOD_PROPERTY);
    private static boolean isPopulated = false;

    /**
     * constructor is private
     */
    private NativeMethodCallController() {
    }

    /**
     * 
     * @return
     */
    public static NativeMethodCallController getInstanceOf() {
        myObj.populate();
        return myObj;
    }

    private NativeMethodInfo createNativeMethod(String input) {
        String className = input.substring(0, input.lastIndexOf("."));
        String methodAndDesc = input.substring(input.lastIndexOf(".") + 1);
        return new NativeMethodInfo(className, methodAndDesc);
    }

    private void populate() {
        if (isPopulated) {
            return;
        }
        isPopulated = true;
        Set keys = nativeMethodProp.keySet();
        Iterator<String> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            NativeMethodInfo keyNMI = createNativeMethod(key);
            String value = nativeMethodProp.getProperty(key);
            Vector<NativeMethodInfo> nMInfoValues = populateAllValues(value);
            record.put(keyNMI, nMInfoValues);
        }
    }

    private Vector<NativeMethodInfo> populateAllValues(String propertyValue) {
        Vector ret = new Vector();
        String[] nativeMethodInfoList = propertyValue.split(",");
        for (int loop = 0; loop < nativeMethodInfoList.length; loop++) {
            String value = nativeMethodInfoList[loop].trim();
            if (value.length() != 0) {
                ret.addElement(createNativeMethod(value));
            }
        }
        return ret;
    }

    public Vector<NativeMethodInfo> getMethodCalledByANativeMethod(String className, String methodName) {
        NativeMethodInfo key = new NativeMethodInfo(className, methodName);
        return record.get(key);
    }

    public Vector<MethodInfo> getMethodCalledByANativeMethod(MethodInfo nativeMethod,
            ClassFile currentClass, int opcode) {
        /**
         * That is how it works.
         * 1. Get the nativeMethodInfo set.
         * 2. check if the call was invoke
         */
        return null;
    }
    /**
     * 
     * @param className
     * @param methodName
     * @return
     */
    //public Vector<MethodInfo> getMethodCalledByANativeMethod(String className, String methodName) {
      //  return null;
    //}
}
