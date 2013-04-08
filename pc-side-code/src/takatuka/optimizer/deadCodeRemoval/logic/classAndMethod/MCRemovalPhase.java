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
package takatuka.optimizer.deadCodeRemoval.logic.classAndMethod;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.StartMeAbstract;
import takatuka.classreader.logic.constants.StatGroups;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.optimizer.deadCodeRemoval.logic.StartMeDCR;

/**
 * 
 * Description:
 * <p>
 *  - Remove all the class files which are not set as KeepStatic or KeepInstance
 *  - Remove all the function from remaining classfiles with keep = false. In case a class
 *    file is KeepStatic but not KeepInstance then also remove it ALL non-static
 *    functions.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class MCRemovalPhase {

    private static final MCRemovalPhase mcRemoval = new MCRemovalPhase();
    private double totalClassFilesRemoved = 0;
    private double totalMethodsRemoved = 0;
    private Oracle oracle = Oracle.getInstanceOf();
    private Vector listOfRemovedClasses = new Vector();
    private Vector listOfRemovedFunctions = new Vector();
    private LogHolder logHolder = LogHolder.getInstanceOf();
    private StatsHolder statHolder = StatsHolder.getInstanceOf();
    private double totalMethodsBeforeDCR = 0;

    private MCRemovalPhase() {
    }

    public static final MCRemovalPhase getIntanceOf() {
        return mcRemoval;
    }

    public Vector getListOfRemovedClasses() {
        return listOfRemovedClasses;
    }

    public Vector getListOfRemovedFunctions() {
        return listOfRemovedFunctions;
    }

    /**
     *  - Remove all the class files which are not set as KeepStatic or KeepInstance
     *  - Remove all the function from remaining classfiles with keep = false. In case a class
     *    file is KeepStatic but not KeepInstance then also remove it ALL non-static
     *    functions.
     */
    void execute() {
        MCMarkPhase.debugOutput("\n*********** REMOVAL PHASE ****************\n");
        ClassFileController cFileContr = ClassFileController.getInstanceOf();
        DCClassFile cFile = null;
        Vector<String> candidateInterfaceForKeeping = MCMarkPhase.getInstanceOf().getCandidateInterfaceForKeeping();
        double totalClassesBeforeDCR = cFileContr.getCurrentSize();
        for (int loop = 0; loop < cFileContr.getCurrentSize(); loop++) {
            cFile = (DCClassFile) cFileContr.get(loop);


            if ((!cFile.getInstanceAccess() && !cFile.getStaticAccess()
                    && !cFile.isKeepPerUserRequest())
                    || (shouldRemoveAsInterface(cFile, candidateInterfaceForKeeping))) {
                cFile = (DCClassFile) cFileContr.remove(loop);
                logHolder.addLog("removed cFile = " + cFile.getFullyQualifiedClassName(),
                        StartMeDCR.DEAD_CODE_REMOVAL_FILE, false);
                loop--;
                totalClassFilesRemoved++;
                listOfRemovedClasses.addElement(loop);
            } else {
                removeMethods(cFile);
            }
        }
        logHolder.addLog("Dead-Code-Removal: classes removed =" + totalClassFilesRemoved + ", "
                + "functions removed from classes kept  =" + totalMethodsRemoved, true);
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "total # of classes-removed", totalClassFilesRemoved);
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "total # of methods-removed",
                totalMethodsRemoved);
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "total-classes-removed", 
                 StartMeAbstract.roundDouble((totalClassFilesRemoved / totalClassesBeforeDCR) * 100, 2) + "%");
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "total-methods-removed",
                 StartMeAbstract.roundDouble((totalMethodsRemoved / totalMethodsBeforeDCR) * 100, 2) + "%");

    }

    private boolean shouldRemoveAsInterface(ClassFile cFile,
            Vector<String> candidateInterfaceForKeeping) {
        try {
            if (true) {
                return false;
                /**
                 * todo this function is disabled for the time being and should
                 * be enabled after more testing.
                 */
            }
            if (!cFile.getAccessFlags().isInterface()) {
                return false;
            }
            String myName = cFile.getFullyQualifiedClassName();
            if (candidateInterfaceForKeeping.contains(myName)) {
                return false;
            }
            Vector<ClassFile> superInterfacesVec = new Vector<ClassFile>();
            oracle.getAllSuperInterfaces(cFile, superInterfacesVec);
            Iterator<ClassFile> it = superInterfacesVec.iterator();
            while (it.hasNext()) {
                ClassFile superInterface = it.next();
                if (candidateInterfaceForKeeping.contains(superInterface.getFullyQualifiedClassName())) {
                    return false;
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return true;
    }

    private void methodRemovedLog() {
    }

    private void removeMethods(DCClassFile cFile) {
        MethodInfoController mContr = cFile.getMethodInfoController();
        DCMethodInfo method = null;
        //int removeMax = Integer.MAX_VALUE;
        //remove all methods other than keep set
        for (int loop = 0; loop < mContr.getCurrentSize(); loop++) {
            method = (DCMethodInfo) mContr.get(loop);
            totalMethodsBeforeDCR++;
            ClassFile.currentClassToWorkOn = cFile;
            MultiplePoolsFacade cp = cFile.getConstantPool();
            String methodName = oracle.methodOrFieldName(method, cp);
            String methodDesc = oracle.methodOrFieldDescription(method, cp);
            /*            if ((!method.isKeepPerUserRequest() ||
            (cFile.getStaticAccess() && !cFile.getInstanceAccess() &&
            !method.getAccessFlags().isStatic())) &&
            allowedRemoval(methodName, methodDesc)) {*/
            if (!(method.isFMKeepPerUserRequest() || method.isKeepWithMayBe())
                    && allowedRemoval(methodName, methodDesc)) {
                method = (DCMethodInfo) mContr.remove(loop);
                if (method != null) {
                    logHolder.addLog("removed method =" + oracle.getMethodOrFieldString(method),
                            StartMeDCR.DEAD_CODE_REMOVAL_FILE, false);
                    MethodRemovalInfo mRemovalInfo = new MethodRemovalInfo(
                            cFile.getFullyQualifiedClassName(), methodName, methodDesc);
                    this.listOfRemovedFunctions.add(mRemovalInfo);
                    loop--;
                    totalMethodsRemoved++;
                }
            }
        }

    }

    private boolean allowedRemoval(String methodName, String methodDescr) {
        if (/*methodName.equals("<init>") ||*/methodName.equals("<clinit>")) {
            return false;
        }
        return true;
    }

    private class MethodRemovalInfo {

        String classNameFQ = null;
        String methodName = null;
        String methodDesc = null;

        public MethodRemovalInfo(String classNameFQ, String methodName,
                String methodDesc) {
            this.classNameFQ = classNameFQ;
            this.methodDesc = methodDesc;
            this.methodName = methodName;
        }

        @Override
        public String toString() {
            return "Method remove =" + classNameFQ
                    + "->" + methodName + "(" + methodDesc + ")";
        }
    }
}
