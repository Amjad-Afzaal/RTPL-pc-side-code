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
package takatuka.classreader.logic;


import takatuka.classreader.logic.file.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: It is from where we start.... </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeClassReader extends StartMeAbstract {

    public StartMeClassReader() {
        super();
    }

    @Override
    public void init(String args[]) throws Exception {
        if (args.length >= 3) {
            ClassFileReader.filesToRead = Integer.parseInt(args[2]);
        }
    }

    @Override
    public void setOutputWriter(String args[]) throws Exception {
        super.outputWriter = new ClassFileWriter(args[1]);
    }

    @Override
    public void read(String args[]) throws Exception {
        ClassFileReader cfReader = ClassFileReader.getInstanceOf();
        cfReader.addFakeClassPathToRead(args[0]);
        cfReader.read();
    }

    @Override
    public void execute(String args[]) throws Exception {

    }

    @Override
    public void validateInput(String args[]) throws Exception {
        if (args.length < 2 || args.length > 3) {
            Miscellaneous.println(
                    "Usage StartMe <input file-name/directory-Name> <output directory-Name> <OPTIONAL: Max no. of class files to be read>");
            Miscellaneous.println(
                    " If the input file is a directory then it recursively traverse it");
            Miscellaneous.println(
                    " The program will stop on the first file, not conforms with class-file-format");
            Miscellaneous.println(
                    " The output directory will have classfile.class and classfile.txt files. ");
            Miscellaneous.println(
                    " The txt file contains seperated section of a classfile.");
            Miscellaneous.exit();

        }
    }

    public static void main(String args[]) throws Exception {
        (new StartMeClassReader()).start(args);
    }
}
