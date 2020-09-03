package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper;

import java.util.Map;

public class FakeImageRecognizerHelper implements ImageRecognizeHelper {

    @Override
    public int parseDigit(Map.Entry<String, byte[]> keyboardImage) {
        return 7;
    }
}
