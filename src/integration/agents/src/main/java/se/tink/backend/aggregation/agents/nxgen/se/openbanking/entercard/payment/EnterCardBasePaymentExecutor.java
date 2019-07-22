package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.configuration.EnterCardConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.dto.PaymentInitiationRequestParams;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardCurrency;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.rpc.PaymentInitiationRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.util.EnterCardPaymentUtil;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.controllers.utils.sca.ScaRedirectCallbackHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class EnterCardBasePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private EnterCardApiClient apiClient;
    private ScaRedirectCallbackHandler redirectCallbackHandler;
    private EnterCardConfiguration configuration;

    public EnterCardBasePaymentExecutor(
            EnterCardApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            EnterCardConfiguration configuration) {
        this.apiClient = apiClient;
        this.configuration = configuration;
        this.redirectCallbackHandler =
                new ScaRedirectCallbackHandler(supplementalInformationHelper, 30, TimeUnit.SECONDS);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        String state = UUID.randomUUID().toString();
        String creditorAccountNumber = paymentRequest.getPayment().getCreditor().getAccountNumber();
        String debtorAccountNumber = paymentRequest.getPayment().getDebtor().getAccountNumber();

        PaymentInitiationRequestParams params =
                EnterCardPaymentUtil.getPaymentInitiationReqParams(
                        paymentRequest.getPayment().getCreditor().getAccountIdentifierType(),
                        paymentRequest.getPayment().getDebtor().getAccountIdentifierType(),
                        creditorAccountNumber);

        PaymentInitiationRequest request =
                new PaymentInitiationRequest.PaymentInitiationRequestBuilder()
                        .withPaymentRequestType(params.getRequestType())
                        .withOcrNumber(params.getOcrNumber())
                        .withClearingNumber(params.getClearingNumber())
                        .withPayeeAccountNumber(new BigInteger(creditorAccountNumber))
                        .withReceiverAccountType(params.getAccountType())
                        .withPayee(paymentRequest.getPayment().getCreditor().getName())
                        .withCustomerAccountNumber(new BigInteger(debtorAccountNumber))
                        .withAmount(paymentRequest.getPayment().getAmount().getValue())
                        .withCurrency(
                                EnterCardCurrency.fromString(
                                        paymentRequest.getPayment().getCurrency()))
                        .withRedirectURI(
                                new URL(configuration.getRedirectUrl())
                                        .queryParam(EnterCardConstants.QueryKeys.STATE, state)
                                        .toString())
                        .build();

        return apiClient.createPayment(request).toTinkPaymentResponse(paymentRequest, state);
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
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        // TODO: Remove after  testing
        return new PaymentResponse(paymentRequest.getPayment(), paymentRequest.getStorage());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;

        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                // TODO:: Check what we get from callback
                Optional<Map<String, String>> response =
                        redirectCallbackHandler.handleRedirect(
                                new URL(
                                        paymentMultiStepRequest
                                                .getStorage()
                                                .get(StorageKeys.E_SIGN_URL)),
                                paymentMultiStepRequest.getStorage().get(StorageKeys.STATE));

                if (!response.isPresent()) {
                    throw new PaymentException("SCA time-out.");
                }

                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;

            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }

        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(PaymentStatus.PAID);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        // TODO: Remove after  testing
        List<PaymentResponse> response = new ArrayList<PaymentResponse>();

        for (PaymentRequest request : paymentListRequest.getPaymentRequestList()) {
            response.add(fetch(request));
        }

        return new PaymentListResponse(response);
    }
}
