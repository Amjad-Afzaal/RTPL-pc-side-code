/*
 * Copyright 2009 Christian Schindelhauer, Peter Thiemann, Faisal Aslam, Luminous Fennell and Gidon Ernst.
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
package takatuka.offlineGC.DFA.logic.factory;

import java.util.*;
import java.util.ArrayList;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.dataObjs.constantPool.StringInfo;
import takatuka.classreader.logic.constants.JavaInstructionsOpcodes;
import takatuka.classreader.logic.constants.TagValues;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.GCInstruction;
import takatuka.offlineGC.generateInstrs.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.tukFormat.dataObjs.LFClassFile;
import takatuka.verifier.dataObjs.attribute.*;

/**
 *
 * @author Faisal Aslam
 */
public class NewIdAndObjectSizeUtil {

    public Vector getTopNIDCorrespToObjectSize(int n, int minSize) {
        List list = new ArrayList(createNewIdAndObjectSizeCollection());
        Collections.sort(list);
        Vector ret = new Vector();
        if (n > list.size()) {
            n = list.size();
        }
        LogHolder.getInstanceOf().addLogHeading("Selected New Instr With Object Size", LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
        for (int loop = 1; loop <= n; loop++) {
            NewIdAndObjectSize newIdAndObjectSize = (NewIdAndObjectSize) list.get(list.size() - loop);
            if (newIdAndObjectSize.objectSize <= minSize) {
                LogHolder.getInstanceOf().addLog(loop + ":  TOO SMALL ---- " + newIdAndObjectSize, LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
                break;
            }
            LogHolder.getInstanceOf().addLog(loop + ":  " + newIdAndObjectSize, LogRecord.LOG_FILE_FOR_OFFLINE_GC, false);
            ret.addElement(newIdAndObjectSize.newId);

        }
        return ret;
    }
    private class NewIdAndObjectSize implements Comparable<NewIdAndObjectSize> {

        int newId = 0;
        int objectSize = 0;

        public NewIdAndObjectSize(int newId, int objectSize) {
            this.newId = newId;
            this.objectSize = objectSize;
        }

        public int compareTo(NewIdAndObjectSize o) {
            return new Integer(objectSize).compareTo(o.objectSize);
        }

        @Override
        public String toString() {
            NewInstrIdFactory newIdFactory = NewInstrIdFactory.getInstanceOf();
            GCInstruction instr = (GCInstruction) newIdFactory.getInstrANewIdAssignedTo(newId);

            return "newId= " + newId + " object =" + objectName(instr) + ", object Size=" + objectSize;
        }

        private String objectName(GCInstruction instr) {
            Oracle oracle = Oracle.getInstanceOf();
            GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
            if (instr.getMnemonic().equals("NEW")) {
                int thisPointer = instr.getOperandsData().intValueUnsigned();
                ClassFile cFile = oracle.getClass(thisPointer, gcp);
                return cFile.getFullyQualifiedClassName();
            } else if (instr.getOpCode() == JavaInstructionsOpcodes.LDC
                    || instr.getOpCode() == JavaInstructionsOpcodes.LDC_W) {
                int cpIndex = instr.getOperandsData().intValueUnsigned();
                StringInfo stringInfo = (StringInfo) gcp.get(cpIndex, TagValues.CONSTANT_String);
                int utf8CPIndex = stringInfo.getIndex().intValueUnsigned();
                return "LDC: " + oracle.getUTF8(utf8CPIndex, gcp);
            }
            //System.err.println("error 12314 "+instr);
            //Miscellaneous.exit();
            return instr.getMnemonic();
        }
    }

    private Vector createNewIdAndObjectSizeCollection() {
        NewInstrIdFactory newInstrIdFactory = NewInstrIdFactory.getInstanceOf();
        Iterator<Integer> it = newInstrIdFactory.getAllNewIds().iterator();
        Vector ret = new Vector();
        while (it.hasNext()) {
            int newId = it.next();
            int objectSize = getSizeOfANewId(newId);
            ret.addElement(new NewIdAndObjectSize(newId, objectSize));
        }
        return ret;
    }

    private int getSizeOfANewId(int newId) {
        try {
            Oracle oracle = Oracle.getInstanceOf();
            NewInstrIdFactory newInstrIdFactory = NewInstrIdFactory.getInstanceOf();
            VerificationInstruction instr = newInstrIdFactory.getInstrANewIdAssignedTo(newId);
            if (instr.getOpCode() == JavaInstructionsOpcodes.NEW) {
                int thisClass = instr.getOperandsData().intValueSigned();
                LFClassFile cFileofObject = (LFClassFile) oracle.getClass("java/lang/Object");
                int objectClassSize = cFileofObject.getNonStaticFieldLength();
                LFClassFile cFile = (LFClassFile) oracle.getClass(thisClass,
                        GlobalConstantPool.getInstanceOf());
                if (cFile == null) {
                    return 0;
                }
                return cFile.getNonStaticFieldLength()-objectClassSize;
            } else if (instr.getOpCode() == JavaInstructionsOpcodes.LDC
                    || instr.getOpCode() == JavaInstructionsOpcodes.LDC_W) {
                int cpIndex = instr.getOperandsData().intValueUnsigned();
                GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
                StringInfo stringInfo = (StringInfo) gcp.get(cpIndex, TagValues.CONSTANT_String);
                int utf8CPIndex = stringInfo.getIndex().intValueUnsigned();
                return oracle.getUTF8(utf8CPIndex, gcp).length() * 2 + 4;
            } else {
                return 10;
            }
        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return 0;
    }
}
