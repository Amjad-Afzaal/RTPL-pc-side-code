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
package takatuka.offlineGC.OGI.FTT;

import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.DAGUtils.GraphLabelsController;
import takatuka.offlineGC.OGI.GraphUtils.CreateIMGraph;
import takatuka.offlineGC.OGI.PNR.*;
import java.util.*;
import takatuka.classreader.logic.logAndStats.*;
import takatuka.offlineGC.DFA.dataObjs.virtualThreading.*;
import takatuka.offlineGC.OGI.factory.*;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.offlineGC.DFA.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class FTTAlgoBase extends PNRBase {

    private static final FTTAlgoBase myObj = new FTTAlgoBase();

    /**
     *
     */
    protected FTTAlgoBase() {
    }

    /**
     *
     * @return
     */
    public static FTTAlgoBase getInstanceOf() {
        return myObj;
    }

    @Override
    public void execute() {
        setFactory();
        IFactory factory = FactoryPlaceHolder.getInstanceOf().getCurrentFactory();
        if (MethodLevelGraphController.getInstanceOf().size() == 0) {
            VirtualThreadController vContr = VirtualThreadController.getInstanceOf();
            Collection<VirtualThread> vThreadCollection = vContr.getAllFinishedThreads();
            Iterator<VirtualThread> it = vThreadCollection.iterator();
            CreateIMGraph createGraph = (CreateIMGraph) factory.createGraph();
            LogHolder.getInstanceOf().addLog(" Total Number of Theads =" + vThreadCollection.size(), false);
            while (it.hasNext()) {
                VirtualThread vThread = it.next();
                anyThingWithVirtualThread(vThread);
                InstrGraphNode interMethodGraph = createGraph.createInterMethodGraph(vThread, true);
                LogHolder.getInstanceOf().addLog(" created inter method graph ", false);

                GraphLabelsController.getInstanceOf().printGraphInFile(interMethodGraph);

                LogHolder.getInstanceOf().addLog("Done with thread # " + vThread, false);

            }
        }
        HashSet<TTReference> allRefFreed = new HashSet<TTReference>();
        execute(null, allRefFreed);
    }

    @Override
    protected void execute(InstrGraphNode sourceGraphNode,
            HashSet<TTReference> refFreed) {
        FTTAlgo.getInstanceOf().execute();
    }
}


