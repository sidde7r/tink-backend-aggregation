package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment;

import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.NO_SCA_METHOD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.errors.SparkassenKnownErrors.PsuErrorMessages.SYSTEM_ERROR_CONTACT_ADVISOR;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.BasePaymentExecutor;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Payment;

@Slf4j
public class SparkassenPaymentExecutor extends BasePaymentExecutor {

    public SparkassenPaymentExecutor(
            PaymentApiClient apiClient,
            PaymentAuthenticator authenticator,
            SessionStorage sessionStorage) {
        super(apiClient, authenticator, sessionStorage);
    }

    @Override
    // NZG-1028
    // This method logs some additional information about payment that fail weirdly.
    // This happens in SCA method selection step, but the message tells nothing and is not related
    // to missing sca methods.
    protected void debugLoggingForUnexpectedFailures(
            AgentException agentException, Payment payment) {
        if (agentException instanceof LoginException) {
            LoginException loginException = (LoginException) agentException;
            if (LoginError.NO_AVAILABLE_SCA_METHODS == loginException.getError()) {
                Throwable cause = loginException.getCause();
                if (cause instanceof HttpResponseException) {
                    HttpResponseException httpResponseException = (HttpResponseException) cause;
                    Optional<ErrorResponse> maybeRelevantErrorResponse =
                            ErrorResponse.fromHttpException(httpResponseException)
                                    .filter(
                                            ErrorResponse.anyTppMessageMatchesPredicate(
                                                    NO_SCA_METHOD))
                                    .filter(
                                            ErrorResponse.psuMessageContainsPredicate(
                                                    SYSTEM_ERROR_CONTACT_ADVISOR));
                    if (maybeRelevantErrorResponse.isPresent()) {
                        log.info(
                                "[Sparkasse] No-sca-methods error during payment authorization! Payment details - amount is "
                                        + getAmountBucketInString(payment.getExactCurrencyAmount())
                                        + " - creditor iban country: "
                                        + payment.getCreditor()
                                                .getAccountIdentifier(IbanIdentifier.class)
                                                .getIban()
                                                .substring(0, 2));
                    }
                }
            }
        }
    }

    private String getAmountBucketInString(ExactCurrencyAmount amount) {
        int[] bucketLimits = {0, 10, 20, 50, 100, 500, 1000};
        for (int i = 1; i < bucketLimits.length; i++) {
            if (bucketLimits[i] > amount.getDoubleValue()) {
                return "between " + bucketLimits[i - 1] + " and " + bucketLimits[i];
            }
        }
        return "over " + bucketLimits[bucketLimits.length - 1];
    }
}
