package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.REDIRECT_URI;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.WIZARD_SESSION_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.Urls.FTS_WIDGET_CDN;
import static se.tink.libraries.payment.enums.PaymentStatus.SIGNED;

import com.github.rholder.retry.RetryException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.payment.InsufficientFundsException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationFailedByUserException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.LastError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.PaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPayment;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.FinTechSystemsSession;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

@RequiredArgsConstructor
@Slf4j
public class FinTechSystemsPaymentExecutor implements PaymentExecutor {
    private final FinTecSystemsApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final String redirectUrl;
    private final FinTecSystemsStorage storage;
    private final PersistentLogin persistentAgent;
    SessionStatusRetryer sessionStatusRetryer = new SessionStatusRetryer();

    private static final long WAIT_FOR_MINUTES = 3L;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        validateThatCredentialDidNotSucceedWithPaymentAlready();

        FinTechSystemsPaymentResponse finTechSystemsPaymentResponse =
                apiClient.createPayment(paymentRequest);
        URL wizardUrl = buildWizardUrl(finTechSystemsPaymentResponse.getWizardSessionKey());
        handleRedirect(wizardUrl);
        return finTechSystemsPaymentResponse.toTinkPaymentResponse(paymentRequest.getPayment());
    }

    private void validateThatCredentialDidNotSucceedWithPaymentAlready() {
        if (storage.retrieveTransactionId().isPresent()) {
            throw new IllegalStateException(
                    "There was a successful payment on this credential already! FTS agent does not support multiple payments on the same credential due to account-check part.");
        }
    }

    private void handleRedirect(URL wizardUrl) {
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(wizardUrl));

        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    private URL buildWizardUrl(String wizardSessionKey) {
        return new URL(FTS_WIDGET_CDN)
                .queryParam(WIZARD_SESSION_KEY, wizardSessionKey)
                .queryParam(REDIRECT_URI, buildCallBackUrl());
    }

    private String buildCallBackUrl() {
        return redirectUrl + "?state=" + strongAuthenticationState.getState();
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        FinTechSystemsSession sessionsResponse =
                pollSessionUntilSessionIsFinished(paymentMultiStepRequest);

        if (sessionsResponse.getLastError().isEmpty()) {
            return handleSuccessfulPayment(paymentMultiStepRequest);
        } else {
            throw getPaymentExceptionFromFtsErrorMessage(sessionsResponse.getLastError());
        }
    }

    private PaymentMultiStepResponse handleSuccessfulPayment(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment tinkPayment = paymentMultiStepRequest.getPayment();

        FinTechSystemsPayment ftsPaymentResponse =
                apiClient.fetchPaymentStatus(paymentMultiStepRequest);

        switch (PaymentStatus.fromString(ftsPaymentResponse.getPaymentStatus())) {
            case NONE:
            case RECEIVED:
                tinkPayment.setStatus(SIGNED);
                break;
            default:
                log.error(
                        "Unknow status received from Fintech Systems={}",
                        ftsPaymentResponse.getPaymentStatus());
                throw new PaymentCancelledException();
        }

        storage.storeTransactionId(ftsPaymentResponse.getTransaction());

        // The line below is supposed to make sure that persistent storage gets saved.
        // Currently, we think that operation command for payment does not include, in any way,
        // making sure that persistent storage gets saved.
        // To make the FTS payment + pseudo acc check work, we need to make sure persistent storage
        // is saved.
        persistentAgent.persistLoginSession();

        fillTinkPaymentWithDebtorData(tinkPayment, ftsPaymentResponse);

        return new PaymentMultiStepResponse(tinkPayment, AuthenticationStepConstants.STEP_FINALIZE);
    }

    private Payment fillTinkPaymentWithDebtorData(
            Payment tinkPayment, FinTechSystemsPayment ftsPaymentResponse) {
        tinkPayment.setDebtor(new Debtor(new IbanIdentifier(ftsPaymentResponse.getSenderIban())));
        return tinkPayment;
    }

    private PaymentException getPaymentExceptionFromFtsErrorMessage(String lastError) {
        log.info("FTS lastError:{}", lastError);
        switch (LastError.fromString(lastError)) {
            case CLIENT_ABORTED:
                return new PaymentAuthorizationCancelledByUserException(
                        ErrorEntity.create(lastError, lastError),
                        InternalStatus.PAYMENT_AUTHORIZATION_CANCELLED);
            case TOKEN_MISMATCH:
            case LOGIN_FAILED:
            case LOGIN_NEXT_FAILED:
                return new PaymentAuthenticationException(InternalStatus.USER_UNAUTHORIZED);

            case CONSENT_INVALID:
            case MAX_LOGIN_TRIES:
            case MAX_TAN_TRIES:
            case WRONG_TAN:
                return new PaymentAuthorizationFailedByUserException(
                        ErrorEntity.create(lastError, lastError),
                        InternalStatus.PAYMENT_AUTHORIZATION_FAILED);

            case INIT_FAILED:
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

    private FinTechSystemsSession pollSessionUntilSessionIsFinished(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        FinTechSystemsSession sessionsResponse;
        try {
            sessionsResponse =
                    sessionStatusRetryer.callUntilSessionStatusIsNotFinished(
                            () ->
                                    apiClient.fetchSessionStatus(
                                            paymentMultiStepRequest.getPayment().getUniqueId()));
        } catch (ExecutionException | RetryException e) {
            log.error("Unable to get Session Status from FTS", e);
            sessionsResponse =
                    apiClient.fetchSessionStatus(
                            paymentMultiStepRequest.getPayment().getUniqueId());
            if (sessionsResponse != null) {
                throw getPaymentExceptionFromFtsErrorMessage(sessionsResponse.getLastError());
            } else {
                throw new PaymentException(e.getMessage(), e.getCause());
            }
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
}
