package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.REDIRECT_URI;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.WIZARD_SESSION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Urls.FTS_WIDGET_CDN;
import static se.tink.libraries.payment.enums.PaymentStatus.SIGNED;

import com.github.rholder.retry.RetryException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.LastError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.PaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetSessionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@RequiredArgsConstructor
@Slf4j
public class FinTechSystemsPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final FinTecSystemsApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final FinTecSystemsConfiguration providerConfiguration;
    private final String redirectUrl;
    SessionStatusRetryer sessionStatusRetryer = new SessionStatusRetryer();

    private static final long WAIT_FOR_MINUTES = 9L;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        CreatePaymentResponse createPaymentResponse = apiClient.createPayment(paymentRequest);
        URL wizardUrl = getWizardUrl(createPaymentResponse.getWizardSessionKey());
        handleRedirect(wizardUrl);
        return createPaymentResponse.toTinkPayment(createPaymentResponse);
    }

    private void handleRedirect(URL wizardUrl) {
        log.info("callBackURL: " + getCallBackUrl());
        openThirdPartyApp(wizardUrl);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {

        GetSessionsResponse sessionsResponse =
                pollSessionUntilSessionIsFinished(paymentMultiStepRequest);

        if (sessionsResponse.getLastError().isEmpty()) {
            return handleSuccessfulPayment(paymentMultiStepRequest);
        } else {
            throw getPaymentExceptionFromFtsErrorMessage(sessionsResponse.getLastError());
        }
    }

    private PaymentMultiStepResponse handleSuccessfulPayment(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentCancelledException {
        GetPaymentResponse paymentResponse = apiClient.fetchPaymentStatus(paymentMultiStepRequest);

        switch (PaymentStatus.fromString(paymentResponse.getPaymentStatus())) {
            case NONE:
            case RECEIVED:
                return updatePaymentStatus(paymentMultiStepRequest, SIGNED);
            default:
                log.error(
                        "Unknow status received from Fintech Systems={}",
                        paymentResponse.getPaymentStatus());
                throw new PaymentCancelledException();
        }
    }

    private PaymentException getPaymentExceptionFromFtsErrorMessage(String lastError) {
        log.info("FTS lastError:{}", lastError);
        switch (LastError.fromString(lastError)) {
            case CLIENT_ABORTED:
                return new PaymentAuthorizationTimeOutException(
                        lastError, InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);
            case TOKEN_MISMATCH:
            case LOGIN_FAILED:
            case LOGIN_NEXT_FAILED:
                return new PaymentAuthenticationException(InternalStatus.USER_UNAUTHORIZED);

            case CONSENT_INVALID:
            case MAX_LOGIN_TRIES:
            case MAX_TAN_TRIES:
                return new PaymentAuthorizationFailedByUserException(
                        ErrorEntity.create(lastError, lastError),
                        InternalStatus.PAYMENT_AUTHORIZATION_FAILED);

            case WRONG_TAN:
                return new PaymentAuthorizationFailedByUserException(
                        ErrorEntity.create(lastError, lastError),
                        InternalStatus.PAYMENT_AUTHORIZATION_FAILED);

            case SESSION_EXPIRED:
                return new PaymentAuthorizationTimeOutException(
                        lastError, InternalStatus.PAYMENT_AUTHORIZATION_TIMEOUT);

            case NO_COMPATIBLE_ACCOUNTS:
            case PINNED_IBAN_NOT_FOUND:
                return new PaymentValidationException(
                        lastError, InternalStatus.INVALID_SOURCE_ACCOUNT);

            case PINNED_HOLDER_NOT_FOUND:
                return new PaymentValidationException(lastError);

            case SECURITY_BALANCE_FAILED:
            case SECURITY_MAX_AMOUNT_EXCEEDED:
                return new InsufficientFundsException(lastError);

            case TECH_ERROR:
            case TRANSACTION_FAILED:
                return new PaymentException(lastError);

            case VALIDATION_FAILED:
                return new PaymentValidationException(
                        InternalStatus.PAYMENT_VALIDATION_FAILED_NO_DESCRIPTION);
            default:
                log.error("Unexpected error, FTS error code:{}", lastError);
                return new PaymentException(lastError);
        }
    }

    private PaymentMultiStepResponse updatePaymentStatus(
            PaymentMultiStepRequest paymentMultiStepRequest,
            se.tink.libraries.payment.enums.PaymentStatus signed) {
        // If no error update payment status as signed
        paymentMultiStepRequest.getPayment().setStatus(signed);
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(), AuthenticationStepConstants.STEP_FINALIZE);
    }

    private GetSessionsResponse pollSessionUntilSessionIsFinished(
            PaymentMultiStepRequest paymentMultiStepRequest) throws PaymentException {
        GetSessionsResponse sessionsResponse;
        try {
            sessionsResponse =
                    sessionStatusRetryer.callUntilSessionStatusIsNotFinished(
                            () ->
                                    apiClient.getSessionStatus(
                                            paymentMultiStepRequest.getPayment().getUniqueId()));
        } catch (ExecutionException | RetryException e) {
            log.error("Unable to get Session Status from FTS", e);
            throw new PaymentException(e.getMessage(), e.getCause());
        }
        return sessionsResponse;
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "fetchMultiple not implemented for " + this.getClass().getName());
    }

    private void openThirdPartyApp(URL authorizeUrl) {
        ThirdPartyAppAuthenticationPayload payload =
                ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
        this.supplementalInformationHelper.openThirdPartyApp(payload);
    }

    private URL getWizardUrl(String wizardSessionKey) {
        return new URL(FTS_WIDGET_CDN)
                .queryParam(WIZARD_SESSION_KEY, wizardSessionKey)
                .queryParam(REDIRECT_URI, getCallBackUrl());
    }

    private String getCallBackUrl() {
        return redirectUrl + "?state=" + strongAuthenticationState.getState();
    }
}
