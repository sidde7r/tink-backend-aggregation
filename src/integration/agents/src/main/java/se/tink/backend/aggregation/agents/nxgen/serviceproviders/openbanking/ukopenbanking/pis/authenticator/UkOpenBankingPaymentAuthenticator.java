package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.authenticator;

import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.BANK_SIDE_FAILURE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.NO_BANK_SERVICE;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.VisibleForTesting;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.signableoperation.enums.InternalStatus;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@RequiredArgsConstructor
@Slf4j
public class UkOpenBankingPaymentAuthenticator {

    @VisibleForTesting static final long WAIT_FOR_MINUTES = 9L;

    private final UkOpenBankingPisAuthApiClient apiClient;
    private final OpenIdAuthenticationValidator authenticationValidator;
    private final UkOpenBankingAuthenticationErrorMatcher authenticationErrorMatcher;
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final String callbackUri;
    private final ClientInfo clientInfo;

    public String authenticate(String intentId) throws PaymentAuthorizationException {
        try {
            openThirdPartyApp(intentId);

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

    private void openThirdPartyApp(String intentId) {
        this.supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(getAuthorizationUrl(intentId)));
    }

    private String retrieveAuthCode() throws PaymentAuthorizationException {
        final Map<String, String> callbackData = waitAndGetSupplementalInformation();

        final String authCode = getAuthCodeFromCallbackData(callbackData);

        validateIdToken(callbackData, authCode);

        return authCode;
    }

    private URL getAuthorizationUrl(String intentId) {
        return this.apiClient.buildAuthorizeUrl(
                strongAuthenticationState.getState(), callbackUri, clientInfo, intentId);
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
        final Optional<String> error =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR);

        if (error.isPresent()) {
            final String errorDescription =
                    getCallbackElement(
                                    callbackData, OpenIdConstants.CallbackParams.ERROR_DESCRIPTION)
                            .orElse("");

            handleError(callbackData, error.get(), errorDescription);
        }

        log.info("OpenId callback success.");
    }

    private void handleError(
            Map<String, String> callbackData, String errorType, String errorDescription)
            throws PaymentAuthorizationException {

        if (authenticationErrorMatcher.isKnownOpenIdError(errorType)) {
            log.info(
                    "OpenId {} callback: {}",
                    errorType,
                    SerializationUtils.serializeToString(callbackData));

            final ErrorEntity errorEntity = ErrorEntity.create(errorType, errorDescription);

            if (StringUtils.isBlank(errorDescription)) {
                throw new PaymentAuthorizationException(
                        InternalStatus.PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION);
            } else if (authenticationErrorMatcher.isAuthorizationCancelledByUser(
                    errorDescription)) {
                throw new PaymentAuthorizationCancelledByUserException(
                        errorEntity, InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED);
            } else if (authenticationErrorMatcher.isAuthorizationTimeOut(errorDescription)) {
                throw new PaymentAuthorizationTimeOutException(
                        errorEntity, InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);
            } else if (authenticationErrorMatcher.isAuthorizationFailedByUser(errorDescription)) {
                throw new PaymentAuthorizationFailedByUserException(
                        errorEntity, InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
            } else {
                log.warn(
                        "Unknown error message from bank during payment authorisation: {}",
                        errorDescription);
                throw new PaymentAuthorizationException(
                        InternalStatus.PAYMENT_AUTHORIZATION_UNKNOWN_EXCEPTION);
            }
        } else if (OpenIdConstants.Errors.SERVER_ERROR.equalsIgnoreCase(errorType)) {
            throw BANK_SIDE_FAILURE.exception(errorDescription);
        } else if (OpenIdConstants.Errors.TEMPORARILY_UNAVAILABLE.equalsIgnoreCase(errorType)) {
            throw NO_BANK_SERVICE.exception(errorDescription);
        }
        log.warn("Unknown errorType {} and errorDescription {}", errorType, errorDescription);
        throw new PaymentAuthorizationException(InternalStatus.PAYMENT_AUTHORIZATION_FAILED);
    }

    private String getAuthCodeFromCallbackData(Map<String, String> callbackData) {
        return getCallbackElement(callbackData, OpenIdConstants.CallbackParams.CODE)
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        String.format(
                                                "callbackData did not contain code. CallbackUri: %s, Data received: %s",
                                                callbackUri,
                                                SerializationUtils.serializeToString(
                                                        callbackData))));
    }

    private void validateIdToken(Map<String, String> callbackData, String authCode) {
        final String state =
                getCallbackElement(callbackData, OpenIdConstants.Params.STATE).orElse(null);
        final Optional<String> maybeIdToken =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN);

        maybeIdToken.ifPresent(
                idToken -> authenticationValidator.validateIdToken(idToken, authCode, state));
    }

    private static TransferExecutionException createFailedTransferException() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(
                        "Payment failed - authorisation of payment failed, needs further investigation.")
                .setEndUserMessage("Authorisation of payment failed.")
                .setInternalStatus(InternalStatus.PAYMENT_AUTHORIZATION_FAILED.toString())
                .build();
    }

    private static Optional<String> getCallbackElement(
            Map<String, String> callbackData, String key) {
        return Optional.ofNullable(callbackData.get(key)).filter(StringUtils::isNotBlank);
    }
}
