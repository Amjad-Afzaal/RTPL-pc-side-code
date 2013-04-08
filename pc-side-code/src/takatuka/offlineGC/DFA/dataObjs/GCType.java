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
package takatuka.offlineGC.DFA.dataObjs;

import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.cpGlobalization.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.logic.factory.*;

/**
 * 
 * Description:
 * <p>
 * </p> 
 * @author Faisal Aslam
 * @version 1.0
 */
public class GCType extends Type {

    private HashSet<TTReference> refereces = new HashSet<TTReference>();
    private MethodInfo method = null;
    private Vector callingParams = null;
    private GCType childClone = null;

    /**
     *
     * @param input int
     * @param isReference boolean
     */
    public GCType(int type, boolean isReference, int newId) {
        super(type, isReference, newId);
        if (isReference) {
            refereces.add(new TTReference(type, newId));
            this.type = 0;
        }
    }

    public GCType(boolean isReference) {
        super(isReference);
    }

    public GCType getChildClone() {
        return childClone;
    }

    /**
     * create a input which is not reference and not array. The reference input is
     * set to VOID
     */
    public GCType() {
        super();
    }

    public GCType(int type) {
        super(type);
    }

    public void deleteAllRef() {
        refereces.clear();
    }
    public void delete(HashSet<TTReference> refToBeDeleted) {
        if (!isReference) {
            return;
        }
        refereces.removeAll(refToBeDeleted);
    }



    private void copy(GCType input) {
        isReference = input.isReference();
        isArray = input.isArrayReference();
        this.refereces = new HashSet<TTReference>();
        this.refereces.addAll(input.refereces);
        this.type = input.type;
        this.value = input.value;
        //does not copy method and calling parameters
    }

    @Override
    public Object clone() {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        GCType ret = (GCType) frameFactory.createType();
        try {
            ret.type = type;
            ret.isReference = isReference;
            ret.isArray = isArray;
            ret.value = value;
            this.childClone = ret;
            ret.refereces.addAll((Collection<TTReference>) this.refereces.clone());
        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
        return ret;
    }

    public HashSet<Integer> getAllNewIds() {
        HashSet<Integer> set = new HashSet();
        Iterator<TTReference> it = refereces.iterator();
        while (it.hasNext()) {
            TTReference ref = it.next();
            set.add(ref.getNewId());
        }
        return set;
    }

    @Override
    public HashSet<Integer> getRefClassThisPtr() {
        HashSet<Integer> set = new HashSet();
        Iterator<TTReference> it = refereces.iterator();
        while (it.hasNext()) {
            TTReference ref = it.next();
            set.add(ref.getClassThisPointer());
        }
        return set;
    }

    public static GCType addReference(GCType oldType, int classId, int newId) throws Exception {
        if (!oldType.isReference) {
            throw new Exception("Only reference could have multiple types associated with it");
        }
        GCType ret = (GCType) oldType.clone();
        ret.refereces.add(new TTReference(classId, newId));
        return ret;
    }

    /**
     * add reference of two types and return result as a new input
     * @param type1
     * @param type2
     * @return
     * @throws java.lang.Exception
     */
    public static GCType addReferences(GCType type1, GCType type2) throws Exception {
        if (!type1.isReference || !type2.isReference) {
            throw new Exception("Only reference could have multiple types associated with it");
        }
        GCType ret = (GCType) type1.clone();
        ret.refereces.addAll((Collection<TTReference>) type2.refereces.clone());
        ret.type = 0;
        return ret;
    }

    /**
     * 
     * @param ref
     */
    public void addReference(TTReference ref) {
        this.refereces.add(ref);
    }

    /**
     * 
     * @param input
     */
    public void addReferences(HashSet input) {
        this.refereces.addAll(input);
    }

    /**
     * 
     * @return
     */
    public HashSet<TTReference> getReferences() {
        return (HashSet) refereces.clone();
    }

    @Override
    public int getType() {
        if (isReference) {
            throw new UnsupportedOperationException();
        } else {
            return type;
        }
    }

    @Override
    public int setReferenceType(String name, int refIndex) {
        int cur = refIndex;
        int start = cur + 1;
        String refName = name.substring(start, name.indexOf(";", start));
        int localType = Oracle.getInstanceOf().getClassInfoByName(refName);
        if (localType <= 0) {
            //todo  it could be a reference of premitive types.
            //ERROR
            //Miscellaneous.printlnErr("Error #089. Exitings..."+refName);
            //Oracle.getInstanceOf().getClassInfoByName(refName);
            //new Exception().printStackTrace();
            //Miscellaneous.exit();
        }
        try {
            this.isReference = true;
            addReference(this, localType, -1);
        } catch (Exception ex) {
            ex.printStackTrace();
            Miscellaneous.exit();
        }
        return name.indexOf(";", start);
    }

    @Override
    public void setType(int type, boolean isReference) {
        if (isReference) {
            //use add referemce
            throw new UnsupportedClassVersionError();
        }
        this.type = type;
    }

    @Override
    public String toString() {
        if (!isReference) {
            String ret = "(" + typeToString(type);
            if (value != null) {
                //ret = ret + ", val=" + value;
            }
            ret = ret + ")";
            return ret;
        } else {
            String ret = "(Ref";
            if (isArrayReference()) {
                ret = "(Array";
            }
            return ret + "=" + refereces + ")";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof GCType)) {
            return false;
        }
        GCType inputType = (GCType) obj;
        if (isReference == inputType.isReference
                && isArray == inputType.isArray && refereces.equals(inputType.refereces)
                && type == inputType.type) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + (this.refereces != null ? this.refereces.hashCode() : 0);
        hash = 83 * hash + this.type;
        hash = 83 * hash + (this.isReference ? 1 : 0);
        hash = 83 * hash + (this.isArray ? 1 : 0);
        return hash;
    }
}
