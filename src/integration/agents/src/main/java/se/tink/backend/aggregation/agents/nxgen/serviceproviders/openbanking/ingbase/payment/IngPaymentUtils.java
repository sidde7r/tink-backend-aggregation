package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.payment;

import org.assertj.core.util.Strings;

public class IngPaymentUtils {

    public static String modifyMarketCode(String authorizationUrl, String market) {
        if (!Strings.isNullOrEmpty(authorizationUrl)
                && authorizationUrl.length() > 1
                && authorizationUrl.endsWith("XX")) {
            // The redirect we receive from the bank might have generic or wrong market code at
            // the end e.g. https://myaccount.ing.com/payment-initiation/{paymentId}}/XX
            // we need to send French one like this:
            // https://myaccount.ing.com/payment-initiation/{paymentId}}/FR

            return authorizationUrl.substring(0, authorizationUrl.length() - 2) + market;
        }
        return authorizationUrl;
    }
}
