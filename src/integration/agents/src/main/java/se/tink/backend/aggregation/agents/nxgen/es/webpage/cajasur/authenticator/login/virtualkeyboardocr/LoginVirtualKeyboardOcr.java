package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur.authenticator.login.virtualkeyboardocr;

import com.google.common.collect.Lists;
import java.awt.image.BufferedImage;

public class LoginVirtualKeyboardOcr extends NumbersVirtualKeyboardOcr {

    public LoginVirtualKeyboardOcr(BufferedImage virtualKeyboardImage) {
        super(
                VirtualKeyboardImageParameters.createEnterpriseConfiguration(),
                Lists.newArrayList(
                        new SubImageNumberCheckSumCorrelation("1627713458-96", 0),
                        new SubImageNumberCheckSumCorrelation("1752858395-96", 1),
                        new SubImageNumberCheckSumCorrelation("3522085418-96", 2),
                        new SubImageNumberCheckSumCorrelation("4049401402-96", 3),
                        new SubImageNumberCheckSumCorrelation("1405771207-96", 4),
                        new SubImageNumberCheckSumCorrelation("2140092309-96", 5),
                        new SubImageNumberCheckSumCorrelation("1661481153-96", 6),
                        new SubImageNumberCheckSumCorrelation("1428844876-96", 7),
                        new SubImageNumberCheckSumCorrelation("3545991190-96", 8),
                        new SubImageNumberCheckSumCorrelation("2516460739-96", 9)),
                virtualKeyboardImage);
    }
}
