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

import takatuka.optimizer.cpGlobalization.logic.util.Oracle;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.exception.*;


/**
 * <p>Title: </p>
 *
 * <p>Description:
 * Follows the same steps (except last one) as indicated at Section 4.9 at
 * http://java.sun.com/docs/books/jvms/second_edition/html/ClassFile.doc.html
 * 1. Check final classes are not subclassed and final methods are not overridden.
 * 2. Check that every class, but Object, has direct a super class
 * 3. Check all the static constraints for Constant Pool
 * 4. finally check all referenceInfos. See they have valid names, classes and descriptor.
 * </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class Pass2 {
    private static final Pass2 pass2 = new Pass2();
    private static final String OBJECT_CLASS = "java/lang/Object";
    
    
    private Pass2() {
        super();
    }

    public static Pass2 getInstanceOf() {
        return pass2;
    }

    public void execute() throws Exception {

        ClassFileController cont = ClassFileController.getInstanceOf();
        if (cont.getCurrentSize() == 0) {
            return;
        }
        ClassFile file = null;
        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            file = (ClassFile) cont.get(loop);
            ClassFile.currentClassToWorkOn = file;
            //to check each class have super class and it is also non final.
            checkAllButObjectHaveANonFinalSuperClass(file);

            //to check no one extend a final method
            checkFinalMethods(file);

        }

        //following will check constant pool static constraints as well as references constraints.
        ConstantPoolStaticConstraints.getInstanceOf().execute();
    }

    /**
     * Check if a final method is overridden. Follows the following step.
     * For a given ClassFile go to each method
     * -- For a given method check that if the same method (with exactly same signatures)
     *    is present in its superclasses and has
     *    final flag set. If so then return false. Otherwise return true.
     * @param file ClassFile
     * @return boolean
     */
    protected void checkFinalMethods(ClassFile file) throws Exception {
        MethodInfoController cont = file.getMethodInfoController();
        MethodInfo info = null;
        ClassFile superClass = null;
        for (int loop = 0; loop < cont.getCurrentSize(); loop++) {
            info = (MethodInfo) cont.get(loop);
            //go to super for finding the method
            superClass = Oracle.getInstanceOf().getClass(file.getSuperClass(), file.getConstantPool());
            if (checkSuperMethodFinal(info, superClass)) {
                throw new VerifyErrorExt(Messages.SUPER_FINAL_METHOD);
            }
        }

    }


    private boolean checkSuperMethodFinal(MethodInfo method, ClassFile file) throws
            Exception {
        if (file == null) { //not loaded?
            return false;
        }
        MethodInfo superMethod = file.hasMethod(method.getKey());
        if (superMethod != null) { //method found
            if (superMethod.getAccessFlags().isFinal()) {
                return true;
            } else {
                return false;
            }
        }
        // method Not found. Hence check super class.
        if (superMethod == null && file.getSuperClass() != null) {
            file = Oracle.getInstanceOf().getClass(file.getSuperClass(), file.getConstantPool());
            return checkSuperMethodFinal(method, file);
        } else { //no super exist and method not found...
            return false;
        }
    }


    /**
     * 1. check that each class has a super class (other than object)
     * 2. check that each super class is not final.
     * @return boolean
     */
    protected void checkAllButObjectHaveANonFinalSuperClass(ClassFile file) throws
            Exception {
        ClassFile superClass = null;
        //check each class
        if (file.getSuperClass() == null &&
            !(file.getFullyQualifiedClassName().equals(OBJECT_CLASS))) {
            throw new VerifyErrorExt(Messages.NO_SUPER_CLASS);
        }

        superClass = Oracle.getInstanceOf().getClass(file.getSuperClass(), file.getConstantPool());
        //not null is checked to make sure it is loaded.
        if (superClass != null && superClass.getAccessFlags().isFinal()) {
            throw new VerifyErrorExt(Messages.FINAL_SUPERCLASS);
        }

    }


}
