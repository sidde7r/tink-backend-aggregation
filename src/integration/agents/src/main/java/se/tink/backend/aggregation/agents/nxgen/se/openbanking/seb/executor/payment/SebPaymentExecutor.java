package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.SebConstants.SebSignSteps;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.entities.PaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc.PaymentSigningRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SebPaymentExecutor implements FetchablePaymentExecutor {

    private SebApiClient apiClient;

    public SebPaymentExecutor(SebApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        String paymentProduct = getPaymentProduct(paymentRequest.getPayment()).getValue();

        DebtorAccountEntity debtorAccountEntity = DebtorAccountEntity.of(paymentRequest);
        CreditorAccountEntity creditorAccountEntity =
                CreditorAccountEntity.paymentProductsMapper
                        .get(PaymentProduct.fromString(paymentProduct))
                        .apply(paymentRequest.getPayment().getCreditor().getAccountNumber());
        AmountEntity amountEntity = AmountEntity.of(paymentRequest);
        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest.Builder()
                        .withTemplateId(paymentProduct)
                        .withCreditorAccount(creditorAccountEntity)
                        .withCreditorAccountMessage("")
                        .withDebtorAccount(debtorAccountEntity)
                        .withDebtorrAccountMessage("")
                        .withExecutionDate(getCurrentDate())
                        .withAmount(amountEntity)
                        .withRemittanceInformationUnstructured(
                                paymentRequest.getPayment().getReference().getValue())
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName())
                        .build();

        return apiClient
                .createPaymentInitiation(createPaymentRequest, paymentProduct)
                .toTinkPaymentResponse(paymentProduct, paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        String paymentProduct = getPaymentProduct(paymentRequest.getPayment()).getValue();
        return apiClient
                .getPayment(paymentRequest.getPayment().getUniqueId(), paymentProduct)
                .toTinkPaymentResponse(
                        paymentProduct, paymentId, paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        PaymentStatus paymentStatus;
        String nextStep;
        String paymentProduct = getPaymentProduct(paymentMultiStepRequest.getPayment()).getValue();
        String paymentId = paymentMultiStepRequest.getPayment().getUniqueId();
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                PaymentStatusResponse paymentStatusResponse =
                        apiClient.getPaymentStatus(paymentId, paymentProduct);
                if (paymentStatusResponse.isReadyForSigning()) {
                    PaymentStatusResponse signingResponse =
                            apiClient.signPayment(
                                    paymentId,
                                    paymentProduct,
                                    new PaymentSigningRequest(
                                            paymentStatusResponse.getAuthenticationMethodId()));
                } else {
                    throw new IllegalStateException("Payment can not be signed at the moment");
                }
                nextStep = SebSignSteps.SAMPLE_STEP;
                paymentStatus =
                        SebPaymentStatus.mapToTinkPaymentStatus(
                                SebPaymentStatus.fromString(
                                        paymentStatusResponse.getTransactionStatus()));
                break;
            case SebSignSteps.SAMPLE_STEP:
                paymentStatus = sampleStepSebAutoSignsAfterAFewSeconds(paymentId, paymentProduct);
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
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
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        // Mocking of fetch multiple because they don't have the endpoint for fetch multiple
        List<PaymentResponse> responseList = new ArrayList<>();
        for (PaymentRequest paymentRequest : paymentListRequest.getPaymentRequestList()) {

            final String paymentProduct = getPaymentProduct(paymentRequest.getPayment()).getValue();
            final String paymentId = paymentRequest.getPayment().getUniqueId();
            PaymentResponse response =
                    apiClient
                            .getPayment(paymentId, paymentProduct)
                            .toTinkPaymentResponse(
                                    paymentProduct,
                                    paymentId,
                                    paymentRequest.getPayment().getType());
            responseList.add(response);
        }
        return new PaymentListResponse(responseList);
    }

    private PaymentStatus sampleStepSebAutoSignsAfterAFewSeconds(
            String providerId, String paymentProduct) throws PaymentException {
        // Should be enough to get the payment auto signed.
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        PaymentStatusResponse paymentResponse =
                apiClient.getPaymentStatus(providerId, paymentProduct);
        return SebPaymentStatus.mapToTinkPaymentStatus(
                SebPaymentStatus.fromString(paymentResponse.getTransactionStatus()));
    }

    private PaymentProduct getPaymentProduct(Payment payment) {
        PaymentType paymentType = payment.getType();
        Type creditorAccountType = payment.getCreditor().getAccountIdentifierType();
        switch (paymentType) {
            case SEPA:
                return PaymentProduct.SEPA_CREDIT_TRANSFER;
            case INTERNATIONAL:
                // this is because currently we don't have needed info in PaymentRequest object to
                // create cross border payment
                throw new IllegalStateException(ErrorMessages.CROSS_BORDER_PAYMENT_NOT_SUPPORTED);
            case DOMESTIC:
                return getDomesticPaymentProduct(creditorAccountType);
            default:
                throw new IllegalStateException(ErrorMessages.UNKNOWN_PAYMENT_PRODUCT);
        }
    }

    private String getCurrentDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }

    private PaymentProduct getDomesticPaymentProduct(Type creditorAccountType) {
        switch (creditorAccountType) {
            case SE_BG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_BNAKGIROS;
            case SE_PG:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_PLUSGIROS;
            default:
                return PaymentProduct.SWEDISH_DOMESTIC_PRIVATE_CREDIT_TRANSFERS;
        }
    }
}
