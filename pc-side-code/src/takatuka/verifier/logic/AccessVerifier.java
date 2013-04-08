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
 * <p>Description: </p>
 ** Part of Verification phase-4
 * Checks that the currently executing method has access to the referenced method or field.
 * -- check private, protected, public and package-private. here.
 * -- Per documentation (section 5.4.4) http://java.sun.com/docs/books/jvms/second_edition/html/ConstantPool.doc.html#75929
 *
 * Definations
 *  C : interface or class
 *  D : interface or class
 *  R : field or method
 *
 * --- D can refer/assess C
 * 1. C is public
 * 2. C/D share run-time package
 *
 * --- C can refer/access C.R
 * 0. If C can refer/access C (as defined above) and
 * 1. if C == D
 * 2. R is public
 * 3. R is protected and D is subclass of C
 * 4. R is protected or package-private and C, D both share runtime package.
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class AccessVerifier {
    private static final AccessVerifier accessVerifier = new AccessVerifier();

    private AccessVerifier() {
        super();
    }

    public static final AccessVerifier getInstanceOf() {
        return accessVerifier;
    }

    public void execute(ClassFile classD, ClassFile classC, FieldInfo fieldR) {
        try {
            if (!checkClassAccess(classD, classC)) {
                throw new VerifyErrorExt(Messages.INVALID_CLASS_ACCESS + ", " +
                                         classD.getFullyQualifiedClassName() +
                                         " cannot access " +
                                         classC.getFullyQualifiedClassName());
            } else if (!checkFieldMethodAccess(classD, classC, fieldR)) {
                throw new VerifyErrorExt(Messages.INVALID_CLASS_ACCESS + ", " +
                                         classD.getFullyQualifiedClassName() +
                                         " cannot access a field/method from class" +
                                         classC.getFullyQualifiedClassName());

            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    /**
     *  --- C can refer/access D.R
     * 1. if C == D
     * 2. R is public
     * 3. R is protected and D is subclass of C
     * 4. R is protected or package-private and C, D both share runtime package.

     * @param classD ClassFile
     * @param classC ClassFile
     * @param fieldR FieldInfo
     * @return boolean
     */
    public boolean checkFieldMethodAccess(ClassFile classD, ClassFile classC,
                                          FieldInfo fieldR) throws Exception {
        //if C == D
        if (classD.equals(classC)) {
            return true;
        }
        AccessFlags rAccess = fieldR.getAccessFlags();

        // R is public -- OR -- R is protected and D is subclass of C -- Or --
        // R is protected or package-private and C, D both share runtime package.
        if (rAccess.isPublic() ||
            (rAccess.isProtected() && Oracle.getInstanceOf().isSubClass(classD, classC))
            || ((rAccess.isProtected() || rAccess.isPackagePrivate())
                && samePackage(classD, classC))) {
            return true;
        }
        return false;
    }

    private boolean samePackage(ClassFile classD, ClassFile classC) throws
            Exception {
        String classDPackage = Oracle.getInstanceOf().getPackage(classD);
        String classCPackage = Oracle.getInstanceOf().getPackage(classC);
        return classCPackage.equals(classDPackage);
    }

    /**
     *  --- D can refer/assess C
     * 1. C is public
     * 2. C/D share run-time package
     *
     * @param classD ClassFile
     * @param classC ClassFile
     * @return boolean
     * @throws Exception
     */
    public boolean checkClassAccess(ClassFile classD, ClassFile classC) throws
            Exception {
        //C is public or C/D share run-time package
        if (classC.getAccessFlags().isPublic() || samePackage(classD, classC)) {
            return true;
        }
        return false; //not allowed access
    }
}
