package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@Slf4j
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
            callback.forEach((k, v) -> log.info(k + " : " + v));
            throw new PaymentAuthorizationException(
                    "The Authorization failed during SCA",
                    InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
        }
        this.psuAuthenticationFactor = callback.get(PSU_AUTHORIZATION_FACTOR_KEY);
    }
}
