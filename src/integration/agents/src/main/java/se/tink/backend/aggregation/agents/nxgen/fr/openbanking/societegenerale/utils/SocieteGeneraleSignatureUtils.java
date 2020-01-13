package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.SignatureValues;

public class SocieteGeneraleSignatureUtils {

    public static String getAlgorithm() {
        return String.format("%s=\"%s\"", SignatureValues.ALGORITHM, SignatureValues.RSA_SHA256);
    }
}
