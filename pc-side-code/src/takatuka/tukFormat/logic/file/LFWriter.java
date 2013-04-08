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
package takatuka.tukFormat.logic.file;

import java.io.*;

import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.constantpool.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.file.*;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * @author Faisal Aslam
 * @version 1.0
 */
public class LFWriter extends OptimClassFileWriter {


    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    public LFWriter(String outputDirectory) throws Exception {
        super(outputDirectory);
    }

    /**
     * return the name and path of the TUK File.
     * @return
     */
    public static String getTukFileNamePath() {
        return getOutputDirectory() + "/0.tuk";
    }

    @Override
    protected String writeGlobalPool(BufferedByteCountingOutputStream buff) throws
            FileNotFoundException,
            IOException,
            Exception {
        String ret = "";
        logHolder.addLog("Writing common constant pool");
        //first print the header and then the constant pool
        CPHeader.getInstanceof().populateAddress();
        ret = ret + CPHeader.getInstanceof().writeSelected(buff);
        ret = ret + GlobalConstantPool.getInstanceOf().writeSelected(buff);
        buff.flush();

        return ret;
    }


}