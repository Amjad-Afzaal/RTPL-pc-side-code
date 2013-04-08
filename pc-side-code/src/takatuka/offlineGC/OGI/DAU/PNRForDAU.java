package takatuka.offlineGC.OGI.DAU;

import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.classreader.dataObjs.MethodInfo;
import takatuka.offlineGC.OGI.GraphUtils.CreateIMGraph;
import takatuka.offlineGC.DFA.dataObjs.attribute.*;
import takatuka.offlineGC.DFA.logic.factory.NewInstrIdFactory;
import takatuka.optimizer.cpGlobalization.logic.util.Oracle;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class PNRForDAU extends PNRAlgo {

    private static final PNRForDAU myObj = new PNRForDAU();

    /**
     *
     */
    protected PNRForDAU() {
    }

    /**
     *
     * @return
     */
    public static PNRForDAU getInstanceOf() {
        return myObj;
    }


    @Override
    protected GCInstruction isValidNodeToFreeRefat(InstrGraphNode gNode) {
        Oracle oracle = Oracle.getInstanceOf();
        CreateIMGraph imGraph = CreateIMGraph.getInstanceOf();
        String methodStr = oracle.getMethodOrFieldString(gNode.getMethod());
        GCInstruction instr = gNode.getInstruction().getNormalInstrs().firstElement();
        if (DAUAlgo.currentNewIdToFree == -1) {
            System.err.println("Error # 2252");
            System.exit(1);
        }
        int newId = DAUAlgo.currentNewIdToFree;
        MethodInfo method = NewInstrIdFactory.getInstanceOf().getMethodOfNewId(newId);
        String newIdMethodStr = oracle.getMethodOrFieldString(method);
        if (newIdMethodStr.equals(methodStr)) {
            return instr;
        } else if (!imGraph.isGraphCreatedMultipleTimes(methodStr)) {
            /**
             * TODO: above else if needed to be make better. The multiple time
             * should be in the subgraph where newId exist instead of complete graph
             * of the program.
             */
            return instr;
        }

        return null;
    }
}
