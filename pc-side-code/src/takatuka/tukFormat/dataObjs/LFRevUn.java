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
package takatuka.tukFormat.dataObjs;

import java.io.*;

import takatuka.classreader.dataObjs.*;
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
public class LFRevUn extends Un {
    public static boolean bigEndian = false;
    public LFRevUn() throws Exception {
        super();
    }

    public LFRevUn(byte[] bytes) throws Exception {
        super(bytes);
    }

    public LFRevUn(int value) throws Exception {
        super(value);
    }


    private Un reverseUn(byte[] input) throws Exception {
        byte ret[] = new byte[input.length];
        for (int loop = 0; loop < ret.length; loop++) {
            ret[loop] = input[ret.length - 1 - loop];
        }
        return new Un(ret);
    }

    /**
     *
     * @param buff BufferedByteCountingOutputStream
     * @throws Exception
     */
    @Override
    public String writeSelected(BufferedByteCountingOutputStream buff) throws
            Exception {
        if (bigEndian) {
            return super.writeSelected(buff);
        }
        Un revUn = null;
        if (getData() != null) {
            revUn = reverseUn(getData());
        } else {
            return super.writeSelected(buff);
        }
        return revUn.writeSelected(buff);
    }

    public static void main(String args[]) throws Exception {
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        BufferedByteCountingOutputStream bOut = new
                                                BufferedByteCountingOutputStream(
                byteOutput);
        //(new LFRevUn(7)).writeSelected(bOut);
        byte b[] = {5, 3, 2};
        Miscellaneous.println(new LFRevUn(b));
        bOut.flush();
        bOut.close();
        byteOutput.close();
        Miscellaneous.println(byteOutput.toByteArray());

    }
}
