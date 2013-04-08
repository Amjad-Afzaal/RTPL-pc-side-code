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
package takatuka.optimizer.VSS.dataObjs;

import java.util.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSLocalVariables extends LocalVariables {

    private HashMap<Integer, Type> maxBlocksPerElement = new HashMap<Integer, Type>();

    public SSLocalVariables(int maxSize) {
        super(maxSize);
    }

    @Override
    public Object clone() {
        SSLocalVariables obj = (SSLocalVariables) super.clone();
        obj.maxBlocksPerElement = maxBlocksPerElement;
        return obj;
    }

    @Override
    public void addVaraibles(Vector variables) {
        super.addVaraibles(variables);
        calculateMaxBlocksPerElement();
    }

    @Override
    public boolean merge(FrameElement localVars) {
        boolean ret = super.merge(localVars);
        calculateMaxBlocksPerElement();
        return ret;
    }

    @Override
    public int add(Type type) {
        int ret = super.add(type);
        calculateMaxBlocksPerElement();
        return ret;
    }

    @Override
    public Type set(int index, Type type) {
        Type ret = super.set(index, type);
        calculateMaxBlocksPerElement();
        return ret;
    }

    private void calculateMaxBlocksPerElement() {
        for (int loop = 0; loop < elements.size(); loop++) {
            Type currentType = (Type) elements.get(loop);
            if (!currentType.isReference()
                    && (currentType.getType() == Type.SPECIAL_TAIL
                    || currentType.getType() == Type.UNUSED)) {
                continue;
            }
            Type savedType = maxBlocksPerElement.get(loop);
            int size = 0;
            if (savedType != null) {
                size = savedType.getBlocks();
            }
            if (currentType.getBlocks() > size) {
                maxBlocksPerElement.put(loop, currentType);
            }
        }
    }

    public int getImprovement() {
        int totalSize = 0;
        Iterator<Type> it = maxBlocksPerElement.values().iterator();
        while (it.hasNext()) {
            int maxSizesStored = it.next().getBlocks();
            totalSize += maxSizesStored;
        }
        if (elements.size() > totalSize) {
            //Miscellaneous.println("It works -------------- " + totalSize + ", " + elements.size());
        } else if (elements.size() < totalSize) {
            Miscellaneous.printlnErr("Error #899");
            Miscellaneous.exit();
        }
        return elements.size() - totalSize;
    }

    /**
     *
     * @return what is the maximum size from the set of value stored in a local variable.
     */
    public HashMap<Integer, Type> getMaxBlockPerElement() {
        return maxBlocksPerElement;
    }
}
