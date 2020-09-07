package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper;

import java.awt.Color;
import java.util.Map;
import se.tink.backend.aggregation.utils.ImageRecognizer;

public class ImageRecognizerHelperImpl implements ImageRecognizeHelper {

    public int parseDigit(Map.Entry<String, byte[]> keyboardImage) {
        return Integer.parseInt(
                ImageRecognizer.ocr(keyboardImage.getValue(), Color.WHITE).replaceAll("\\s", ""));
    }
}
