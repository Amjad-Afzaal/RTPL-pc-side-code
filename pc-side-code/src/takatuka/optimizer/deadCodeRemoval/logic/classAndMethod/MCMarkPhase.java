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
import takatuka.classreader.dataObjs.attribute.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.dataObj.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.*;
import takatuka.optimizer.deadCodeRemoval.logic.StartMeDCR;

/**
 *
 * Description:
 * <p>
 * Defines:
 * ---------
 * method: name & description
 * examine list: method & class-fully-qualified name
 *
 * The current algorithm works as follows.
 * -----------------------------------------
 *
 *  - remove method x from the examine list
 *  - set x as keep
 *  - If method is not static then each method in sub-class
 *    and super-class of x with same signatures are put
 *    in the examine list.
 * -  If class of x is accessed for first time then also add its cinit method in examine list
 *  - traverse x bytecode
 *  --- If found a NEW set class corresponding to it as foundInstance
 *  ----- For such class also keep its equals, hashCode, wait, notify, notifyAll methods.
 *  --- If found a invokeVirtual or invokespecial then add the method in
 *      the examine list
 *  --- If found a invokeStatic then add the method in examine list and set
 *      corresponding class as keepStatic
 *  --- If found a getStatic then add the class as keepStatic
 *  - if examine list is not empty then Go to the start of algorithm
 *  - else
 *    Call class ActualRemoval
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
class MCMarkPhase {

    private static final String STATIC_INIT_NAME = "<clinit>";
    private static final String VOID_DESC = "()V";
    private static final String OBJECT_CLASS = "java/lang/Object";
    private static final ExamineList examineList = ExamineList.getInstanceOf();
    private static final Oracle oracle = Oracle.getInstanceOf();
    private static final MCMarkPhase mcmAlgo = new MCMarkPhase();
    private HashMap<String, HashSet> cacheSubClasses = new HashMap();
    private HashMap<ClassFile, Vector> cacheSuperClasses = new HashMap();
    private Vector<String> intefaceCandidate = new Vector<String>();
    private static boolean printDebugInfo = false;
    private static Vector<String> notFoundClassFileNames = new Vector<String>();
    private static HashSet<MethodExamineListEntry> methodKept = new HashSet<MethodExamineListEntry>();
    private static LogHolder logHolder = LogHolder.getInstanceOf();
    private static HashSet<MethodExamineListEntry> toExamineIfTheirClassisKept = new HashSet<MethodExamineListEntry>();

    private MCMarkPhase() {
    }

    public static final MCMarkPhase getInstanceOf() {
        return mcmAlgo;
    }

    public static void debugOutput(String str) {
        logHolder.addLog(str, StartMeDCR.DEAD_CODE_REMOVAL_FILE, printDebugInfo);
    }

    public Vector<String> getCandidateInterfaceForKeeping() {
        return intefaceCandidate;
    }

    public boolean addMethodInExamineList(String fullyQualifiedClassName,
            String methodName, String methodDescription, boolean isGetFromByteCodeTraverse, boolean isUserRequested) {
        return examineList.add(new MethodExamineListEntry(fullyQualifiedClassName,
                methodName, methodDescription, isGetFromByteCodeTraverse, isUserRequested));
    }

    public HashSet<DCClassFile> getAllSubClasses(DCClassFile cFile) throws Exception {
        return getAllSubClasses(cFile.getFullyQualifiedClassName());
    }

    public HashSet<DCClassFile> getAllSubClasses(String fQClassName) throws Exception {
        HashSet ret = cacheSubClasses.get(fQClassName);
        if (ret != null) {
            return ret;
        }
        ret = oracle.getAllSubClasses(fQClassName);
        cacheSubClasses.put(fQClassName, (HashSet) ret.clone());
        return ret;
    }

    private void getAllSuperClasses(ClassFile cFile, Vector retVector) throws Exception {
        Vector<ClassFile> ret = cacheSuperClasses.get(cFile);
        if (ret != null) {
            retVector.addAll(ret);
            return;
        }
        ret = new Vector();
        ClassFile.currentClassToWorkOn = cFile;
        oracle.getAllSuperClasses(cFile, ret);
        cacheSuperClasses.put(cFile, (Vector) ret.clone());
        retVector.addAll((Vector) ret.clone());
        //for fast caching
        while (ret.size() > 0) {
            cacheSuperClasses.put(ret.remove(0), (Vector) ret.clone());
        }
        //printSuperClasses();
    }

    /**
     *
     * @return
     */
    public HashSet<MethodExamineListEntry> getAllMethodKept() {
        return methodKept;
    }

    /**
     *
     */
    public void clearMethodKeptRecord() {
        methodKept.clear();
    }

    public void execute() {
        if (passOne()) {
            /**
             * PassTwo is called only if some object are examine inside the
             * PassOne loop. 
             */
            PassTwo.getInstanceOf().execute();
        }
    }

    /**
     * We start from here
     *
     * @return
     */
    private boolean passOne() {
        DCMethodInfo mInfo = null;
        boolean insideTheLoop = false;
        while (examineList.size() != 0) {
            insideTheLoop = true;
            //Miscellaneous.println("Current size of examine list = " + examineList.size());
            //removed method from examine list
            MethodExamineListEntry methodX = examineList.remove();
            if (methodX == null) {
                continue;
            }
            if (methodX.toString().contains("IRead,")) {
                //System.out.println("stop here "+methodX.getMethodClass().getInstanceAccess()+", "+
                ///methodX.getMethodClass().getStaticAccess());
            }
            debugOutput("examining =" + methodX);
            mInfo = (DCMethodInfo) methodX.getMethodInfo();
            //set method as keep
            if (mInfo != null) {
                if (methodX.getMethodName().equals("<init>")
                        && methodX.getMethodClass().getFullyQualifiedClassName().contains("takatuka/vm/VM$1")) {
                    //Miscellaneous.println("Stop here 79");
                    //printDebugInfo = true;
                }

                if (!methodX.isGotMethodFromBytecodeTraverse()) {
                    mInfo.setKeepPerUserReqOrStatic();
                } else {
                    mInfo.setKeepWithMayBe(true);
                }
                methodKept.add(methodX);
                debugOutput("\n\n\n 1: method is kept =" + methodX.getMethodName() + ", "
                        + methodX.getMethodFullyQualifiedClassName() + " with MayBe="
                        + mInfo.isKeepWithMayBe() + ", method className " + methodX.getMethodFullyQualifiedClassName());
            } else {
                debugOutput("\n\n");
            }

            //add more methods in examine list
            updateExamineList(methodX);

            //traverse bytecode
            debugOutput("****** Start travesing method " + methodX);
            traverseBytecode(methodX);
            debugOutput("****** End travesing method " + methodX);
            keepExceptionThrownByMethod(methodX);
        }
        checkImplemenationsMethodToBeKept();
        return insideTheLoop;
    }

    private void checkImplemenationsMethodToBeKept() {
        if (toExamineIfTheirClassisKept.isEmpty()) {
            return;
        }
        Iterator<MethodExamineListEntry> it = toExamineIfTheirClassisKept.iterator();
        while (it.hasNext()) {
            MethodExamineListEntry entry = it.next();
            if (entry.getMethodClass().getInstanceAccess()) {
                debugOutput(entry + "");
                examineList.add(entry);
            }
        }
        toExamineIfTheirClassisKept.clear();
        passOne();
    }

    private void keepExceptionThrownByMethod(MethodExamineListEntry methodX) {
        try {
            DCMethodInfo method = methodX.getMethodInfo();
            if (method == null) {
                return; //cannot traverse it.
            }
            CodeAtt codeAtt = method.getCodeAtt();
            if (codeAtt == null) {
                return;
            }
            Vector<ExceptionTableEntry> exceptionTableEntries = codeAtt.getExceptions();
            Iterator<ExceptionTableEntry> it = exceptionTableEntries.iterator();
            while (it.hasNext()) {
                ExceptionTableEntry entry = it.next();
                int exceptionClass = entry.getCatchType().intValueUnsigned();
                if (exceptionClass == 0) {
                    continue;
                }
                DCClassFile cFile = (DCClassFile) getClass(exceptionClass,
                        methodX.getMethodClass());
                cFile.setInstanceAccess(true);
                DCMethodInfo initMethod = (DCMethodInfo) oracle.getInstanceInitMethod(cFile);
                int size = cFile.getMethodInfoController().getCurrentSize();
                for (int loop = 0; loop < size; loop++) {
                    DCMethodInfo mInfo = (DCMethodInfo) cFile.getMethodInfoController().get(loop);
                    mInfo.setKeepWithMayBe(true);
                }
                //initMethod.setKeepWithMayBe(true);
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

    }

    private static void addClinitInExamineList(String fQClassName) {
        //always use <clinit> .
        examineList.add(fQClassName, STATIC_INIT_NAME, VOID_DESC, false, false);
        debugOutput("Adding to examime <clinit> of class " + fQClassName);
    }

    /**
     *  If method is not static then each method in
     *  super-class of x with same signatures are put
     *  in the examine list.
     *
     *  If method is from an interface then all the implementation methods with same
     *  signature are added to examine in case that implementation is kept.
     *
     * @param methodX
     */
    private void updateExamineList(MethodExamineListEntry methodX) {
        try {
            //always use <clinit>
            addClinitInExamineList(methodX.getMethodFullyQualifiedClassName());

            DCMethodInfo mInfo = methodX.getMethodInfo();
            DCClassFile mClass = methodX.getMethodClass();
            if ((mInfo != null && mInfo.getAccessFlags().isStatic())
                    || methodX.isStaticParent()
                    || methodX.getMethodName().equals(STATIC_INIT_NAME)
                    || !methodX.isGotMethodFromBytecodeTraverse()) {
                return;
            }
            debugOutput("2: updating examine list for method="
                    + methodX);

            Vector myFamily = new Vector();
            //if (methodX.getMethodFullyQualifiedClassName().equals(OBJECT_CLASS))
            getAllSuperClasses(mClass, myFamily);

            Iterator<DCClassFile> familyIt = myFamily.iterator();
            while (familyIt.hasNext()) {
                DCClassFile checkFile = familyIt.next();
                MethodExamineListEntry entry = new MethodExamineListEntry(
                        checkFile.getFullyQualifiedClassName(),
                        methodX.getMethodName(), methodX.getMethodDescr(), false, false);
                if (methodX.getMethodInfo() != null && methodX.getMethodInfo().getAccessFlags().isStatic()) {
                    entry.setStaticParent();
                }
                if (examineList.add(entry)) {
                    debugOutput("3: list contains " + checkFile.getFullyQualifiedClassName()
                            + ", mIndex=" + methodX.getMethodName() + ", "
                            + methodX.getMethodDescr());
                }
            }
            /**
             * for interfaces
             */
            updateExamineListForInterfaces(methodX);
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void updateExamineListForInterfaces(MethodExamineListEntry methodX) throws Exception {
        /**
         * for interface if and only if any implementation's
         * class is kept then also examine the method otherwise not.
         */
        Vector subInterfaces = new Vector();
        if (methodX.getMethodClass() == null) {
            return;
        }
        oracle.getAllSubInterfaces(methodX.getMethodClass(), subInterfaces);
        subInterfaces.add(methodX.getMethodClass());
        Iterator<DCClassFile> it = subInterfaces.iterator();
        HashSet<DCClassFile> classFileToExamine = new HashSet<DCClassFile>();
        while (it.hasNext()) {
            String classFQName = it.next().getFullyQualifiedClassName();
            Vector toExamineVec = oracle.getAllDirectImplementations(classFQName);
            Vector temp = new Vector();
            /**
             * for abstract classes
             */
            Iterator<DCClassFile> itExam = temp.iterator();
            while (itExam.hasNext()) {
                DCClassFile cFile = itExam.next();
                if (cFile.getAccessFlags().isAbstract()) {
                       temp.addAll(oracle.getAllSubClasses(cFile));
                }
            }
            toExamineVec.addAll(temp);
            classFileToExamine.addAll(toExamineVec);
        }
        Iterator<DCClassFile> familyIt = classFileToExamine.iterator();
        while (familyIt.hasNext()) {
            DCClassFile checkFile = familyIt.next();
            MethodExamineListEntry entry = new MethodExamineListEntry(
                    checkFile.getFullyQualifiedClassName(),
                    methodX.getMethodName(), methodX.getMethodDescr(), false, false);
            toExamineIfTheirClassisKept.add(entry);
        }
    }

    /**
     *  - traverse x bytecode
     *  --- If found a NEW set class corresponding to it as foundInstance
     *  --- If found a invokeVirtual or invokespecial then add the method in
     *      the examine list
     *  --- If found a invokeStatic then add the method in examine list and set
     *      corresponding class as keepStatic
     *  --- If found a getStatic then add the class as keepStatic
     *
     * @param methodX
     */
    private void traverseBytecode(MethodExamineListEntry methodX) {
        /*if (methodX.getMethodName().equals("run") &&
        methodX.getMethodClass().getFullyQualifiedClassName().contains("takatuka.vm.VM$1")) {
        Miscellaneous.println("Stop here");
        }*/
        DCMethodInfo method = methodX.getMethodInfo();
        if (method == null) {
            return; //cannot traverse it.
        }

        CodeAtt codeAtt = method.getCodeAtt();
        if (codeAtt == null) {
            return;
        }
        Vector bytecode = codeAtt.getInstructions();
        Iterator<Instruction> bcIt = bytecode.iterator();
        Instruction instr = null;
        DCClassFile bytecodeClass = methodX.getMethodClass();
        while (bcIt.hasNext()) {
            instr = bcIt.next();
            traversBytecodeOperation(instr, bytecodeClass);
        }
    }

    private void setStaticAccess(DCClassFile cFile) {
        addClinitInExamineList(cFile.getFullyQualifiedClassName());
        cFile.setStaticAccess();
    }

    public static void setKeepPerUserRequest(DCClassFile cFile) {
        cFile.setKeepPerUserRequest();
        addClinitInExamineList(cFile.getFullyQualifiedClassName());
    }

    public static void setInstanceAccess(DCClassFile cFile) {
        cFile.setInstanceAccess(true);
        addClinitInExamineList(cFile.getFullyQualifiedClassName());
    }

    private void keepMyInterfaceAndItsAllSuperInterface(DCClassFile cFile) throws Exception {
        Vector superInterfaces = new Vector();
        /*if (cFile.getFullyQualifiedClassName().contains("ClassImplementingInterface")) {
        Miscellaneous.println("Stop ere");
        }*/
        oracle.getAllSuperInterfaces(cFile, superInterfaces);
        Iterator<DCClassFile> it = superInterfaces.iterator();
        while (it.hasNext()) {
            DCClassFile interfClass = it.next();
            if (interfClass != null) {
                //Miscellaneous.println("see me here " + interfClass.getFullyQualifiedClassName());
                if (!cFile.isKeepPerUserRequest()) {
                    setInstanceAccess(interfClass);
                } else {
                    setKeepPerUserRequest(interfClass);
                }
            }
        }
    }

    private void keepSuperClasses(DCClassFile cFile) throws Exception {
        Vector superClasses = new Vector();
        ClassFile.currentClassToWorkOn = cFile;
        //also keep super interfaces
        getAllSuperClasses(cFile, superClasses);
        Iterator<DCClassFile> sClassesIt = superClasses.iterator();
        while (sClassesIt.hasNext()) {
            DCClassFile temp = sClassesIt.next();
            if (!cFile.isKeepPerUserRequest()) {
                setInstanceAccess(temp);
            } else {
                setKeepPerUserRequest(temp);
            }
        }

    }

    public void keepMeAndMySuperClassesAndMySuperInterfaces(DCClassFile cFile) {
        //I am kept
        setInstanceAccess(cFile);
        try {
            //now keep my super classes
            keepSuperClasses(cFile);
            //keep my interfaces. Also keep super interfaces of my interfaces

            keepMyInterfaceAndItsAllSuperInterface(cFile);
        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
    }

    private void invokeInstructionsHandler(int opcode, Instruction instr,
            DCClassFile codeCFile) {
        MultiplePoolsFacade cp = codeCFile.getConstantPool();
        ClassFile.currentClassToWorkOn = codeCFile;

        Un operandData = (Un) instr.getOperandsData().clone();
        int cpIndex = -1;
        try {
            //get the index and leave every thing else
            operandData = Un.cutBytes(2, operandData);
            cpIndex = operandData.intValueUnsigned();
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }

        int tag = TagValues.CONSTANT_Methodref;
        if (opcode == JavaInstructionsOpcodes.INVOKEINTERFACE) {
            tag = TagValues.CONSTANT_InterfaceMethodref;
        }
        ReferenceInfo mRefInfo = (ReferenceInfo) cp.get(cpIndex,
                tag);
        NameAndTypeInfo nAt = (NameAndTypeInfo) cp.get(mRefInfo.getNameAndTypeIndex().
                intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        String methodName = oracle.getUTF8(nAt.getIndex().intValueUnsigned(), cp);
        String methodDesc = oracle.getUTF8(nAt.getDescriptorIndex().intValueUnsigned(), cp);
        ClassInfo cInfo = (ClassInfo) cp.get(mRefInfo.getIndex().intValueUnsigned(), TagValues.CONSTANT_Class);
        String classFQName = oracle.getUTF8(cInfo.getIndex().intValueUnsigned(), cp);

        debugOutput("\tAdding to examine " + classFQName + ", "
                + methodName + ", "
                + methodDesc);
        //-------------- end delme
        //debug_output("cFile = " + cFile.getFullyQualifiedClassName());
        MethodExamineListEntry entry = new MethodExamineListEntry(classFQName, methodName, methodDesc, true, false);
        //todo problem with this code. It is possible that are previous add was not got from traverse and this is...
        if (examineList.add(entry) && opcode == JavaInstructionsOpcodes.INVOKESTATIC) {
            MethodExamineListEntry examListEntry = examineList.getLastEntry();
            DCMethodInfo lastAddedMethod = examListEntry.getMethodInfo();
            if (lastAddedMethod != null) {
                lastAddedMethod.setKeepPerUserReqOrStatic();
                if (entry.getMethodClass() != null) {
                    //Miscellaneous.println("-------- "+entry.getMethodClass().getFullyQualifiedClassName());
                    entry.getMethodClass().setKeepPerUserRequest();
                }
            }
        }
    }

    private void newInstructionHandler(Instruction instr, DCClassFile codeCFile) {
        MultiplePoolsFacade cp = codeCFile.getConstantPool();
        int cpIndex = instr.getOperandsData().intValueUnsigned();
        DCClassFile cFile = (DCClassFile) getClass(cpIndex, codeCFile);
        if (cFile == null) {
            return; //may be not loaded
        }
        keepMeAndMySuperClassesAndMySuperInterfaces(cFile);
    }

    private void traversBytecodeOperation(Instruction instr, DCClassFile codeCFile) {
        int opcode = instr.getOpCode();
        int cpIndex = -1;
        DCClassFile cFile = null;
        MultiplePoolsFacade cp = codeCFile.getConstantPool();
        ClassFile.currentClassToWorkOn = codeCFile;
        if (opcode == JavaInstructionsOpcodes.NEW) {
            debugOutput(" found instr =" + instr.getMnemonic());
            newInstructionHandler(instr, codeCFile);
        } else if (opcode == JavaInstructionsOpcodes.INVOKESPECIAL
                || opcode == JavaInstructionsOpcodes.INVOKEVIRTUAL
                || opcode == JavaInstructionsOpcodes.INVOKESTATIC
                || opcode == JavaInstructionsOpcodes.INVOKEINTERFACE) {
            debugOutput(" found instr =" + instr.getMnemonic());
            invokeInstructionsHandler(opcode, instr, codeCFile);
        } else if (opcode == JavaInstructionsOpcodes.GETSTATIC
                || opcode == JavaInstructionsOpcodes.PUTSTATIC) {
            debugOutput(" found instr =" + instr.getMnemonic());
            cpIndex = instr.getOperandsData().intValueUnsigned();
            ReferenceInfo mRefInfo = (ReferenceInfo) cp.get(cpIndex,
                    TagValues.CONSTANT_Fieldref);
            int classIndex = mRefInfo.getIndex().intValueUnsigned();
            cFile = getClass(classIndex, codeCFile);
            if (cFile == null) {
                return; //may be not loaded
            }
            setStaticAccess(cFile);

        } else if (opcode == JavaInstructionsOpcodes.CHECKCAST
                || opcode == JavaInstructionsOpcodes.INSTANCEOF) {
            debugOutput(" found instr =" + instr.getMnemonic());
            //get the operand if it corresponds to a interface then
            //that interface and its subinterfaces are CANDIDATES to be kept.
            cpIndex = instr.getOperandsData().intValueUnsigned();
            cFile = getClass(cpIndex, codeCFile);
            if (cFile != null && cFile.getAccessFlags().isInterface()) {
                //Miscellaneous.println(cFile.getFullyQualifiedClassName() + "-sfasfasf-sfasfsa-----asfasfas----asfas");
                this.intefaceCandidate.addElement(cFile.getFullyQualifiedClassName());
            }
        }
    }

    private DCClassFile getClass(int index, ClassFile cFileInput) {
        ClassFile.currentClassToWorkOn = cFileInput;
        MultiplePoolsFacade cp = cFileInput.getConstantPool();
        ClassInfo cInfo = (ClassInfo) cp.get(index, TagValues.CONSTANT_Class);
        UTF8Info UTF8Name = (UTF8Info) cp.get(cInfo.getIndex().intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        DCClassFile cFile = (DCClassFile) oracle.getClass(UTF8Name.convertBytes());
        if (cFile == null) {
            String className = UTF8Name.convertBytes();
            if (!notFoundClassFileNames.contains(className)
                    && !(className.startsWith("[") && className.length() == 2)) {
                notFoundClassFileNames.addElement(className);
                debugOutput("\n***** WARNING: cannot find class file = " + className);
                if (!className.trim().startsWith("[")) {
                    LogHolder.getInstanceOf().addLog("WARNING: cannot find class file = "
                            + className + ". Press any key to continue ...", true);

                    Scanner in = new Scanner(System.in);
                    in.nextLine();
                }
            }
        }
        return cFile;
    }
}
