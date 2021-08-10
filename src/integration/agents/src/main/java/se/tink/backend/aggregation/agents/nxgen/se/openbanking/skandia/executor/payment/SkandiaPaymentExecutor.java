package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.MinimumValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.SignPaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class SkandiaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SkandiaApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final StrongAuthenticationState strongAuthenticationState;

    public SkandiaPaymentExecutor(
            SkandiaApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.strongAuthenticationState = strongAuthenticationState;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();

        validatePayment(payment);

        try {
            return apiClient.createPayment(payment).toTinkPayment(payment);
        } catch (HttpResponseException e) {
            throw HttpResponseExceptionHandler.checkForErrors(e);
        }
    }

    private void validatePayment(Payment payment) throws PaymentValidationException {
        if (payment.getExactCurrencyAmount().getExactValue().compareTo(MinimumValues.MINIMUM_AMOUNT)
                < 0) {
            throw new PaymentValidationException(
                    String.format(
                            "Transfer amount can't be less than %.2f SEK",
                            MinimumValues.MINIMUM_AMOUNT),
                    InternalStatus.INVALID_MINIMUM_AMOUNT);
        }
        // add DebtorAccount validation
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {

        return apiClient
                .getPayment(paymentRequest.getPayment())
                .toTinkPayment(paymentRequest.getPayment());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        final Payment payment = paymentMultiStepRequest.getPayment();

        try {
            final String state = strongAuthenticationState.getState();

            final SignPaymentResponse signedPayment = apiClient.signPayment(payment, state);

            authenticatePIS(signedPayment);
        } catch (HttpResponseException e) {
            throw HttpResponseExceptionHandler.checkForErrors(e);
        }

        final PaymentResponse paymentResponse = fetchAndValidatePayment(paymentMultiStepRequest);

        return new PaymentMultiStepResponse(
                paymentResponse, AuthenticationStepConstants.STEP_FINALIZE);
    }

    private PaymentResponse fetchAndValidatePayment(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        PaymentResponse paymentResponse =
                fetch(
                        new PaymentRequest(
                                paymentMultiStepRequest.getPayment(),
                                Storage.copyOf(paymentMultiStepRequest.getStorage())));

        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();

        if (paymentStatus == PaymentStatus.PENDING || paymentStatus == PaymentStatus.CANCELLED) {
            throw new PaymentCancelledException();
        }

        if (paymentStatus != PaymentStatus.SIGNED) {
            throw new PaymentRejectedException(PaymentRejectedException.MESSAGE);
        }

        return paymentResponse;
    }

    private void authenticatePIS(SignPaymentResponse signedPayment) throws PaymentException {
        URL url =
                signedPayment
                        .getScaRedirect()
                        .orElseThrow(
                                () ->
                                        new PaymentException(
                                                ErrorMessages.SCA_REDIRECT_MISSING,
                                                InternalStatus
                                                        .PAYMENT_REJECTED_BY_BANK_NO_DESCRIPTION));

        supplementalInformationHelper.openThirdPartyApp(ThirdPartyAppAuthenticationPayload.of(url));

        supplementalInformationHelper.waitForSupplementalInformation(
                strongAuthenticationState.getSupplementalKey(),
                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                TimeUnit.MINUTES);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return paymentListRequest.getPaymentRequestList().stream()
                .map(this::fetch)
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(), PaymentListResponse::new));
    }
}
