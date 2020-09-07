package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper;

import java.util.Map;

public interface ImageRecognizeHelper {

    int parseDigit(Map.Entry<String, byte[]> keyboardImage);
}
