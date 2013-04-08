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
package takatuka.optimizer.deadCodeRemoval.logic.cp;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * To remove the non-referred entries from constant pools.
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class DCRFromCP {

    private static final DCRFromCP dcrFromCP = new DCRFromCP();
    private static final GlobalConstantPool gcp = GlobalConstantPool.getInstanceOf();
    private int totalRemoved = 0;
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    private static final StatsHolder statHolder = StatsHolder.getInstanceOf();
    private int sizeOfEntriesRemoved = 0;

    private DCRFromCP() {
    //no one create me but me.
    }

    public static final DCRFromCP getInstanceOf() {
        return dcrFromCP;
    }

    public int getTotalRemove() {
        return totalRemoved;
    }

    /**
     * Go through all the CP entries of type References, Integer, Long, Float, Double
     * 
     * If an entry is not referrred from CP and also from bytecode the replace it 
     * with empty info
     */
    public void execute() {
        try {
            execute(TagValues.CONSTANT_Class);
            execute(TagValues.CONSTANT_Fieldref);
            execute(TagValues.CONSTANT_Methodref);
            execute(TagValues.CONSTANT_Double);
            execute(TagValues.CONSTANT_Long);
            execute(TagValues.CONSTANT_Integer);
            execute(TagValues.CONSTANT_Float);
            execute(TagValues.CONSTANT_String);

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        logHolder.addLog("Dead-code-Removal: Total Constant Pool entries removed = " + totalRemoved, true);
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "CP entries removed", totalRemoved);
        statHolder.addStat(StatGroups.DEAD_CODE_REMOVAL,
                "total Size of CP entries removed", sizeOfEntriesRemoved);
    }

    public int getSizeOfCPEntriesRemove() {
        return sizeOfEntriesRemoved;
    }
    
    public void execute(int tag) throws Exception {
        int countLocal = 0;
        for (int loop = 0; loop < gcp.getAll(tag).size(); loop++) {
            //ClassFile.currentClassToWorkOn = gcp.getClass(loop, tag);
            if (gcp.getReferenceCount(loop, tag) == 0 &&
                    gcp.getReferredCountFromCPObjects(loop, tag) == 0) {
                ClassFile.currentClassToWorkOn = gcp.getClass(loop, tag);
                sizeOfEntriesRemoved += gcp.replaceWithEmptyInfo(loop, tag);
                totalRemoved++;
                countLocal++;
            }
        }
        logHolder.addLog(tag + ":" + "Total Constant Pool entries removed = " + countLocal);
    }
}
