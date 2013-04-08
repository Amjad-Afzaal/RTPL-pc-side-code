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
package takatuka.optimizer.bytecode.changer.logic.comb;

import takatuka.classreader.dataObjs.attribute.*;
import java.util.*;
import takatuka.optimizer.bytecode.changer.logic.freq.*;
import takatuka.classreader.logic.logAndStats.LogHolder;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.vm.autoGenerated.vmSwitch.*;
import takatuka.classreader.logic.util.*;
/**
 * 
 * Description:
 * <p>
 *  1. Takes CodeAtt as a parameter
 *  2. Go through each instruction one by one and  
 *  3. apply the algorithm defined below to create a tree 
 *  4. After all Instructions visited select a path with maximum savings.
 *  5. Based on the path change the instructions.
 *   --- also change the branch addresses accordingly
 *   --- set the code_length in the code attribute
 * 
 * Algorithm in short:
 * 1. If instruction is first instruction then it is ROOT of the tree
 * 2. Else if instruction is i_th instruction then we have to see how to combine it
 * with previous instruction leafs, in all possible way. 
 * 3. Each combination make a new leaf in the tree. 
 * In case we decide not to combine then we also make a new leaf with single new
 * instruction.
 * -- removal phase
 * 4. Each combine result in future of leave.
 * 5. Each leave may result in future combine of degree n, n-1 ... leave.
 * 6. Hence we group instruction base on their future as n, n-1, .... leave 
 *  
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class ByteCodeCombiner {

    private int newTotalCodeAttSize = 0;
    private int originalTotalCodeAttSize = 0;
    
    private int level = 0;
    private TreeNode root = null;
    private Vector currentLeafs = new Vector();
    private static final ByteCodeCombiner main = new ByteCodeCombiner();
    private int numberOfFunctions = 0;
    private RegisterCustomInstruction regCustInst = RegisterCustomInstruction.getInstanceOf();
    
    private static final LogHolder logHolder = LogHolder.getInstanceOf();
    
    public static ByteCodeCombiner getInstanceOf() {
        return main;
    }

    private void init() {
        currentLeafs = new Vector();
        root = null;
        level = 0;

    }

    /**
     * From here we start ...
     */
    public void execute() {
        numberOfFunctions = 0;
        if (regCustInst.numberOfOpCodesavailable() == 0) {
            return;
        }
        logHolder.addLog("optimizing bytecode ... for combination size=" +
        InstructionFrequency.numberOfInstructionCombined + ", for # of combinations =" +
        FreqMap.getInstanceOf().size());
        try {
            CodeAtt codeAtt = null;
            TreeNode bestLeaf = null;
            Vector newInstructions = new Vector();
            Vector<CodeAttCache> codeAttVec = Oracle.getInstanceOf().getAllCodeAtt();
            int size = codeAttVec.size();
            for (int loop = 0; loop < size; loop++) {
                
                init();
                codeAtt = (CodeAtt)codeAttVec.get(loop).getAttribute();
                if (codeAtt == null || codeAtt.getCodeLength().intValueUnsigned() == 0) {
                    continue;
                }
                
                //now use above leaf to get the path with max saving 
                bestLeaf = getBestLeaf(codeAtt);
                if (bestLeaf == null) {
                    continue;
                }
                //Miscellaneous.println(" ******* BEST LEAF *****\n" + printBestLeaf(bestLeaf, bestLeaf.getTotalSavings()));
                newInstructions = bestLeaf.getRootPathValues();
                newInstructions.remove(0); //as root is empty
                setOpcodes(newInstructions);
                codeAtt.setInstructions(newInstructions);
                numberOfFunctions++;
                //Miscellaneous.println("done with ="+numberOfFunctions);
                if ((numberOfFunctions % 500) == 0 && numberOfFunctions > 0) {
                    logHolder.addLog("bytecode optimized so far " + numberOfFunctions + " functions ....", true);
                }
            }

        } catch (Exception d) {
            d.printStackTrace();
            Miscellaneous.exit();
        }
        //Miscellaneous.println("Maximum combinations size = "+InstructionFrequency.numberOfInstructionCombined);
        
        logHolder.addLog("Combined multiple instructions, new insructions =" +
                FreqMap.getInstanceOf().getAllKeys().size() +
                ", total Savings=" + (this.originalTotalCodeAttSize - this.newTotalCodeAttSize) + "...");
        init(); // for garbage collection 
    }

    private void setOpcodes(Vector<Instruction> instructions) {
        
        for (int loop = 0; loop < instructions.size(); loop ++) {
            Instruction inst = instructions.elementAt(loop);
            if(Instruction.getOpcode(inst.getMnemonic()) == -1) {
                regCustInst.register(inst, 
                        ((InstructionsCombined)inst).getSimpleInstructions());
            }
        }
    }
    private String printBestLeaf(TreeNode leaf, int totalSavings) {

        String ret = leaf.toString();

        if (leaf.getParent() != null) {
            ret = printBestLeaf(leaf.getParent(), totalSavings) + ", " + ret;
        } else {
            ret = totalSavings + ", " + ret;
        }
        return ret;
    }
    
    private TreeNode getBestLeaf(CodeAtt code) {
        if (code == null) {
            return new TreeNode(null, new InstructionsCombined());
        }
        Vector instVec = code.getInstructions();
        Instruction inst = null;
        //logHolder.addLog("creating Tree  "+instVec.size());
        for (int loop = 0; loop < instVec.size(); loop++) {
            inst = (Instruction) instVec.elementAt(loop);
            //Miscellaneous.println(" \n****************************** \n");
            //Miscellaneous.println("Instruction  :" + inst.getOpCode() + ", " + inst.getMnemonic());
            treeMaker(inst);
            //printLeaves();
            discardLeaf();
            //printLeaves();
            //Miscellaneous.println(loop+", ------------------- ");
            level++;
        }
        //logHolder.addLog(" Tree is created");
        TreeNode leaf = getLeafWithMaxSavings();
        //Miscellaneous.println("******** best at the end *********  ="+printBestLeaf(leaf, leaf.getTotalSavings()));
        return leaf;
    }

    private void combineNodesCreator(TreeNode leaf, Vector futureLeafs,
            Instruction inst) {
        InstructionsCombined instCombine = new InstructionsCombined();
        int loopEnd = leaf.getNumberOfLeaves();
        instCombine.addInstructionRevOrder(inst);
        for (int leaveLoop = 0; leaveLoop < loopEnd && leaveLoop <=
                InstructionFrequency.numberOfInstructionCombined; leaveLoop++) {
            if (leaveLoop == 0) {
                //Miscellaneous.println(" here 1 ="+leaf.getFirstInstruction());
                instCombine.addInstructionRevOrder(leaf.getFirstInstruction());
            } else {
                leaf = leaf.getParent();
                //Miscellaneous.println("here 2 ="+leaf.getFirstInstruction());
                instCombine.addInstructionRevOrder(leaf.getFirstInstruction());
            }
            futureLeafs.addElement(new TreeNode(leaf.getParent(), instCombine));
        }        
    }

    private String printTree(Vector nodes, int level) {
        String ret = "level =" + level + ", ";
        level++;
        Vector uniqueParent = new Vector();
        TreeNode node = null;
        for (int loop = 0; loop < nodes.size(); loop++) {
            if (loop != 0) {
                ret = ret + ",";
            }
            node = (TreeNode) nodes.elementAt(loop);
            ret = ret + node;
            if (node.getParent() != null &&
                    !uniqueParent.contains(node.getParent())) {
                uniqueParent.addElement(node.getParent());
            }
        }
        ret = ret + "\n";
        Vector dummy = new Vector();
        for (int loop = 0; loop < uniqueParent.size(); loop++) {
            dummy.addElement((TreeNode) uniqueParent.elementAt(loop));
            ret = ret + printTree(dummy, level);
        }
        return ret;
    }

    private void treeMaker(Instruction inst) {
        //Miscellaneous.println(" current Instruction = "+ inst.getMnemonic()+", "+
        //      inst.getOpCode());
        Vector futureLeafs = new Vector();

        if (root == null) {
            TreeNode emptyParent = new TreeNode(null, new InstructionsCombined());
            InstructionsCombined instComb = new InstructionsCombined();
            instComb.addInstruction(inst);
            root = new TreeNode(emptyParent, instComb);
            futureLeafs.addElement(root);
        //Miscellaneous.println("me me"+root);
        } else {
            for (int loop = 0; loop < currentLeafs.size(); loop++) {
                TreeNode leaf = (TreeNode) currentLeafs.elementAt(loop);

                //add leave instruction
                InstructionsCombined instComb = new InstructionsCombined();
                instComb.addInstruction(inst);
                futureLeafs.addElement(new TreeNode(leaf, instComb));

                //add all combine Instuction 
                combineNodesCreator(leaf, futureLeafs, inst);

            }
        }
        currentLeafs = futureLeafs;
    }

    private void printLeaves() {
        String ret = "Level =" + level + ", ";
        for (int loop = 0; loop < currentLeafs.size(); loop++) {
            if (loop != 0) {
                ret = ret + ", ";
            }
            ret = ret + currentLeafs.elementAt(loop);
        }
        //Miscellaneous.println(ret);
    }

    private void discardLeaf() {
        HashMap categorySaving = new HashMap();
        //round one
        for (int loop = 0; loop < currentLeafs.size(); loop++) {
            TreeNode node = (TreeNode) currentLeafs.elementAt(loop);

            InstructionsCombined combValue = node.getValue();
            int category = node.getNumberOfLeaves();
            if (category > InstructionFrequency.numberOfInstructionCombined) {
                category = InstructionFrequency.numberOfInstructionCombined;
            }
            if (InstructionFrequency.lengthOfInstrctionsCombined(node.getValue().
                    getInstructions()) > InstructionFrequency.numberOfInstructionCombined ||
                    node.getTotalSavings() < 0 || !combValue.isValidCombination()) {
                continue;
            }
            TreeNode savedNode = (TreeNode) categorySaving.get(category);
            if (savedNode == null ||
                    savedNode.getTotalSavings() < node.getTotalSavings()) {
                categorySaving.put(category, node);
            }
        }
        this.currentLeafs = new Vector(categorySaving.values());
    }

    private TreeNode getLeafWithMaxSavings() {
        if (currentLeafs.size() == 0) {
            return null;
        }
        TreeNode maxSavingsNode = (TreeNode) currentLeafs.elementAt(0);
        for (int loop = 0; loop < currentLeafs.size(); loop++) {
            TreeNode node = (TreeNode) currentLeafs.elementAt(loop);
            if (node.getTotalSavings() > maxSavingsNode.getTotalSavings()) {
                maxSavingsNode = node;
            }
        }
        return maxSavingsNode;
    }
}