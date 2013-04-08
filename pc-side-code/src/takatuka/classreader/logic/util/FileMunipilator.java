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
package takatuka.classreader.logic.util;

import java.io.*;

/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */

public class FileMunipilator {

    /**
     * returns all the files (no directories) in given dir as well as in its sub directory.
     * each file path+name is followed by a \n and then another file path+name so on...
     * if extension is empty or null then it get all the files otherwise only 
     * return files with specific extensions only.
     * @param dir
     * @param extension
     * @return
     */
    public static String getAllFiles(String dir, String extension) {
        String ret = "";
        File file = new File(dir);
        if(!file.isDirectory()) {//assume it is directory
            return "";
        }
        File [] files = file.listFiles();
        for (int loop = 0; loop < files.length; loop ++) {
            file = files[loop];
            if (file.isDirectory()){
                ret = ret + getAllFiles(file.getAbsolutePath(), extension);
            }
            if (extension == null || extension.length()==0 ||
                    files[loop].getName().endsWith(extension))
            ret = ret + files[loop].getAbsolutePath()+"\n";
        }
        return ret;
    }
    
    public static void main(String str[]) {
        Miscellaneous.println(getAllFiles("C:/D_virtual/squawk/cldc/src", ".java"));
    }
}
