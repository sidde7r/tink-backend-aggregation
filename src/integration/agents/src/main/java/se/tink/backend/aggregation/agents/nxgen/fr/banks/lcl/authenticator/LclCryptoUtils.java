package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator;

public class LclCryptoUtils {

    public static String computeXorPin(String pin, int key) {
        StringBuilder sb = new StringBuilder();

        for (char digit : pin.toCharArray()) {
            int xorResult = (int) digit ^ key;
            sb.append((char) xorResult);
        }

        return sb.toString();
    }
}
