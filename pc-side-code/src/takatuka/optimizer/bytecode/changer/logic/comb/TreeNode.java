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
import org.apache.commons.lang.builder.*;
/**
 * 
 * Description:
 * <p>
 *
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */

public class TreeNode {
    private TreeNode parent = null;
    //private Vector children = new Vector(); //has TreeNodes in it...
    private InstructionsCombined value = null;
    private int numberOfLeaves = 0;
    private int savingCache = -1;
    
    public TreeNode(TreeNode parent, InstructionsCombined value) {
        this.parent = parent;
        this.value = (InstructionsCombined)value.clone();
        //Miscellaneous.println("here print "+value.getInstructions().size());
        if (value.getInstructions().size() > 1) {
            numberOfLeaves = 0; //combined
        } else if (parent != null){
            numberOfLeaves = parent.numberOfLeaves + 1;
        }
    }

    @Override    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TreeNode)) {
            return false;
        }
        TreeNode input = (TreeNode)obj;
        if (input.value.equals(value) && 
                ((input.getParent() == null && parent == null) || 
                (input.getParent()!= null && parent != null &&
                input.getParent().equals(parent)))) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
       return new HashCodeBuilder().append(value).append(parent).toHashCode();
    }
    
    public InstructionsCombined getValue() {
        return value;
    }
    
    public Instruction getFirstInstruction() {
        return (Instruction)value.getInstructions().elementAt(0);
    } 
    
    public TreeNode getParent() {
        return parent;
    }
    
    public int getNumberOfLeaves() {
        return numberOfLeaves;
    }
    
    /*public int addChild(Object child) {
        children.addElement(child);
        return children.size();
    }*/
    
    /**
     * It returns all nodes from root to the current node (including current node)
     * The order is reverse. It implies that current node is at first position and
     * root is at the last position of the vector. 
     * @return
     */
    public Vector getRootPath() {
        Vector allParents = new Vector();
        TreeNode current = this;
        allParents.addElement(current);
        while (current.getParent() != null) {
            current= current.getParent();
            allParents.addElement(current);
        }
        return allParents;
    }
    /**
     * It return Vector of values from root to this node
     * The value the root node is at the TOP and value of current node is at 
     * the bottom of returned vector
     * @return
     */
    public Vector getRootPathValues() {
        Vector parents = getRootPath();
        Vector retValues = new Vector();
        TreeNode currentNode = null;
        for (int loop = parents.size()-1; loop >= 0; loop --) {
            currentNode = (TreeNode)parents.elementAt(loop);
            retValues.addElement(currentNode.value);
        }
        return retValues;
    }
    
    //public Vector getChildren() {
      //  return children;
    //}
    
    public int getTotalSavings() {
        if (savingCache >= 0) {
            return savingCache;
        }
        savingCache = value.getSavings();        
        if (savingCache < 0) {
            return savingCache;
        }
        if (parent != null) {
            savingCache += parent.getTotalSavings();
        }
        return savingCache;
    }
    
    
    @Override
    public String toString() {
        return  "{"+value.getMnemonic()+ ", "+numberOfLeaves+ ", "+getTotalSavings()+"} ";
    }
}
