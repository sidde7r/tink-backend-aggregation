package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;

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

    public String getPsuAuthenticationFactor() throws PaymentRejectedException {
        // Related to @SupplementaryDataEntity
        if (psuAuthenticationFactor == null) {
            throw new PaymentRejectedException();
        }
        return psuAuthenticationFactor;
    }

    public void setPsuAuthenticationFactor(Map<String, String> callback) {
        String psuAuthFactorKey = callback.get(PSU_AUTHORIZATION_FACTOR_KEY);
        log.info(
                "[PAYMENT SIGNER] Callback size {}. Is containing {}: {}",
                callback.size(),
                PSU_AUTHORIZATION_FACTOR_KEY,
                callback.containsKey(PSU_AUTHORIZATION_FACTOR_KEY));
        this.psuAuthenticationFactor = psuAuthFactorKey;
    }
}
