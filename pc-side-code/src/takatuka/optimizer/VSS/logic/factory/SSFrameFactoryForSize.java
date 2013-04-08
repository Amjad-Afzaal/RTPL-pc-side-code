package takatuka.optimizer.VSS.logic.factory;

import takatuka.optimizer.VSS.dataObjs.*;
import takatuka.verifier.dataObjs.*;
import takatuka.verifier.logic.factory.VerificationFrameFactory;

/**
 * <p>Title: </p>
 * <p>Description:
 *
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class SSFrameFactoryForSize extends VerificationFrameFactory {

    private static final SSFrameFactoryForSize frameFactory = new SSFrameFactoryForSize();

    protected SSFrameFactoryForSize() {
        super();
    }

    public static VerificationFrameFactory getInstanceOf() {
        return frameFactory;
    }

    @Override
    public OperandStack createOperandStack(int maxStack) {
        return new SSOperandStack(maxStack);
    }

    @Override
    public LocalVariables createLocalVariables(int maxStack) {
        return new SSLocalVariables(maxStack);
    }
}
