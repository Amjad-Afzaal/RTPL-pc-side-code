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
 * <p>Description: </p>
 * @author Faisal Aslam
 * @version 1.0
 */

public class BufferedByteCountingOutputStream extends BufferedOutputStream {
    private int byteCount = 0;
    private boolean allowedFun = false;
    private int markCount = 0;
    public BufferedByteCountingOutputStream(OutputStream out) {
        super(out);
    }

    public BufferedByteCountingOutputStream(OutputStream out, int size) {
        super(out, size);
    }

    /**
     * record the current point of buffer
     */
    public void mark() {
        markCount = byteCount;
    }
    
    /**
     * return bytes from the marked point
     * @return
     */
    public byte[] getFromMarked() {
        byte[] ret = new byte[buf.length - markCount];
        for (int loop = markCount; loop < buf.length; loop ++) {
            ret[loop-markCount] = buf[loop];
        }
        return ret;
    }
    /**
     * All the write should be done only using this function.
     * @param b byte[]
     * @throws IOException
     */
    @Override
    public void write(byte[] b) throws IOException {
        allowedFun = true;
        if ((byteCount % 5000) == 0) {
            //this.flush(); //automatic flush... I hope the super class will have similar automatic flush???
        }
        byteCount += b.length;
        super.write(b);
        allowedFun = false;
    }

    public int numberOfBytesWritten() {
        return byteCount;
    }

    /**
     * Note that the following function is not implemented and throws UnsupportedOperationException
     * @param b int
     * @throws IOException
     */
    @Override
    public void write(int b) throws IOException {
        throw new UnsupportedOperationException();
    }

    /**
     * Note that the following function is not implemented and throws UnsupportedOperationException
     * @param b byte[]
     * @param off int
     * @param len int
     * @throws IOException
     */
    @Override
    public void write(byte[] b,
                      int off,
                      int len) throws IOException {
        if (allowedFun) { //only allowed to be called from within write(byte) function otherwise not.
            super.write(b, off, len);
        } else {
            throw new UnsupportedOperationException(
                    "Using that function is not allowed.");
        }
    }

}
