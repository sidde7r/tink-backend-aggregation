package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;

public class TransactionsScaVirtualKeyboardOcr extends NumbersVirtualKeyboardOcr {

    public TransactionsScaVirtualKeyboardOcr(BufferedImage virtualKeyboardImage) {
        super(
                VirtualKeyboardImageParameters.createTransactionsSCAConfiguration(),
                Lists.newArrayList(
                        new SubImageNumberCheckSumCorrelation("2574875706-150", 0),
                        new SubImageNumberCheckSumCorrelation("1898855350-150", 1),
                        new SubImageNumberCheckSumCorrelation("703743688-150", 2),
                        new SubImageNumberCheckSumCorrelation("1444231452-150", 3),
                        new SubImageNumberCheckSumCorrelation("823842625-150", 4),
                        new SubImageNumberCheckSumCorrelation("3201879118-150", 5),
                        new SubImageNumberCheckSumCorrelation("3427852132-150", 6),
                        new SubImageNumberCheckSumCorrelation("3386420344-150", 7),
                        new SubImageNumberCheckSumCorrelation("3996298118-150", 8),
                        new SubImageNumberCheckSumCorrelation("1240257521-150", 9)),
                virtualKeyboardImage);
    }
}
