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

import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.classreader.logic.util.Miscellaneous;
import takatuka.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class ClassFileWriter {

    private static String outputDirectory = null;

    public ClassFileWriter(String outputDirectory) throws Exception {
        setOutputDirectory(outputDirectory);
    }

    public static void setOutputDirectory(String outputDirName) throws Exception {
        File temp = new File(outputDirName);
        if (!temp.exists()) {
            if (!temp.mkdirs()) {
                throw new Exception("Output directory does not exist and could not created!");
            }
        }
        outputDirectory = temp.getAbsolutePath(); //it will convert all slashes in one form and remove last slash....

    }

    public static String getOutputDirectory() {
        return outputDirectory;
    }

    private static void makeDirectories(String fileName) {
        String newName = outputDirectory + "/";
        if (fileName.lastIndexOf('/') > 0) {
            newName = newName + fileName.substring(0, fileName.lastIndexOf('/'));
        }
        (new File(newName)).mkdirs();
    }

    public static String createDestinationName(String forClass, boolean isclass) {
        makeDirectories(forClass);
        if (isclass) {
            return outputDirectory + "/" + forClass + ".class";
        } else {
            return outputDirectory + "/" + forClass + ".txt";
        }
    }

    public void writeAll() throws FileNotFoundException,
            IOException, Exception {
        Miscellaneous.println("Started Writing file(s) ...\n");
        ClassFileController controller = ClassFileController.getInstanceOf();
        ClassFile classFile = null;
        String filename = null;
        String classname = null;
        String txtname = null;
        //File file = null;
        for (int loop = 0; loop < controller.getCurrentSize(); loop++) {
            classFile = (ClassFile) controller.get(loop);
            ClassInfo cinfo = (ClassInfo) classFile.getConstantPool().get(
                    classFile.getThisClass().intValueUnsigned(),
                    TagValues.CONSTANT_Class);
//               Miscellaneous.println(cinfo.getIndex().intValueUnsigned());
            filename = UTF8Info.convertBytes((UTF8Info) classFile.getConstantPool().
                    get(cinfo.getIndex().intValueUnsigned(),
                    TagValues.CONSTANT_Utf8));
            LogHolder.getInstanceOf().addLog("writing class file =" + filename);
            classname = createDestinationName(filename, true);
            //Miscellaneous.println("Writing " + classname + "....");
            writeFile(new File(classname), classFile);
            txtname = createDestinationName(filename, false);
            //Miscellaneous.println("Writing " + txtname + "....");
            writeFile(new File(txtname), classFile.toString());
        }

        Miscellaneous.println("\nDone with Writing file(s)!");
    }

    public static void writeFile(File file, String str) throws
            FileNotFoundException, IOException, Exception {
        if (file.exists()) {
            file.delete();
        }
        FileWriter fileWriter = new FileWriter(file);
        BufferedWriter buff = new BufferedWriter(fileWriter);
        buff.flush();
        buff.write(str);
        buff.close();
        fileWriter.close();

    }

    public void writeFile(File file, ClassFile classFile) throws
            FileNotFoundException,
            IOException, Exception {
        FileOutputStream fos = new FileOutputStream(file);
        BufferedByteCountingOutputStream bos = new BufferedByteCountingOutputStream(fos);
        classFile.writeSelected(bos);
        bos.flush();
        // dispose all the resources after using them.
        fos.close();
        bos.close();
    }
}
