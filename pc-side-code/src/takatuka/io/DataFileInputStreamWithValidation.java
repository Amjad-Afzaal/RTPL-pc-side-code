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
package takatuka.io;

import java.io.*;

/**
 * <p>Title: </p>
 *
 * <p>Description:
 * In case the stream is closed and file still has data then it throws Exception
 * Similarly if EOF has reached the it throws Exception
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class DataFileInputStreamWithValidation {
    DataInputStream dataStream = null;
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    public static final String EXTRA_BYTES =
            "Class file have extra bytes at the end or wrong format";
    public static final String TRUNCATED_BYTES = "Class file truncated";
    public DataFileInputStreamWithValidation(File file) throws
            FileNotFoundException {
        fis = new FileInputStream(file);
        bis = new BufferedInputStream(fis);
        dataStream = new DataInputStream(bis);
    }

    /* public int read(byte[] b) throws IOException {
         return dataStream.read(b);
     }*/

    public int read(byte[] b, int off, int len) throws IOException {
        int ret = dataStream.read(b, off, len);
        if (ret == -1) { //end of file reached.
            throw new VerifyError(TRUNCATED_BYTES);
        }
        return ret;
    }

    public void close() throws IOException {
        if (available() > 0) {
            throw new VerifyError(EXTRA_BYTES);
        }
        fis.close();
        bis.close();
        dataStream.close();
    }

    public int available() throws IOException {
        return dataStream.available();
    }


}
