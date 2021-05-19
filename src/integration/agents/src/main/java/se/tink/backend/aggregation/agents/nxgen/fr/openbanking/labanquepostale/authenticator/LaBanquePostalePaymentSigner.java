package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.Map;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;

public class LaBanquePostalePaymentSigner {
    private static final String PSU_AUTHORIZATION_FACTOR_KEY = "psuAuthenticationFactor";
    String paymentAuthorizationUrl;
    String psuAuthenticationFactor;

    public String getPaymentAuthorizationUrlOrThrow() throws PaymentAuthenticationException {
        Optional.ofNullable(paymentAuthorizationUrl)
                .orElseThrow(
                        () ->
                                new PaymentAuthenticationException(
                                        "Payment authentication failed. There is no authorization url!",
                                        new PaymentRejectedException()));
        return paymentAuthorizationUrl;
    }

    public void setPaymentAuthorizationUrl(String paymentAuthorizationUrl) {
        this.paymentAuthorizationUrl = paymentAuthorizationUrl;
    }

    public String getPsuAuthenticationFactor() {
        return psuAuthenticationFactor;
    }

    public void setPsuAuthenticationFactorOrThrow(Map<String, String> callback)
            throws PaymentAuthorizationException {
        // Related to @SupplementaryDataEntity
        if (!callback.containsKey(PSU_AUTHORIZATION_FACTOR_KEY)
                || callback.getOrDefault("error", "").equalsIgnoreCase("authentication_error")) {
            throw new PaymentAuthorizationException(
                    "The Authorization failed on the bank side by the user");
        }
        this.psuAuthenticationFactor = callback.get(PSU_AUTHORIZATION_FACTOR_KEY);
    }
}
