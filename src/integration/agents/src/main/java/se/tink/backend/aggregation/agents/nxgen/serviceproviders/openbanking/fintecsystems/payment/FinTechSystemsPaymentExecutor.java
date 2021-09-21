package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.REDIRECT_URI;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConstants.PathVariables.WIZARD_SESSION_KEY;
import static se.tink.libraries.payment.enums.PaymentStatus.PAID;
import static se.tink.libraries.payment.enums.PaymentStatus.SIGNED;

import com.github.rholder.retry.RetryException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationTimeOutException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.FinTecSystemsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.enums.PaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc.GetPaymentResponse;
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

@RequiredArgsConstructor
@Slf4j
public class FinTechSystemsPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final FinTecSystemsApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;
    private final FinTecSystemsConfiguration providerConfiguration;
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
        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(), WAIT_FOR_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {

        boolean isSessionFinished = false;
        try {
            isSessionFinished =
                    sessionStatusRetryer.callUntilSessionStatusIsNotFinished(
                            () ->
                                    apiClient.getSessionStatus(
                                            paymentMultiStepRequest.getPayment().getUniqueId()));
        } catch (ExecutionException | RetryException e) {
            log.error("Unable to get Session Status from FTS", e);
            throw new PaymentException(e.getMessage(), e.getCause());
        }

        if (isSessionFinished) {
            GetPaymentResponse paymentResponse =
                    apiClient.fetchPaymentStatus(paymentMultiStepRequest);
            switch (PaymentStatus.fromString(paymentResponse.getPaymentStatus())) {
                case NONE:
                    // If no error update payment status as signed
                    paymentMultiStepRequest.getPayment().setStatus(SIGNED);
                    return new PaymentMultiStepResponse(
                            paymentMultiStepRequest.getPayment(),
                            AuthenticationStepConstants.STEP_FINALIZE);
                case RECEIVED:
                    // If no error and RECEIVED then update payment status as signed
                    paymentMultiStepRequest.getPayment().setStatus(PAID);
                    return new PaymentMultiStepResponse(
                            paymentMultiStepRequest.getPayment(),
                            AuthenticationStepConstants.STEP_FINALIZE);
                default:
                    log.error(
                            "Unknow status received from Fintech Systems={}",
                            paymentResponse.getPaymentStatus());
                    throw new PaymentCancelledException();
            }
        } else {
            throw new PaymentAuthorizationTimeOutException();
        }
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
        return new URL(providerConfiguration.getWizardUrl())
                .queryParam(WIZARD_SESSION_KEY, wizardSessionKey)
                .queryParam(REDIRECT_URI, getCallBackUrl());
    }

    private String getCallBackUrl() {
        return providerConfiguration.getRedirectUrl()
                + "?state="
                + strongAuthenticationState.getState();
    }
}
