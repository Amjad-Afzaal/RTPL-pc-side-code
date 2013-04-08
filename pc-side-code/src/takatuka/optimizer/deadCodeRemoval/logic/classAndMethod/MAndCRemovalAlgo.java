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

import takatuka.optimizer.deadCodeRemoval.dataObj.xml.*;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.optimizer.deadCodeRemoval.logic.xml.*;

/**
 * 
 * Description:
 * <p>
 * 
 * This is the only public class in the whole package. It is interface to
 *  the outer world. It calls other classes of this package.
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class MAndCRemovalAlgo {

    private static final MAndCRemovalAlgo mAndCAlgo = new MAndCRemovalAlgo();
    private Oracle oracle = Oracle.getInstanceOf();
    private HashMap<String, DCClassFile> startedFromClasses = new HashMap();
    private static final String MAIN_METHOD_NAME = "main";
    private static final String MAIN_METHOD_DESC = "([Ljava/lang/String;)V";
    private String mainClassName = null;
    private static final MCMarkPhase markPhase = MCMarkPhase.getInstanceOf();
    private static HashSet<MethodExamineListEntry> startingFunctions = new HashSet<MethodExamineListEntry>();

    private MAndCRemovalAlgo() {
        //no one creates me but me.
    }

    static void Debug_DCR(String str) {
        //Miscellaneous.println(str);
    }

    public static MAndCRemovalAlgo getInstanceOf() {
        return mAndCAlgo;
    }

    public HashSet<MethodExamineListEntry> getAllMethodToBeExamine() {
        return startingFunctions;
    }

    private void startFromClass(String fullyQualifiedName) {
        DCClassFile cFile = (DCClassFile) oracle.getClass(fullyQualifiedName);
        if (cFile == null) {
            Miscellaneous.printlnErr("#201 Cannot find class file =" + fullyQualifiedName);
            Miscellaneous.exit();
        }
        startFromClass(cFile);
    }

    public void setMainClassName(String mainClassName) {
        if (mainClassName == null) {
            this.mainClassName = null;
            return;
        }
        mainClassName = mainClassName.replace(".", "/");
        this.mainClassName = mainClassName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    private void startFromClass(DCClassFile cFile) {
        if (startedFromClasses.get(cFile.getFullyQualifiedClassName()) != null) {
            return; //already started from it
        }
//        Debug_DCR("Starting from cFile = " + cFile.getFullyQualifiedClassName());
        startedFromClasses.put(cFile.getFullyQualifiedClassName(), cFile);
        MethodInfoController mCont = cFile.getMethodInfoController();
        MultiplePoolsFacade cp = cFile.getConstantPool();
        for (int loop = 0; loop < mCont.getCurrentSize(); loop++) {
            MethodInfo mInfo = (MethodInfo) mCont.get(loop);
            ClassFile.currentClassToWorkOn = cFile;
            MCMarkPhase.setKeepPerUserRequest(cFile);
            startFromMethod(cFile.getFullyQualifiedClassName(),
                    oracle.methodOrFieldName(mInfo, cp),
                    oracle.methodOrFieldDescription(mInfo, cp));
        }
    }

    private void startFromMainClass(String fullyQualifiedName) {
        if (fullyQualifiedName == null) {
            return;
        }
        startFromMethod(fullyQualifiedName, MAIN_METHOD_NAME, MAIN_METHOD_DESC);
    }

    private void startFromMethod(String classFullyQualifiedName, String methodName,
            String methodDesc) {
        DCClassFile cFile = (DCClassFile) oracle.getClass(classFullyQualifiedName);
        ExamineList examineList = ExamineList.getInstanceOf();
        DCMethodInfo addedMethod = null;
        if (cFile == null) {
            LogHolder.getInstanceOf().addLog("Deadcode removal cannot find"
                    + " class-name=" + classFullyQualifiedName + ", ignoring... ", true);
            Miscellaneous.printlnErr("Deadcode removal cannot find"
                    + " class-name=" + classFullyQualifiedName + ", ignoring... ");
            Miscellaneous.exit();
            return;
        }
        ClassFile.currentClassToWorkOn = cFile;
        MCMarkPhase.setKeepPerUserRequest(cFile);
        //Todo following code need a review.
        if (classFullyQualifiedName.endsWith("Thread") && methodName.endsWith("run")) {
            markPhase.addMethodInExamineList(classFullyQualifiedName,
                    methodName, methodDesc, true, true);
            addedMethod = examineList.getLastEntry().getMethodInfo();
        } else {
            markPhase.addMethodInExamineList(classFullyQualifiedName,
                    methodName, methodDesc, false, true);
            addedMethod = examineList.getLastEntry().getMethodInfo();
        }
        if (addedMethod != null) {
            addedMethod.setKeepPerUserReqOrStatic();
        }
    }

    private void startFromXMLEntries() {
        startFromMainClass(mainClassName);
        startFromXMLSpecifiedPackages();
        startFromXMLSpecifiedClasses();
    }

    private void startFromXMLSpecifiedPackages() {
        Vector classes = DCXMLUtil.getInstanceOf().getAllClassesFromPackageXML();
        Iterator<DCClassFile> dcIt = classes.iterator();
        while (dcIt.hasNext()) {
            DCClassFile cFile = dcIt.next();
            startFromClass(cFile);
        }
    }

    private void startFromXMLSpecifiedClasses() {
        Vector<ClassFileXML> classFileXMLVec = ReadXMLForKeepReferences.getInstanceOf().getClassFileXML();
        Iterator<ClassFileXML> dcIt = classFileXMLVec.iterator();
        while (dcIt.hasNext()) {
            ClassFileXML xmlClass = dcIt.next();
            // Debug_DCR("-----------> " + xmlClass);
            DCClassFile cFile = (DCClassFile) oracle.getClass(xmlClass.getName());
            if (cFile == null) {
                Miscellaneous.println("Error: in XML, cannot find class-file " + xmlClass);
                Miscellaneous.exit();
            }
            MCMarkPhase.debugOutput("Keeping class per user request " + cFile.getFullyQualifiedClassName());
            MCMarkPhase.setKeepPerUserRequest(cFile);
            MCMarkPhase.getInstanceOf().keepMeAndMySuperClassesAndMySuperInterfaces(cFile);
            //todo here
            if (cFile != null) {
                if (xmlClass.isIncludeAllFunctions()) {
                    startFromClass(cFile);
                }
            }
            if (!xmlClass.isIncludeAllFunctions() && xmlClass.getFunctions().size() > 0) {
                startFromXMLSpecifiedMethods(xmlClass);
            }

        }
    }

    private void startFromXMLSpecifiedMethods(ClassFileXML xmlClass) {
        Vector vecFunField = xmlClass.getFunctions();
        if (vecFunField.isEmpty()) {
            return;
        }
        Iterator<FunctionAndFieldXML> funIt = vecFunField.iterator();
        while (funIt.hasNext()) {
            FunctionAndFieldXML funFieldXML = funIt.next();
            startFromMethod(xmlClass.getName(), funFieldXML.getName(), funFieldXML.getDescription());
        }
    }

    /**
     * Hope you have already mentioned few starting points
     * Now call me to execute actual algorithm
     */
    public void execute() {
        if (mainClassName == null) {
            return;
        }
        startFromXMLEntries();
        startingFunctions.addAll(ExamineList.getInstanceOf().getAllEntries());
        MCMarkPhase.getInstanceOf().execute();
        MCRemovalPhase.getIntanceOf().execute();
    }
}
