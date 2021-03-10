package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.authenticator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParams;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.QueryParamsValues;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankConstants.Urls;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@RequiredArgsConstructor
@Slf4j
public class DemobankPaymentAuthenticator {

    private static final long WAIT_FOR_MINUTES = 9L;

    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String callbackUri;

    public String authenticate(String authorizeUrl) throws PaymentAuthorizationException {
        try {
            openThirdPartyApp(authorizeUrl);

            return retrieveAuthCode();
        } catch (AuthenticationException | AuthorizationException e) {
            if (e.getError() instanceof ThirdPartyAppError
                    && ThirdPartyAppError.TIMED_OUT.equals(e.getError())) {
                throw new PaymentAuthorizationTimeOutException(
                        InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);
            }

            throw createFailedTransferException();
        }
    }

    private void openThirdPartyApp(String authorizeUrl) {
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(buildAuthorizeUrl(authorizeUrl)));
    }

    private URL buildAuthorizeUrl(String authorizeUrl) {
        return new URL(Urls.BASE_URL + authorizeUrl)
                .queryParam(QueryParams.STATE, strongAuthenticationState.getState())
                .queryParam(QueryParams.REDIRECT_URI, callbackUri);
    }

    private String retrieveAuthCode() throws PaymentAuthorizationException {
        final Map<String, String> callbackData = waitAndGetSupplementalInformation();

        return getAuthCodeFromCallbackData(callbackData);
    }

    private Map<String, String> waitAndGetSupplementalInformation()
            throws PaymentAuthorizationException {
        final Map<String, String> callbackData =
                this.supplementalInformationHelper
                        .waitForSupplementalInformation(
                                this.strongAuthenticationState.getSupplementalKey(),
                                WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        checkCallbackDataForErrors(callbackData);

        return callbackData;
    }

    private void checkCallbackDataForErrors(Map<String, String> callbackData)
            throws PaymentAuthorizationException {
        final Optional<String> error = getCallbackElement(callbackData, "error");

        if (error.isPresent()) {
            final String errorDescription =
                    getCallbackElement(callbackData, "error_description").orElse("");

            handleError(callbackData, error.get(), errorDescription);
        }

        log.info("Callback success.");
    }

    private void handleError(
            Map<String, String> callbackData, String errorType, String errorDescription)
            throws PaymentAuthorizationException {

        log.info(
                "Error {} during callback: {}",
                errorType,
                SerializationUtils.serializeToString(callbackData));

        if (StringUtils.isBlank(errorType)) {
            throw new PaymentAuthorizationException(
                    InternalStatus.PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION);
        } else if (errorType.equalsIgnoreCase("access_denied")) {
            final ErrorEntity errorEntity = ErrorEntity.create(errorType, errorDescription);

            throw new PaymentAuthorizationCancelledByUserException(
                    errorEntity, InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED);
        } else {
            log.warn(
                    "Unknown error: {} with message: {} from bank during payment authorisation",
                    errorType,
                    errorDescription);
            throw new PaymentAuthorizationException(
                    InternalStatus.PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION);
        }
    }

    private String getAuthCodeFromCallbackData(Map<String, String> callbackData) {
        return getCallbackElement(callbackData, QueryParamsValues.RESPONSE_TYPE)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "callbackData did not contain code. CallbackUri: %s, Data received: %s",
                                                callbackUri,
                                                SerializationUtils.serializeToString(
                                                        callbackData))));
    }

    private static Optional<String> getCallbackElement(
            Map<String, String> callbackData, String key) {
        return Optional.ofNullable(callbackData.get(key)).filter(StringUtils::isNotBlank);
    }

    private static TransferExecutionException createFailedTransferException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Payment failed - authorisation of payment failed, needs further investigation.")
                .setEndUserMessage("Authorisation of payment failed.")
                .setInternalStatus(InternalStatus.PAYMENT_AUTHORIZATION_FAILED.toString())
                .build();
    }
}
