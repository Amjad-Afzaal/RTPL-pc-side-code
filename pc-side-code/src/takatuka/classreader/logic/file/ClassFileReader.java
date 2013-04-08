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
package takatuka.classreader.logic.file;

import java.io.*;
import java.util.*;

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.io.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * It provide a universal access for reading directory, a class file and a jar file.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ClassFileReader {
    //private ClassFile classFile = null;
    private static int totalFilesRead = 0;
    private static final boolean debug = false;
    private FactoryFacade factory = null;
    private int filesReadAtAtime = 0;    //todo should not be static
    public static int filesToRead = -1;
    private Vector classesToRead = new Vector();
    protected Vector filesToReadVec = new Vector();
    private boolean isReadingFromFileDirectly = false;
    private static long lengthOfInput = 0;
    private static final ClassFileReader cFReader = new ClassFileReader();

    protected ClassFileReader() {
        super();
    }

    public static ClassFileReader getInstanceOf() {
        return cFReader;
    }

    public static int getTotalFilesRead() {
        return totalFilesRead;
    }

    protected void readSomeFiles(File file) throws FileNotFoundException,
            IOException, Exception {
        if (isReadingFromFileDirectly &&
                (!file.getName().endsWith(".class") ||
                !file.exists())) {
            Miscellaneous.printlnErr("Cannot find input class file named= " + file.getName());
            Miscellaneous.exit();
        }
        if (file.isDirectory() || !file.getName().endsWith(".class")) {
            return;
        }
        ClassFile classFile = factory.createClassFile(factory.createConstantPool(), file.getAbsolutePath());

        if (totalFilesRead >= filesToRead && filesToRead > 0) {
            return; //do not read more files.
        }

        finallyRead(classFile, file);

    //Miscellaneous.println(classFile);

    }

    private final void finallyRead(ClassFile classFile, File file) throws
            Exception {
        if (debug) {
            LogHolder.getInstanceOf().addLog("Reading file = " + file + "....");
        }
        if (totalFilesRead % 5000 == 0 && totalFilesRead != 0) {
            Miscellaneous.println("class files read = " + totalFilesRead);
        }
        ClassFile.currentClassToWorkOn = classFile;
        DataFileInputStreamWithValidation dis = new DataFileInputStreamWithValidation(
                file);
        lengthOfInput += file.length();
        ParseClassFile.getInstanceOf().parseFile(dis, classFile);
        ClassFileController.getInstanceOf().add(classFile);
        totalFilesRead = ClassFileController.getInstanceOf().getCurrentSize();
        // dispose all the resources after using them.
        dis.close();
    }

    //todo should not be static 
    public static long getTotalLengthOfInput() {
        return lengthOfInput;
    }

    private void init() {
        this.factory = FactoryPlaceholder.getInstanceOf().getFactory();
        this.filesReadAtAtime = -1; //todo later filesReadAtAtime;
    }

    /**
     * take set of directories or files with path seperated with colons.
     * Example Main.class:./takatukajava/Test.class:.
     * @param path
     */
    public void addFakeClassPathToRead(String path) {
        StringTokenizer stoken = new StringTokenizer(path, System.getProperty("path.separator") );
        while (stoken.hasMoreTokens()) {
            filesToReadVec.addElement(new File((String)stoken.nextElement()));
        }
    }

    public void read() throws Exception {
        init();
        LogHolder.getInstanceOf().addLog("Started reading file(s)... ", true);
        for (int loop = 0; loop < filesToReadVec.size(); loop++) {
            read((File) filesToReadVec.elementAt(loop));
        }
        readMsgs();
    }

    private void read(File file) throws
            Exception {
        if (file.isDirectory()) {
            isReadingFromFileDirectly = false;
            readFilesFromDirectory(file);
        } else {
            isReadingFromFileDirectly = true;
            readSomeFiles(file);
        }
    }

    protected void readMsgs() {
        LogHolder.getInstanceOf().addLog("Done with reading! ");
        LogHolder.getInstanceOf().addLog("Total class files read = " +
                totalFilesRead, true);
    }

    public final boolean isMore() {
        return (classesToRead.size() > 0);
    }

    /* public final void readNext() throws Exception {
    MemClass memClass = null;
    int loop = 0;
    int size = classesToRead.size();
    for (loop = 0; (filesReadAtAtime < 0 ||
    loop < filesReadAtAtime) &&
    loop < size; loop++) {
    memClass = (MemClass) classesToRead.get(loop);
    finallyRead(memClass.cFile, new File(memClass.path));
    classesToRead.remove(loop);
    }
    }*/
    protected void readFilesFromDirectory(File dir) throws
            Exception {
        if (!dir.isDirectory()) {
            throw new Exception(dir + " is not a valid directory ");
        }

        if (totalFilesRead >= filesToRead && filesToRead > 0) {
            return; //do not read more files.
        }

        File files[] = dir.listFiles();
        File temp = null;
        for (int loop = 0; loop < files.length; loop++) {
            temp = (File) files[loop];
            if (temp.isDirectory()) {
                readFilesFromDirectory(temp);
            }
            readSomeFiles(temp);
        }
    }
}
