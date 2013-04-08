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
package takatuka.optimizer.cpGlobalization.logic.util;

import takatuka.classreader.dataObjs.*;
import java.util.*;
import takatuka.classreader.dataObjs.attribute.Instruction;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
class ClassFileOracle {

    private static final ClassFileOracle cfOracle = new ClassFileOracle();

    private ClassFileOracle() {
    }

    static ClassFileOracle getInstanceOf() {
        return cfOracle;
    }

    final MethodInfo getInstanceInitMethod(ClassFile classFile) {
        return (MethodInfo) getMethodOrField(classFile, "<init>", "()V", true);
    }

    /**
     * Given an instruction it return CP-Index used in that instruction 
     * (i.e. from the instruction operand).
     * In case the given instruction does not use any CP-Index then the function
     * returns -1;
     * 
     * @param inst
     * @return 
     */
    final int getCPIndex(Instruction inst) {
        int index = -1;
        if (!inst.isCPInstruction()) {
            return -1;
        }
        try {
            Un operand = (Un) inst.getOperandsData().clone();
            if (operand.size() <= 2) {
                index = operand.intValueUnsigned();
            } else {
                index = Un.cutBytes(2, operand).intValueUnsigned();
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return index;
    }

    final FieldInfo getMethodOrField(ClassFile classFile, FieldInfo field,
            boolean isMethod) {
        FieldInfo mInfo = null;
        MultiplePoolsFacade cp = classFile.getConstantPool();
        ClassFile originalCF = ClassFile.currentClassToWorkOn;
        ClassFile.currentClassToWorkOn = field.getClassFile();
        String givenFieldName = methodOrFieldName(field, field.getClassFile().getConstantPool());
        String givenFieldDesc = methodOrFieldDescription(field, field.getClassFile().getConstantPool());

        //System.out.println(classFile.getFullyQualifiedClassName());
        ClassFile.currentClassToWorkOn = classFile;
        FieldInfoController mCont = classFile.getMethodInfoController();
        if (!isMethod) {
            mCont = classFile.getFieldInfoController();
        }
        int size = mCont.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            mInfo = (FieldInfo) mCont.get(loop);
            String nameToCheck = methodOrFieldName(mInfo, cp);
            String descToCheck = methodOrFieldDescription(mInfo, cp);
            if (givenFieldName.equals(nameToCheck)
                    && descToCheck.equals(givenFieldDesc)) {
                break;
            }
            mInfo = null;
        }
        ClassFile.currentClassToWorkOn = originalCF;
        return mInfo;
    }

    final FieldInfo getMethodOrField(ClassFile classFile, String methodName,
            String methodDesc, boolean isMethod) {
        FieldInfo mInfo = null;
        MultiplePoolsFacade cp = classFile.getConstantPool();
        ClassFile originalCF = ClassFile.currentClassToWorkOn;
        ClassFile.currentClassToWorkOn = classFile;
        FieldInfoController mCont = classFile.getMethodInfoController();
        if (!isMethod) {
            mCont = classFile.getFieldInfoController();
        }
        int size = mCont.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            mInfo = (FieldInfo) mCont.get(loop);
            String checkName = methodOrFieldName(mInfo, cp);
            String checkDesc = methodOrFieldDescription(mInfo, cp);
            if (checkName.equals(methodName) && checkDesc.equals(methodDesc)) {
                break;
            }
            mInfo = null;
        }
        ClassFile.currentClassToWorkOn = originalCF;
        return mInfo;
    }

    final FieldInfo getMethodOrField(String fullyQualifiedClassName,
            String methodName, String methodDesc, boolean isMethod) {
        ClassFile classFile = getClass(fullyQualifiedClassName);
        return getMethodOrField(classFile, methodName, methodDesc, isMethod);
    }

    final String methodOrFieldDescription(FieldInfo mInfo, MultiplePoolsFacade cp) {
        return getUTF8(mInfo.getDescriptorIndex().intValueUnsigned(), cp);
    }

    final String methodOrFieldName(FieldInfo mInfo, MultiplePoolsFacade cp) {
        return getUTF8(mInfo.getNameIndex().intValueUnsigned(), cp);
    }

    final String methodOrFieldName(ReferenceInfo RefInfo, MultiplePoolsFacade cp) {
        NameAndTypeInfo nAt = (NameAndTypeInfo) cp.get(RefInfo.getNameAndTypeIndex().
                intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        return getUTF8(nAt.getIndex().intValueUnsigned(), cp);
    }

    final String methodOrFieldDescription(ReferenceInfo RefInfo, MultiplePoolsFacade cp) {
        NameAndTypeInfo nAt = (NameAndTypeInfo) cp.get(RefInfo.getNameAndTypeIndex().
                intValueUnsigned(), TagValues.CONSTANT_NameAndType);
        return getUTF8(nAt.getDescriptorIndex().intValueUnsigned(), cp);
    }

    final String getUTF8(int index, MultiplePoolsFacade cp) {
        UTF8Info utf8Info = (UTF8Info) cp.get(index, TagValues.CONSTANT_Utf8);
        return utf8Info.convertBytes();
    }

    final Vector<ClassFile> getAllDirectImplementations(String fullyQualifiedInterfaceName) {
        Vector ret = new Vector();
        ClassFileController controller = ClassFileController.getInstanceOf();
        int size = controller.getCurrentSize();
        for (int loop = 0; loop < size; loop++) {
            ClassFile cFile = (ClassFile) controller.get(loop);
            MultiplePoolsFacade cp = cFile.getConstantPool();
            ClassFile.currentClassToWorkOn = cFile;
            InterfaceController interfaceCont = cFile.getInterfaceController();
            for (int interLoop = 0; interLoop < interfaceCont.getCurrentSize(); interLoop++) {
                int cpIndex = ((Un) interfaceCont.get(interLoop)).intValueUnsigned();
                ClassInfo cInfo = (ClassInfo) cp.get(cpIndex, TagValues.CONSTANT_Class);
                String checkName = getUTF8(cInfo.getIndex().intValueUnsigned(), cp);
                if (checkName.equals(fullyQualifiedInterfaceName)) {
                    ret.addElement(cFile);
                }
            }
        }
        return ret;
    }

    final ClassFile getSuperClass(ClassFile cFile) {
        if (cFile == null) {
            return null;
        }
        ClassFile.currentClassToWorkOn = cFile;
        MultiplePoolsFacade cp = cFile.getConstantPool();
        int superThis = cFile.getSuperClass().intValueUnsigned();
        if (superThis == 0) {
            return null; //it is java/lang/Object
        }
        ClassInfo superCI = (ClassInfo) cp.get(superThis, TagValues.CONSTANT_Class);
        String fullyQualifiedName = getUTF8(superCI.getIndex().intValueUnsigned(), cp);
        if (fullyQualifiedName.equals("java/lang/Object")) {
            //return null; //we have reached Object class
        }
        return getClass(fullyQualifiedName); //assumes it is already loaded.
    }

    final HashSet getAllSubClasses(ClassFile cFile) throws Exception {
        return getAllSubClasses(cFile.getFullyQualifiedClassName());
    }

    /**
     * get all the interfaces implemented by the cFile or by any of its super classfiles.
     * 
     * @param cFile
     * @param interfaces
     * @throws Exception
     */
    final Vector<ClassFile> getAllInterfacesImpled(ClassFile cFile) throws Exception {
        Vector<ClassFile> ret = new Vector<ClassFile>();
        getAllInterfacesImpledHelper(cFile, ret);
        Iterator<ClassFile> it = ret.iterator();
        Vector<ClassFile> temp = new Vector<ClassFile>();
        while (it.hasNext()) {
            ClassFile inter = it.next();
            getAllSuperInterfaces(inter, temp);
        }
        ret.addAll(temp);
        return ret;
    }

    private void getAllInterfacesImpledHelper(ClassFile cFile, Vector<ClassFile> interfaces) throws Exception {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        Vector<ClassFile> superClasses = new Vector<ClassFile>();
        superClasses.add(cFile);
        //get all the super classes
        getAllSuperClasses(cFile, superClasses);
        //now get all the interfaces implemented by those classes and the current class.
        Iterator<ClassFile> superClassesIt = superClasses.iterator();
        while (superClassesIt.hasNext()) {
            ClassFile superClass = superClassesIt.next();
            InterfaceController interfaceCont = superClass.getInterfaceController();
            for (int loop = 0; loop < interfaceCont.getCurrentSize(); loop++) {
                Un interfaceThisPointer = (Un) interfaceCont.get(loop);
                interfaces.add(getClass(interfaceThisPointer.intValueUnsigned(), pOne));
            }
        }
    }

    /**
     * Give an interface it returns all the classes implementing it 
     * directly or though its any subinterface.
     *
     * @param givenInterface
     * @param implementation
     * @throws Exception
     */
    final void getAllImplementations(ClassFile givenInterface, HashSet<ClassFile> implementation) throws Exception {
        /**
         * first get all Sub interfaces.
         */
        Vector<ClassFile> subInterfaces = new Vector();
        if (!givenInterface.getAccessFlags().isInterface()) {
            return;
        }
        getAllSubInterfaces(givenInterface, subInterfaces);
        subInterfaces.add(givenInterface);
        /**
         * Go though all the classfile n and find each class implementing
         * any of those m interface (O(n*m) complexity).
         *
         * Each such class is added in the return implemention vector.
         */
        ClassFileController cFileContr = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileContr.getCurrentSize(); loop++) {
            ClassFile currentCFile = (ClassFile) cFileContr.get(loop);
            if (currentCFile.getAccessFlags().isInterface()) {
                continue;
            }
            Iterator<ClassFile> it = subInterfaces.iterator();
            while (it.hasNext()) {
                ClassFile aInterface = it.next();
                if (isImplementsTheInterface(aInterface, currentCFile)) {
                    /*System.out.println(currentCFile.getFullyQualifiedClassName()+
                    " implements at least one of those interfaces ....");
                     *
                     */
                    implementation.add(currentCFile);
                    break;
                }
            }
        }
    }

    /**
     * Given an interface it returns all the interfaces extending this interface.
     * @param givenInterface
     * @param interfaces
     * @throws Exception
     */
    final void getAllSubInterfaces(ClassFile givenInterface, Vector<ClassFile> interfaces) throws Exception {
        /**
         * The complexity of this function is O(n^2). It goes through all the classfiles
         * and find all interfaces extending the given interface.
         * Those classes are added in the returning vector-interfaces.
         * 
         * Subsequently the function recall itself for each of interface found in the first set
         * to find their sub-interfaces.
         * The process continues until no more interfaces can be found.
         */
        if (givenInterface == null || !givenInterface.getAccessFlags().isInterface()) {
            return;
        }
        Vector toExamine = new Vector();
        ClassFileController cFileContr = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileContr.getCurrentSize(); loop++) {
            ClassFile currentCFile = (ClassFile) cFileContr.get(loop);
            if (!currentCFile.getAccessFlags().isInterface()) {
                continue;
            }
            if (isImplementsTheInterface(givenInterface, currentCFile)) {
                interfaces.add(currentCFile);
                toExamine.addElement(currentCFile);
            }
        }
        Iterator<ClassFile> it = toExamine.iterator();
        while (it.hasNext()) {
            getAllSubInterfaces(it.next(), interfaces);
        }
    }

    /**
     * return true/false depending upon if the interface is implemented by the 
     * classfile or not.
     * 
     * @param interfaceCFile
     * @param implementedBy
     * @return
     */
    final boolean isImplementsTheInterface(ClassFile interfaceCFile, ClassFile implementedBy) {
        MultiplePoolsFacade cp = implementedBy.getConstantPool();
        InterfaceController interfaceCont = implementedBy.getInterfaceController();
        int size = interfaceCont.getCurrentSize();
        ClassFile temp = ClassFile.currentClassToWorkOn;
        for (int loop = 0; loop < size; loop++) {
            ClassFile.currentClassToWorkOn = implementedBy;
            //Miscellaneous.println(cFile.getFullyQualifiedClassName()+",,,, ");
            int cpIndex = ((Un) interfaceCont.get(loop)).intValueUnsigned();
            ClassInfo cInfo = (ClassInfo) cp.get(cpIndex, TagValues.CONSTANT_Class);
            String className = getUTF8(cInfo.getIndex().intValueUnsigned(), cp);
            ClassFile toCheckinterfClass = (ClassFile) getClass(className);
            if (toCheckinterfClass != null && toCheckinterfClass.getFullyQualifiedClassName().
                    equals(interfaceCFile.getFullyQualifiedClassName())) {
                return true;
            }
        }
        ClassFile.currentClassToWorkOn = temp;
        return false;
    }

    final void getAllSuperInterfaces(ClassFile cFile, Vector<ClassFile> interfaces) throws Exception {
        MultiplePoolsFacade cp = cFile.getConstantPool();
        InterfaceController interfaceCont = cFile.getInterfaceController();
        int size = interfaceCont.getCurrentSize();
        ClassFile temp = ClassFile.currentClassToWorkOn;
        for (int loop = 0; loop < size; loop++) {
            ClassFile.currentClassToWorkOn = cFile;
            //Miscellaneous.println(cFile.getFullyQualifiedClassName()+",,,, ");
            int cpIndex = ((Un) interfaceCont.get(loop)).intValueUnsigned();
            ClassInfo cInfo = (ClassInfo) cp.get(cpIndex, TagValues.CONSTANT_Class);
            String className = getUTF8(cInfo.getIndex().intValueUnsigned(), cp);
            ClassFile interfClass = (ClassFile) getClass(className);
            ClassFile.currentClassToWorkOn = interfClass;
            if (interfClass != null) {
                interfaces.add(interfClass);
                getAllSuperInterfaces(interfClass, interfaces);
            }
        }
        ClassFile.currentClassToWorkOn = temp;
    }

    final HashSet getAllClassesStartWith(String startWith) throws Exception {
        HashSet ret = new HashSet();
        ClassFileController cFileController = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileController.getCurrentSize(); loop++) {
            ClassFile testFile = (ClassFile) cFileController.get(loop);
            //Miscellaneous.println(".............> "+testFile.getFullyQualifiedClassName());
            if (testFile.getFullyQualifiedClassName().startsWith(startWith)) {
                ret.add(testFile);
            }
        }
        return ret;
    }

    final HashSet getAllSubClasses(String fullyQualifiedClassName) throws Exception {
        HashSet ret = new HashSet();
        ClassFileController cFileController = ClassFileController.getInstanceOf();
        for (int loop = 0; loop < cFileController.getCurrentSize(); loop++) {
            ClassFile testFile = (ClassFile) cFileController.get(loop);
            //Miscellaneous.println(".............> "+testFile.getFullyQualifiedClassName());
            if (testFile.getFullyQualifiedClassName().equals(fullyQualifiedClassName)) {
                continue;
            }
            if (isSubClass(testFile, fullyQualifiedClassName)) {
                //Miscellaneous.println("has subClass " + testFile.getFullyQualifiedClassName());
                ret.add(testFile);
                //Miscellaneous.println("added " + testFile.getFullyQualifiedClassName());
            }
        }
        return ret;
    }

    /**
     * returns all the superclasses of cFileController
     *
     * @param cFileController ClassFile
     * @param retSet Vector
     * @throws Exception
     */
    final void getAllSuperClasses(ClassFile cFile, Vector retSet) throws
            Exception {
        ClassFile superClass = getSuperClass(cFile);
        if (superClass == null) {
            return;
        }
        retSet.addElement(superClass);
        ClassFile.currentClassToWorkOn = superClass;
        getAllSuperClasses(superClass, retSet);
    }

    final ClassFile getClass(int thisClass, MultiplePoolsFacade cp) {
        Object cpObj = cp.get(thisClass, TagValues.CONSTANT_Class);
        if (cpObj == null || cpObj instanceof EmptyInfo) {
            return null;
        }
        ClassInfo classInfo = (ClassInfo) cpObj;
        String fullyQualifiedName = getUTF8(classInfo.getIndex().intValueUnsigned(), cp);
        return getClass(fullyQualifiedName);
    }

    /**
     * It returns common super class of both A and B that it neareast to them.
     *
     * Note that a common super class can always be found because Object is common
     * superclass of every class.
     * 
     * This function assume that you have a global constant pool
     * @param classA int
     * @param classB int
     * @return int
     */
    final int getNearestCommonSuperClass(int classA, int classB) throws
            Exception {
        //1. check if A and B are equals then return anyone of them
        //2. Get all the superclasses of A. Say it "superASet"
        //-- 2(a): Get one superclass of B and see if that is in superASet. if so return it
        //--- Keep repeating step 2(a) and you will get one super class sooner or later for sure...
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        if (classA == classB) {
            return classA;
        }
        ClassFile classAFile = getClass(classA, pOne);
        ClassFile classBFile = getClass(classB, pOne);
//        Miscellaneous.println("here here "+classAFile.getActualClassName()+", "+
//                           classBFile.getActualClassName());
        Vector superASet = new Vector();
        superASet.addElement(classAFile);
        Vector superBSet = new Vector();
        superBSet.addElement(classBFile);
        getAllSuperClasses(classAFile, superASet);
        getAllSuperClasses(classBFile, superBSet);
        for (int loop = 0; loop < superBSet.size(); loop++) {
            ClassFile bsSuper = (ClassFile) superBSet.elementAt(loop);
            if (superASet.contains(bsSuper)) {
//                Miscellaneous.println("return here " + bsSuper.getActualClassName());
                return bsSuper.getThisClass().intValueUnsigned();
            }
        }

        //should never throw this error if classes are loaded
        throw new Exception("Could not find a class file, a loading error ");
    }

    /**
     * Name will be fully qualified for example java/lang/Object
     * In case a class could not be found (may be not loaded or does not exist) then
     * the function will return -1
     * @param name fully-qualified name of a classfile. 
     * @return int this pointer of the class
     */
    final int getClassInfoByName(String name) {
        try {
            ClassFileController fileCont = ClassFileController.getInstanceOf();
            int size = fileCont.getCurrentSize();
            ClassFile file = null;
            for (int loop = 0; loop < size; loop++) {
                file = (ClassFile) fileCont.get(loop);
                if (file.getFullyQualifiedClassName().equals(name)) {
                    return file.getThisClass().intValueUnsigned();
                }
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return -1;
    }

    /**
     * returns true if fileA is subclass of fileB
     *
     * @param classA
     * @param classBfullyQualifiedName
     * @return
     * @throws java.lang.Exception
     */
    final boolean isSubClass(ClassFile classA, String classBfullyQualifiedName) throws
            Exception {
        if (classA == null || classBfullyQualifiedName == null
                || classA.getFullyQualifiedClassName().equals(classBfullyQualifiedName)) {
            return false;
        }
        //it get superclasses of fileA and check if anyone of them is fileB. if so
        //then return true. otherwise return false.
        if (classA.getSuperClass() != null
                && classA.getSuperClass().intValueUnsigned() != 0) {
            ClassFile.currentClassToWorkOn = classA;
            ClassFile superClass = getClass(getClassName(classA.getSuperClass().intValueUnsigned(),
                    classA.getConstantPool()));
            if (superClass == null) {
                return false;
            } else if (superClass.getFullyQualifiedClassName().equals(classBfullyQualifiedName)) {
                return true;
            }
            return isSubClass(superClass, classBfullyQualifiedName); //recursive
        }
        return false;
    }

    /**
     * returns true if fileA is subclass of fileB
     *
     * @param fileA ClassFile
     * @param fileB ClassFile
     * @return boolean
     * @throws Exception
     */
    final boolean isSubClass(ClassFile fileA, ClassFile fileB) throws
            Exception {
        if (fileA == null || fileB == null) {
            return false;
        }
        return isSubClass(fileA, fileB.getFullyQualifiedClassName());
    }

    private String getClassName(int thisPointer, MultiplePoolsFacade cp) {
        ClassInfo cInfo = (ClassInfo) cp.get(thisPointer, TagValues.CONSTANT_Class);
        return getUTF8(cInfo.getIndex().intValueUnsigned(), cp);
    }

    /**
     * return the package of ClassFile
     * @param file ClassFile
     * @return String
     * @throws Exception
     */
    final String getPackage(ClassFile file) throws Exception {
        return file.getPackageName();
    }

    final ClassFile getClass(String fullyQualifiedName) {
        ClassFileController cfCont = ClassFileController.getInstanceOf();
        return (ClassFile) cfCont.getClassByFullyQualifiedName(fullyQualifiedName);
    }

    final int getMethodInfoIndexFromContr(ClassFile cFile, MethodInfo method) {
        MethodInfoController mCont = cFile.getMethodInfoController();
        Oracle oracle = Oracle.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(method);
        for (int loop = 0; loop < mCont.getCurrentSize(); loop++) {
            MethodInfo methodToCompare = (MethodInfo) mCont.get(loop);
            String methodStrToCompare = oracle.getMethodOrFieldString(methodToCompare);
            if (methodStrToCompare.equals(methodStr)) {
                return loop;
            }
        }
        return -1;
    }

    final int getMethodInfoIndexFromContr(ClassFile cFile, String methodName,
            String methodDesc) {
        MethodInfoController mCont = cFile.getMethodInfoController();
        MultiplePoolsFacade localCP = cFile.getConstantPool();
        for (int loop = 0; loop < mCont.getCurrentSize(); loop++) {
            MethodInfo mInfo = (MethodInfo) mCont.get(loop);
            UTF8Info utf8Name = (UTF8Info) localCP.get(mInfo.getNameIndex().
                    intValueUnsigned(), TagValues.CONSTANT_Utf8);
            UTF8Info utf8Desc = (UTF8Info) localCP.get(mInfo.getDescriptorIndex().
                    intValueUnsigned(), TagValues.CONSTANT_Utf8);
            if (methodName.equals(utf8Name.convertBytes())
                    && methodDesc.equals(utf8Desc.convertBytes())) {
                return loop;
            }
        }
        return -1;
    }

    final int getReferenceFieldFromClassFile(MultiplePoolsFacade cp,
            ReferenceInfo ref, ClassFile file, boolean isMethod) {
        //Miscellaneous.println("1 --- "+file.getFullyQualifiedClassName());
        int nATIndex = ref.getNameAndTypeIndex().intValueUnsigned();
        NameAndTypeInfo nAt = (NameAndTypeInfo) cp.get(nATIndex, TagValues.CONSTANT_NameAndType);
        UTF8Info utf8Name = (UTF8Info) cp.get(nAt.getIndex().intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        UTF8Info utf8Desc = (UTF8Info) cp.get(nAt.getDescriptorIndex().intValueUnsigned(),
                TagValues.CONSTANT_Utf8);
        ControllerBase base = file.getFieldInfoController();
        if (isMethod) {
            base = file.getMethodInfoController();
        }
        int size = base.getCurrentSize();
        FieldInfo field = null;
        for (int index = 0; index < size; index++) {
            field = (FieldInfo) base.get(index);
            ClassFile.currentClassToWorkOn = file;
            UTF8Info utf8fieldName = (UTF8Info) file.getConstantPool().
                    get(field.getNameIndex().intValueUnsigned(),
                    TagValues.CONSTANT_Utf8);
            UTF8Info utf8fieldDesc = (UTF8Info) file.getConstantPool().
                    get(field.getDescriptorIndex().intValueUnsigned(),
                    TagValues.CONSTANT_Utf8);
            if (utf8Desc.equals(utf8fieldDesc) && utf8Name.equals(utf8fieldName)) {
                return index;
            }
        }
        return -1;
    }

    final int getReferenceFieldFromClassFile_GLOBALCP(ReferenceInfo ref, ClassFile file,
            boolean isMethod) {
        GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
        return getReferenceFieldFromClassFile(gcp, ref, file, isMethod);
    }

    final boolean isReferenceFromClassFile(ReferenceInfo ref, ClassFile file, boolean isMethod) {
        int fieldIndex = getReferenceFieldFromClassFile_GLOBALCP(ref, file, isMethod);
        if (fieldIndex == -1) {
            return false;
        }
        return true;

    }
}
