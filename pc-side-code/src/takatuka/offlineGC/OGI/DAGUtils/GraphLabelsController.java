package takatuka.offlineGC.OGI.DAGUtils;

import takatuka.offlineGC.OGI.PNR.PNRAlgo;
import java.io.*;
import java.util.*;
import takatuka.classreader.logic.*;
import takatuka.classreader.logic.util.*;
import takatuka.offlineGC.OGI.GraphUtils.INode;
import takatuka.offlineGC.OGI.GraphUtils.InstrGraphNode;
import takatuka.offlineGC.OGI.GraphUtil.FTT.*;
import takatuka.tukFormat.logic.file.*;

/**
 * <p>Title: </p>
 * <p>Description:
 * Contain lables of GraphNode and DAGNodes
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class GraphLabelsController {

    private static final GraphLabelsController myObj = new GraphLabelsController();
    private TreeMap<Long, String> labelsMapForGraphNode = new TreeMap<Long, String>();
    private TreeMap<Long, String> labelsMapForDAGNode = new TreeMap<Long, String>();
    private TreeMap<Long, String> labelsMapForMethodLevelGraphNodes = new TreeMap<Long, String>();
    private static final String DAG_NODE_FILE_NAME = "./dagNodeGraph.dot";
    private static final String GRAPH_NODE_FILE_NAME = "./nodeGraph.dot";
    private static final int GRAP_TYPE_METHODLEVEL = 2;
    private static final int GRAPH_TYPE_DAGNODE = 1;
    private static final int GRAPH_TYPE_INSTRLEVEL = 0;
    public static boolean printGraphInFile = false;

    private GraphLabelsController() {
    }

    public static GraphLabelsController getInstanceOf() {
        return myObj;
    }

    private void clear() {
        labelsMapForDAGNode.clear();
        labelsMapForGraphNode.clear();
    }

    public void addForMethodLevelGraph(long graphId, String label) {
        labelsMapForMethodLevelGraphNodes.put(graphId, label);
    }

    public void add(long graphId, String label, boolean isDAGNode) {
        if (!isDAGNode) {
            labelsMapForGraphNode.put(graphId, label);
        } else {
            labelsMapForDAGNode.put(graphId, label);
        }
    }

    /**
     *
     * @param type
     * @return
     */
    private String toStringGraphNode(int type) {
        String ret = "";
        TreeMap map = labelsMapForDAGNode;
        if (type == GRAPH_TYPE_INSTRLEVEL) {
            map = labelsMapForGraphNode;
        } else if (type == GRAP_TYPE_METHODLEVEL) {
            map = labelsMapForMethodLevelGraphNodes;
        }
        Iterator<Long> keyIt = map.keySet().iterator();
        while (keyIt.hasNext()) {
            long graphId = keyIt.next();
            ret += graphId + " [label=\"" + map.get(graphId) + "\"];\n";
        }
        return ret;
    }

    public void printGraphInFile(INode graph, String fileName) {
        if (!printGraphInFile) {
            return;
        }
        PNRAlgo.debugOn();
        clear();
        int graphType = GRAPH_TYPE_DAGNODE;
        if (graph instanceof InstrGraphNode) {
            graphType = GRAPH_TYPE_INSTRLEVEL;
        } else if (graph instanceof MethodLevelGraphNode) {
            graphType = GRAP_TYPE_METHODLEVEL;
        }
        fileName = StartMeAbstract.outputWriter.getOutputDirectory()
                + "/" + fileName;

        try {
            String str = "digraph G {\n";
            str += graph.toStringWithOutLables()
                    + "\n" + GraphLabelsController.getInstanceOf().
                    toStringGraphNode(graphType);
            str += "}";
            File file = new File(fileName);
            if (file.exists()) {
                file.delete();
            }
            LFWriter.writeFile(file, str);
            /*
            Runtime rt = Runtime.getRuntime();
            Process pro = rt.exec("/usr/bin/dot "+fileName+" -Tjpg -o "+fileName+".jpg");
            Thread.sleep(1000);
            pro.destroy();
             */

        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
        PNRAlgo.debug("Printed graphType =" + graphType + " in the file =" + fileName);
        PNRAlgo.debugBackToOriginal();

    }

    public void printGraphInFile(INode graph) {
        String fileName = DAG_NODE_FILE_NAME;
        if (graph instanceof InstrGraphNode) {
            fileName = GRAPH_NODE_FILE_NAME;
        } else if (graph instanceof MethodLevelGraphNode) {
            fileName = "methodLevel.dot";
        }
        printGraphInFile(graph, fileName);
    }
}
