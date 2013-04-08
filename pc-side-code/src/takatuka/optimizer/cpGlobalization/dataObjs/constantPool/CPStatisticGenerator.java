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
package takatuka.optimizer.cpGlobalization.dataObjs.constantPool;

import java.util.*;
import java.io.*;
import takatuka.classreader.dataObjs.constantPool.base.*;
import takatuka.classreader.dataObjs.constantPool.*;
import takatuka.classreader.logic.*;
import takatuka.classreader.logic.constants.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.io.*;
import takatuka.tukFormat.dataObjs.*;
import takatuka.optimizer.cpGlobalization.dataObjs.constantPool.GCP.*;
import takatuka.optimizer.deadCodeRemoval.logic.cp.*;
import takatuka.classreader.logic.util.*;

/**
 * 
 * Description:
 * <p>
 * This class is for generating Statistic for the Constant Pool
 * 
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class CPStatisticGenerator {

    private static final String CONSTANT_POOL_ENTRIES_STAT_GROUP_KEY = "CONSTANT_POOL_ENTRIES";
    private static final String CONSTANT_POOL_SIZE_STAT_GROUP_KEY = "CONSTANT_POOL_SIZE";
    // entries count
    private static final String ORIGINAL_CP_ENTRIES_COUNT = "Total Constant Pool enteries";
    private static final String GCP_ENTRIES_COUNT = "Global CP enteries";
    private static final String TUK_CP_ENTRIES_COUNT = "Tuk CP enteries";
    private static final String Total_CP_ENTRIES_REDUCTION = "Total Constant Pool count reduction";    //size
    private static final String ORIGINAL_CP_SIZE = "Original CP size";
    private static final String GCP_SIZE = "Global CP size";
    private static final String TUK_CP_SIZE = "Tuk CP size";
    private static final String Total_CP_SIZE_REDUCTION = "Total Constant Pool size reduction";
    private long cacheGlobalPoolSize = 0;
    private static final StatsHolder statsHolder = StatsHolder.getInstanceOf();
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    private static final CPStatisticGenerator staticGenerator = new CPStatisticGenerator();

    public static CPStatisticGenerator getInstanceOf() {
        return staticGenerator;
    }

    private void produceEntriesCountStats() {
        double tukCPEntries = poolsAggregatedCount(true);
        double totalCPOriginalEntries = GlobalConstantPool.getInstanceOf().
                getCountOfOriginalCPEntries();
        double globalCPEntries = poolsAggregatedCount(false);
        double totalReduction = 100 * ((totalCPOriginalEntries - tukCPEntries) / totalCPOriginalEntries);
        statsHolder.addStat(CONSTANT_POOL_ENTRIES_STAT_GROUP_KEY,
                ORIGINAL_CP_ENTRIES_COUNT, totalCPOriginalEntries);
        statsHolder.addStat(CONSTANT_POOL_ENTRIES_STAT_GROUP_KEY,
                GCP_ENTRIES_COUNT, globalCPEntries);
        statsHolder.addStat(CONSTANT_POOL_ENTRIES_STAT_GROUP_KEY,
                TUK_CP_ENTRIES_COUNT, tukCPEntries);
        statsHolder.addStat(CONSTANT_POOL_ENTRIES_STAT_GROUP_KEY,
                Total_CP_ENTRIES_REDUCTION, 
                StartMeAbstract.roundDouble(totalReduction,2)+"%");

    }

    public void execute() {
        produceEntriesCountStats();
        produceSizesStats();
    }

    private void produceSizesStats() {
        InfoBase.printTag(false);
        double totalOriginalCPSize = GlobalConstantPool.getInstanceOf().
                totalOriginalSizeOfCPInBytes();
        double tukCPSize = poolsAggregatedSize(true);
        statsHolder.addStat(CONSTANT_POOL_SIZE_STAT_GROUP_KEY,
                ORIGINAL_CP_SIZE, 
                (totalOriginalCPSize-DCRFromCP.getInstanceOf().getSizeOfCPEntriesRemove()) + "");
        statsHolder.addStat(CONSTANT_POOL_SIZE_STAT_GROUP_KEY,
                GCP_SIZE,
                cacheGlobalPoolSize + "");
        statsHolder.addStat(CONSTANT_POOL_SIZE_STAT_GROUP_KEY,
                TUK_CP_SIZE,
                tukCPSize + "");
        double totalReduction = 100 * ((totalOriginalCPSize - tukCPSize) / totalOriginalCPSize);
        statsHolder.addStat(CONSTANT_POOL_SIZE_STAT_GROUP_KEY,
               Total_CP_SIZE_REDUCTION,
               StartMeAbstract.roundDouble(totalReduction,2)+"%");
        logHolder.addLog(Total_CP_SIZE_REDUCTION + "=" +
               StartMeAbstract.roundDouble(totalReduction, 2) + "%", true);
    }

    public void cacheStatisticsJustAfterGlobalization() {
        cacheGlobalPoolSize = poolsAggregatedSize(false);
    }

    private long poolsAggregatedCount(boolean AfterLoadingFormat) {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        TreeSet set = pOne.getPoolIds();
        Iterator it = set.iterator();
        long totalSize = 0;

        while (it.hasNext()) {
            Integer poolId = (Integer) it.next();
            if (!AfterLoadingFormat) {
                totalSize += pOne.getAll(poolId).size();
            } else {
                if ((poolId != TagValues.CONSTANT_NameAndType &&
                        poolId != TagValues.CONSTANT_Utf8 &&
                        poolId != TagValues.CONSTANT_InterfaceMethodref)) {
                    totalSize += pOne.getAll(poolId).size();

                    if (poolId == TagValues.CONSTANT_String) {
                        totalSize += pOne.getAll(poolId).size();
                    }
                }
            }
        }

        return totalSize - 1; //for classInfo
    }

    private long poolsAggregatedSize(boolean afterLoadingFormat) {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        TreeSet set = pOne.getPoolIds();
        Iterator it = set.iterator();
        long totalSize = 0;

        while (it.hasNext()) {
            Integer poolId = (Integer) it.next();
            totalSize += poolSize(poolId, afterLoadingFormat);
        }
        return totalSize;
    }

    private long poolSize(int poolId, boolean afterLoadingFormat) {
        GlobalConstantPool pOne = GlobalConstantPool.getInstanceOf();
        long size = 0;
        try {
            ByteArrayOutputStream bOut = new ByteArrayOutputStream();
            BufferedByteCountingOutputStream countBuff = new BufferedByteCountingOutputStream(bOut);
            for (int loop = 0; loop < pOne.getCurrentSize(poolId); loop++) {
                Object obj = pOne.get(loop, poolId);
                if (obj instanceof EmptyInfo) {
                    continue;
                }
                LFBaseObject lfBaseObj = (LFBaseObject) obj;
                if (afterLoadingFormat) {
                    lfBaseObj.writeSelected(countBuff);
                } else {
                    lfBaseObj.superWriteSelected(countBuff);
                }
            }
            size = countBuff.numberOfBytesWritten();
            bOut.close();
            countBuff.close();

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        return size;
    }

}
