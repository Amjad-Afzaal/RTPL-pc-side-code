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
package takatuka.offlineGC.DFA.logic;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.*;
import takatuka.offlineGC.DFA.logic.factory.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.optimizer.VSS.logic.DFA.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCMiscInstrs extends SSMiscInstrs {

    private static final GCMiscInstrs myObj = new GCMiscInstrs();
    private static final Oracle oracle = Oracle.getInstanceOf();

    protected GCMiscInstrs() {
    }

    public static GCMiscInstrs getInstanceOf(Vector<Long> nextPossibleInstructionsIds,
            OperandStack stack, MethodInfo currentMethodStatic,
            LocalVariables localVar, int currentPC, HashSet<Type> returnTypes) {
        init(nextPossibleInstructionsIds, stack, currentMethodStatic,
                localVar, currentPC, returnTypes);
        return myObj;
    }

    /**
     *  ..., objectref  ==> ..., objectref
     * The normal check cast does not change the value on the stack. However,
     * the checkcast of GC will change stack value. It is because a field could
     * store multiple types of value and all those values are get from a getfield or
     * getstatic instruction. The checkcast filter those reference values
     * (at the top of stack) that does not pass the check.
     *
     * (copied from JVM specification http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc2.html)
     *
     * The following rules are used to determine whether an objectref that is not null
     * can be cast to the resolved type: if S is the class of the object referred to by
     * objectref and T is the resolved class, array, or interface type, checkcast determines
     * whether objectref can be cast to type T as follows:
     * 
     * If S is an ordinary (nonarray) class, then:
     * o If T is a class type, then S must be the same class (§2.8.1) as T, or a subclass of T.
     * o If T is an interface type, then S must implement (§2.13) interface T.
     *
     * If S is an interface type, then:
     * o If T is a class type, then T must be Object (§2.4.7).
     * o If T is an interface type, then T must be the same interface as S or a superinterface of S (§2.13.2).
     *
     * If S is a class representing the array type SC[], that is, an array of components of type SC, then:
     * o If T is a class type, then T must be Object (§2.4.7).
     * o If T is an array type TC[], that is, an array of components of type TC, then one of the following must be true:
     *  + TC and SC are the same primitive type (§2.4.1).
     *  + TC and SC are reference types (§2.4.6), and type SC can be cast to TC by recursive application of these rules.
     * o If T is an interface type, T must be one of the interfaces implemented by arrays (§2.15).
     */
    @Override
    protected void checkCastInstruction(VerificationInstruction inst) {
        GCType type = (GCType) stack.peep(); //S is in it.
        if (!type.isReference()) {
            throw new VerifyError(Messages.STACK_INVALID + " " + inst);
        }
        Un cpIndex = inst.getOperandsData();
        String name = oracle.getClassInfoName(cpIndex);
        HashSet<TTReference> allSRefs = type.getReferences(); //all S are in it.
        if (!name.startsWith("[")) {
            ClassFile TcFile = oracle.getClass(cpIndex, pOne); //T is in it.
            convertStackTop(allSRefs, TcFile);
        }
        type.deleteAllRef();
        type.addReferences(allSRefs);

        /**
         * @todo check are missing when t and s both are arrays.
         */
    }

    private void convertStackTop(HashSet<TTReference> allSRefs, ClassFile TclassFile) {
        NewInstrIdFactory newIdFactory = NewInstrIdFactory.getInstanceOf();
        Iterator<TTReference> it = allSRefs.iterator();
        HashSet<TTReference> toRemove = new HashSet<TTReference>();
        try {
            while (it.hasNext()) {
                TTReference sRef = it.next();
                if (sRef.isNullReference()) {
                    toRemove.add(sRef);
                    continue;
                }
                int newId = sRef.getNewId();
                boolean isPrimitveArray = newIdFactory.isPrimitiveArrayNewId(newId);
                boolean isRefsArray = newIdFactory.isRefArrayNewId(newId);
                int thisPointer = sRef.getClassThisPointer();
                ClassFile sClassFile = oracle.getClass(thisPointer, pOne);
                /**
                 * S is not an array
                 */
                if (!isRefsArray && !isPrimitveArray) {
                    /**
                     *  S is also not an interface.
                     */
                    if (!sClassFile.getAccessFlags().isInterface()) {

                        if (!checkCastForNormalClass(sClassFile, TclassFile)) {
                            toRemove.add(sRef);
                        }
                    } else {
                        /**
                         * S is not an array or class but interface
                         */
                        if (!checkCastWhenSisAnInterface(sClassFile, sClassFile)) {
                            toRemove.add(sRef);
                        }
                    }
                } else if (!checkCastWhenSisAnArrayAndTisNotAnArray(sClassFile, sClassFile)){
                    toRemove.add(sRef);
                }
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        allSRefs.removeAll(toRemove);
    }

    /**
     * If S is an ordinary (nonarray, non-interface) class, then:
     * o If T is a class type, then S must be the same class (§2.8.1) as T, or a subclass of T.
     * o If T is an interface type, then S must implement (§2.13) interface T.
     *
     * @param sClassFile
     * @param tClassFile
     * @return
     * @throws Exception
     */
    private boolean checkCastForNormalClass(ClassFile sClassFile, ClassFile tClassFile) throws Exception {
        if (tClassFile.getAccessFlags().isInterface()) {
            Vector<ClassFile> interfacesImplemented = oracle.getAllInterfacesImpled(sClassFile);
            if (!interfacesImplemented.contains(tClassFile)) {
                return false;
            }
        } else {
            if (!sClassFile.equals(tClassFile) && !oracle.isSubClass(sClassFile, tClassFile)) {
                return false;
            }
        }
        return true;
    }

    /**
     * If S is an interface type, then:
     * o If T is a class type, then T must be Object (§2.4.7).
     * o If T is an interface type, then T must be the same interface as S or a superinterface of S (§2.13.2).
     *
     *
     * @param sClassFile
     * @param tClassFile
     * @return
     * @throws Exception
     */
    private boolean checkCastWhenSisAnInterface(ClassFile sClassFile, ClassFile tClassFile) throws Exception {
        if (!tClassFile.getAccessFlags().isInterface()) {
            if (!tClassFile.getFullyQualifiedClassName().equals("java/lang/Object")) {
                return false;
            }
        } else {
            if (sClassFile.getThisClass().equals(tClassFile.getThisClass())) {
                return true;
            }
            Vector<ClassFile> superInterfaceOfS = new Vector<ClassFile>();
            oracle.getAllSuperInterfaces(sClassFile, superInterfaceOfS);
            if (!superInterfaceOfS.contains(tClassFile)) {
                return false;
            }
        }
        return true;
    }

    /**
     * If S is a class representing the array type SC[], that is, an array of components of type SC, then:
     * o If T is a class type, then T must be Object (§2.4.7).
     * o If T is an array type TC[], that is, an array of components of type TC, then one of the following must be true:
     *  + TC and SC are the same primitive type (§2.4.1).
     *  + TC and SC are reference types (§2.4.6), and type SC can be cast to TC by recursive application of these rules.
     * o If T is an interface type, T must be one of the interfaces implemented by arrays (§2.15).
     *
     * @param sClassFile
     * @param tClassFile
     * @return
     */
    private boolean checkCastWhenSisAnArrayAndTisNotAnArray(ClassFile sClassFile, ClassFile tClassFile) {
        /**
         * We know that S is an array and T is not an array here.
         */
        if (!tClassFile.getFullyQualifiedClassName().equals("java/lang/Object")) {
            return false;
        }
        return true;
    }
}
