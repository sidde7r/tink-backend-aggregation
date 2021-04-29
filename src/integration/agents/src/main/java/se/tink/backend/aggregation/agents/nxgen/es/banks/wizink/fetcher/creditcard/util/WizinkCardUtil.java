package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.creditcard.util;

import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkDecoder;
import se.tink.backend.aggregation.utils.CreditCardMasker;

public class WizinkCardUtil {

    public static String getMaskedCardNumber(String encodedCardNumber, String xTokenUser) {
        String decodedCardNumber = WizinkDecoder.decodeNumber(encodedCardNumber, xTokenUser);
        return maskCardNumberIfNecessary(decodedCardNumber);
    }

    private static String maskCardNumberIfNecessary(String decodedCardNumber) {
        if (Character.isDigit(decodedCardNumber.charAt(0))) {
            return CreditCardMasker.maskCardNumber(decodedCardNumber);
        }
        return decodedCardNumber;
    }
}
