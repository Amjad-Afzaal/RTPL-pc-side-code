package takatuka.optimizer.VSS.logic.preCodeTravers;

import takatuka.classreader.dataObjs.*;
import takatuka.optimizer.bytecode.branchSetter.dataObjs.attributes.BHInstruction;
import takatuka.verifier.dataObjs.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * It is used to store casting information. The casting is required when the size
 * of local variables and operand stack is reduced.
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class CastInfo implements Comparable<CastInfo> {

    private long instrId = -1;
    private int stackCurrentType = -1;
    private int toConvertIntoType = -1;
    private int stackLocation = -1;
    private MethodInfo method = null;
    private boolean isForMethodDesc = false;
    private BHInstruction instr = null;


    /**
     * 
     * @param instrId
     * @param stackCurrentType
     * @param toConvertIntoType
     * @param stackLocation
     * @param methodInfo
     * @param forMethodDesc
     */
    public CastInfo(BHInstruction instr, int stackCurrentType,
            int toConvertIntoType, int stackLocation,
            MethodInfo methodInfo, boolean forMethodDesc) {
        this.instrId = instr.getInstructionId();
        this.instr = instr;
        this.stackCurrentType = stackCurrentType;
        this.stackLocation = stackLocation;
        this.method = methodInfo;
        this.isForMethodDesc = forMethodDesc;
        this.toConvertIntoType = toConvertIntoType;
    }

    public BHInstruction getInstr() {
        return instr;
    }
    /**
     *
     * @return
     */
    public boolean isForMethodDescription() {
        return isForMethodDesc;
    }

    /**
     * 
     * @return
     */
    public MethodInfo getMethod() {
        return method;
    }

    /**
     * 
     * @return
     */
    public long getInstrId() {
        return instrId;
    }

    /**
     * 
     * @return
     */
    public int getStackCurrentType() {
        return stackCurrentType;
    }

    /**
     * 
     * @return
     */
    public int getTypeToConvertInto() {
        return toConvertIntoType;
    }

    /**
     * 
     * @return
     */
    public int getStackLocation() {
        return stackLocation;
    }

    @Override
    public int compareTo(CastInfo arg0) {
        int comp = new Long(instrId).compareTo(arg0.instrId);
        if (comp != 0) {
            return comp;
        } else {
            comp = new Integer(stackLocation).compareTo(arg0.stackLocation);
            if (comp == 0) {
                //return new Integer(arg0.stackCurrentType).compareTo(stackCurrentType);
            }
        }
        return comp;

    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof CastInfo)) {
            return false;
        }
        CastInfo input = (CastInfo) obj;
        if (input.instrId == instrId && input.stackLocation == stackLocation) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.instrId ^ (this.instrId >>> 32));
        hash = 97 * hash + this.stackLocation;
        return hash;
    }

    @Override
    public String toString() {
        return "instrId=" + instrId + ", stackCurrentType=" + Type.typeToString(stackCurrentType)
                +", toConvertIntoType="+Type.typeToString(toConvertIntoType)
                + ", stack-Location=" + stackLocation
                + ", isForMethodDesc=" + isForMethodDesc;
    }
}
