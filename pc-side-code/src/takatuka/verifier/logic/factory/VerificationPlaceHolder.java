package takatuka.verifier.logic.factory;

/**
 * <p>Title: </p>
 * <p>Description:
 * Based on the placeholder design pattern.
 * </p>
 * @author Faisal Aslam
 * @version 1.0
 */
public class VerificationPlaceHolder {
    private VerificationFrameFactory factory = VerificationFrameFactory.getInstanceOf();
    private static final VerificationPlaceHolder placeholder = new
            VerificationPlaceHolder();

    private VerificationPlaceHolder() {
        super();
    }

    public static VerificationPlaceHolder getInstanceOf() {
        return placeholder;
    }

    public void setFactory(VerificationFrameFactory factory) {
        this.factory = factory;
    }

    public VerificationFrameFactory getFactory() {
        return factory;
    }

}
