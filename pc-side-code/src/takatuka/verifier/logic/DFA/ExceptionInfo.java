package takatuka.verifier.logic.DFA;

import takatuka.verifier.dataObjs.attribute.*;
import java.util.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ExceptionInfo {

    private VerificationInstruction instr = null;
    private Vector<Integer> exceptionClassesThisPointers = new Vector();
    private Vector<Long> targetCatchInstrIDs = new Vector();
    private Vector<Integer> exceptTableIndexesUsed = new Vector();

    public ExceptionInfo(VerificationInstruction instr) {
        this.instr = instr;
    }

    /**
     *
     * @return
     */
    public VerificationInstruction getInstruction() {
        return instr;
    }

    /**
     *
     * @return
     */
    public Vector<Integer> getExceptionClassesThisPointers() {
        return exceptionClassesThisPointers;
    }

    public boolean isEmpty() {
        return getTargetCatchInstrIDs().size() == 0;
    }
    /**
     *
     * @return
     */
    public Vector<Long> getTargetCatchInstrIDs() {
        return targetCatchInstrIDs;
    }

    /**
     * 
     * @return
     */
    public Vector<Integer> getExceptionTableEntriedUsed() {
        return exceptTableIndexesUsed;
    }
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 47 * hash + (this.instr != null ? this.instr.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ExceptionInfo)) {
            return false;
        }
        ExceptionInfo input = (ExceptionInfo) obj;
        if (input.instr.equals(instr)) {
            return true;
        }
        return false;
    }
}
