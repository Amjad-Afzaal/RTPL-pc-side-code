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
package takatuka.optimizer.cpGlobalization.logic;


import takatuka.classreader.logic.file.*;
import takatuka.optimizer.cpGlobalization.logic.factory.*;
import takatuka.optimizer.cpGlobalization.logic.file.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.factory.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.deadCodeRemoval.logic.*;
import takatuka.classreader.logic.util.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: It is from where we start.... </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class StartMeGCP extends StartMeDCR {

    private FactoryFacade factory = null;
    private final static LogHolder logHolder = LogHolder.getInstanceOf();

    public StartMeGCP() {
        super();
    }

    @Override
    public void setOutputWriter(String args[]) throws Exception {
        super.outputWriter = new OptimClassFileWriter(args[1]);
    }

    @Override
    public void setFactoryFacade() {
        super.setFactoryFacade();
        FactoryPlaceholder.getInstanceOf().setFactory(OptimFactoryFacade.getInstanceOf());
    }

    @Override
    public void read(String args[]) throws Exception {
        if (args.length >= 3) {
            ClassFileReader.filesToRead = Integer.parseInt(args[2]) + 1;
        }
        super.read(args);
    }

    @Override
    public void execute(String args[]) throws Exception {
        super.execute(args);
        logHolder.addLog("Started Constant Pool Globalization");
        factory = FactoryPlaceholder.getInstanceOf().getFactory();

        logHolder.addLog("Globalization Phase One .... ");
        ((Phase) GlobalConstantPool.getInstanceOf()).execute();

        logHolder.addLog("Globalization Phase Two .... ");
        (PhaseTwo.getInstanceOf()).execute();
        if (true) {
            logHolder.addLog("Globalization Phase Three .... ");
            (PhaseThree.getInstanceOf()).execute();

            logHolder.addLog("Globalization Phase Four .... ");
            (PhaseFour.getInstanceOf()).execute();
        }

        logHolder.addLog("Done with Constant Pool Globalization", true);
       // DCRFromCP.getInstanceOf().execute();
    }

    @Override
    public void validateInput(String args[]) throws Exception {
        if (args.length < 2 || args.length > 3) {
            Miscellaneous.println(
                    "Usage StartMe<whatever> <input file-name/directory-Name> <output directory-Name> <Optional: Max classes to read from input dir>");
            Miscellaneous.println(
                    " If the input file is a directory then it recursively traverse it");
            Miscellaneous.println(
                    " The program will stop on the first file, not conforms with class-file-format");
            Miscellaneous.println(
                    " The output directory will have classfile.class and classfile.txt files. ");
            Miscellaneous.println(
                    " The txt file contains seperated section of a classfile.");
            Miscellaneous.println("(OPTIONAL:) Maximum number of class files to read");
            Miscellaneous.exit();
        }
    }

    public static void main(String args[]) throws Exception {
        (new StartMeGCP()).start(args);
    }
}
