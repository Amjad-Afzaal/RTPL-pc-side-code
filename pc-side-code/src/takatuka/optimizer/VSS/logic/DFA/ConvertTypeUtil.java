package takatuka.optimizer.VSS.logic.DFA;

import takatuka.optimizer.VSS.logic.preCodeTravers.CastInstructionController;
import takatuka.optimizer.VSS.logic.preCodeTravers.ChangeCodeForReducedSizedLV;
import java.util.*;
import takatuka.classreader.dataObjs.*;
import takatuka.classreader.logic.util.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.dataObjs.attribute.*;
import takatuka.verifier.logic.*;
import takatuka.verifier.logic.exception.*;
import takatuka.verifier.logic.factory.*;
import takatuka.verifier.logic.DFA.*;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class ConvertTypeUtil {

    private static final CastInstructionController castInstrContr = CastInstructionController.getInstanceOf();
    private final static ConvertTypeUtil myObj = new ConvertTypeUtil();
    private static MethodInfo currentMethod = null;

    private ConvertTypeUtil() {
    }

    public static ConvertTypeUtil getInstanceOf(MethodInfo currentMethod_) {
        currentMethod = currentMethod_;
        return myObj;
    }

    public static boolean passIntegerTest(Type type) {
        return (type.getBlocks() == Type.getBlocks(Type.INTEGER, false)
                && type.isIntOrShortOrByteOrBooleanOrCharType());
    }

    public final void convertTypes(Type toBeChanged,
            int indexInStack, VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        DataFlowAnalyzer.Debug_print("------------ " , inst.getMnemonic());
        convertTypes(toBeChanged, frameFactory.createType(Type.INTEGER), indexInStack, inst);
    }

    public final void convertTypes(Type toBeChanged, int toChangeTo,
            int indexInStack, VerificationInstruction inst) {
        VerificationFrameFactory frameFactory = VerificationPlaceHolder.getInstanceOf().getFactory();
        DataFlowAnalyzer.Debug_print("------------ " , inst.getMnemonic());
        convertTypes(toBeChanged, frameFactory.createType(toChangeTo), indexInStack, inst);
    }

    /**
     * Called only from DUP instructions.
     *
     * @param toBeChanged
     * @param indexInStack
     * @param inst
     */
    public final void convertTypesSpecial(int toBeChanged,
            int indexInStack, VerificationInstruction inst) {

        castInstrContr.add(currentMethod, inst,
                toBeChanged, Type.INTEGER,
                indexInStack, false);
       // toBeChanged.setType(Type.INTEGER, false);
    }

    public final void convertTypes(Type toBeChanged, Type toChangeTo,
            int indexInStack, VerificationInstruction inst, boolean isForMethodDesc) {

        if (!toChangeTo.isIntOrShortOrByteOrBooleanOrCharType()
                || !toBeChanged.isIntOrShortOrByteOrBooleanOrCharType()) {
            throw new VerifyErrorExt(Messages.STACK_INVALID
                    + ", " + toBeChanged + ", " + toChangeTo);
        } else if (toBeChanged.getBlocks() == toChangeTo.getBlocks()) {
            //ignore. Do not change.
            return;
        }
        castInstrContr.add(currentMethod, inst,
                toBeChanged.getType(), toChangeTo.getType(),
                indexInStack, isForMethodDesc);
        toBeChanged.setType(toChangeTo.getType(), false);
    }

    /**
     *
     * @param toBeChanged
     * @param toChangeTo
     * @param indexInStack
     * @param inst
     */
    public final void convertTypes(Type toBeChanged, Type toChangeTo,
            int indexInStack, VerificationInstruction inst) {
        convertTypes(toBeChanged, toChangeTo, indexInStack, inst, false);
    }

    /**
     * this is used by the pop only. It make the stack size equal to size of integer.
     *
     * @param currentType
     * @param indexInStack
     * @param instr
     */
    public final void convertTypesToIntegerSize(Type currentType, int indexInStack, VerificationInstruction instr) {
        if (!instr.getMnemonic().contains("POP") || currentType.getBlocks() > Type.getBlocks(Type.INTEGER, false)) {
            Miscellaneous.printlnErr("Error # 9723");
            Miscellaneous.exit();
        }
        if (currentType.getBlocks() < Type.getBlocks(Type.INTEGER, false)) {
            int type = Integer.MIN_VALUE;
            if (!currentType.isReference()) {
                type = currentType.getType();
            }
            castInstrContr.add(currentMethod, instr, type,
                    Type.INTEGER, indexInStack, false);
        }
    }

    public final void convertTypeBasedOnLVIndex(Type fromStack, int localVariableOffSet, VerificationInstruction instr) {
        if (!fromStack.isIntOrShortOrByteOrBooleanOrCharType()) {
            Miscellaneous.printlnErr("invalid function call");
            Miscellaneous.exit();
        }
        TreeMap<Integer, Type> map = ChangeCodeForReducedSizedLV.getTypeMapOfMethod(currentMethod);
        Type basedOnMethodDesc = map.get(localVariableOffSet);
        convertTypes(fromStack, basedOnMethodDesc, 0, instr);
    }
}
