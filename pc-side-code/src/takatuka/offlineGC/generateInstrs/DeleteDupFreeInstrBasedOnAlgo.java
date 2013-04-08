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
package takatuka.offlineGC.generateInstrs;

import java.util.*;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;

/**
 * 
 * If a newId is freed by FTT-PNR or DAU-PNR then remove it
 * from PNR. This is to reduce the number of free instructions generated.
 * 
 * @author Faisal Aslam
 */
public class DeleteDupFreeInstrBasedOnAlgo {

    public void execute() {
        HashMap<Integer, HashSet<FreeInstrRecord>> recordPerAlo =
                GCInstruction.getRecordOfFreeInstrPerAlgo();
        HashSet<FreeInstrRecord> toRemove = recordPerAlo.get(GCInstruction.DAU_PNR);
        toRemove.addAll(recordPerAlo.get(GCInstruction.FTT_PNR));
        //System.out.println("\n********** " + toRemove.size() + ":  " + toRemove);
        HashSet<FreeInstrRecord> removeFrom = recordPerAlo.get(GCInstruction.PNR);
        //System.out.println("\n\n\nPNR ********** " + removeFrom.size() + ":  " + removeFrom);
        Iterator<FreeInstrRecord> fttAndDAUIt = toRemove.iterator();
        while (fttAndDAUIt.hasNext()) {
            removeFrom = recordPerAlo.get(GCInstruction.PNR);
            FreeInstrRecord fttdauFreeInstr = fttAndDAUIt.next();
            FreeInstrRecord toRemRecord = containsNewId(removeFrom, fttdauFreeInstr);
            if (toRemRecord != null) {
                //System.out.println("\nfound  " + fttdauFreeInstr);
                boolean isRemoved = toRemRecord.getInstr().removeReferenceFreedOnMe(fttdauFreeInstr.getRef());
                if (!isRemoved) {
                    System.err.println("**** Some error # 682");
                    System.exit(1);
                }
            }
        }

        toRemove = recordPerAlo.get(GCInstruction.DAU_PNR);
        toRemove.addAll(recordPerAlo.get(GCInstruction.FTT_PNR));
        //System.out.println("\n\n size after = " + toRemove.size() + " --- " + toRemove);
        toRemove = recordPerAlo.get(GCInstruction.PNR);
        //System.out.println("\n\n size after PNR = " + toRemove.size() + " --- " + toRemove);

    }

    private FreeInstrRecord containsNewId(HashSet<FreeInstrRecord> freeInstrRecordSet, FreeInstrRecord toFind) {
        FreeInstrRecord ret = null;
        Iterator<FreeInstrRecord> it = freeInstrRecordSet.iterator();
        while (it.hasNext()) {
            FreeInstrRecord freeInstrRecord = it.next();
            if (freeInstrRecord.getRef().equals(toFind.getRef())) {
                //System.out.println(freeInstrRecord + " === " + toFind);
                return freeInstrRecord;
            }
        }
        return ret;
    }
}
