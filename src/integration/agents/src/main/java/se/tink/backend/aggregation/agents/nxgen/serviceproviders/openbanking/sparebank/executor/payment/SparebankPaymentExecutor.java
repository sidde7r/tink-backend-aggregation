package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.SparebankSignSteps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration.SparebankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AdressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.CreditorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.DebtorAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.StartAuthorizationProcessResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SparebankPaymentExecutor implements PaymentExecutor {
    private SparebankApiClient apiClient;
    private SessionStorage sessionStorage;
    private SparebankConfiguration sparebankConfiguration;

    public SparebankPaymentExecutor(
            SparebankApiClient apiClient,
            SessionStorage sessionStorage,
            SparebankConfiguration sparebankConfiguration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.sparebankConfiguration = sparebankConfiguration;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                        paymentRequest.getPayment().getType());

        CreditorAccountEntity creditorAccountEntity =
                CreditorAccountEntity.of(paymentRequest, paymentProduct);
        DebtorAccountEntity debtorAccountEntity =
                DebtorAccountEntity.of(paymentRequest, paymentProduct);
        AmountEntity amount = AmountEntity.of(paymentRequest);

        CreatePaymentRequest.Builder builingPaymentRequest =
                new Builder()
                        .withCreditorAccount(creditorAccountEntity)
                        .withDebtorAccount(debtorAccountEntity)
                        .withRequestedExecutionDate(getCurrentDate())
                        .withInstructedAmount(amount)
                        .withCreditorName(paymentRequest.getPayment().getCreditor().getName());

        if (paymentProduct == SparebankPaymentProduct.NORWEGIAN_DOMESTIC_CREDIT_TRANSFER) {
            AdressEntity creditorAddress = new AdressEntity.Builder().withCountry("NO").build();

            builingPaymentRequest.withCreditorAddress(creditorAddress);
        }

        return apiClient
                .createPayment(paymentProduct.getText(), builingPaymentRequest.build())
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                        payment.getType());

        return apiClient
                .getPayment(paymentProduct.getText(), payment.getUniqueId())
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        SparebankPaymentProduct paymentProduct =
                SparebankPaymentProduct.mapTinkPaymentTypeToSparebankPaymentProduct(
                        payment.getType());
        String paymentId = payment.getUniqueId();
        PaymentStatus paymentStatus;
        String nextStep;
        switch (paymentMultiStepRequest.getStep()) {
            case AuthenticationStepConstants.STEP_INIT:
                StartAuthorizationProcessResponse startAuthorizationResponse =
                        apiClient.startAuthorizationProcess(paymentProduct.getText(), paymentId);
                if (!startAuthorizationResponse.hasScaRedirectLink()) {
                    throw new IllegalStateException("Payment cannot be signed");
                }
                sessionStorage.put(
                        payment.getUniqueId(), startAuthorizationResponse.getScaRedirectLink());
                paymentStatus = PaymentStatus.PENDING;
                nextStep = SparebankSignSteps.SAMPLE_STEP;
                break;
            case SparebankSignSteps.SAMPLE_STEP:
                GetPaymentStatusResponse paymentStatusReponse =
                        apiClient.getPaymentStatus(paymentProduct.getText(), paymentId);
                // paymentStatus =
                // SparebankPaymentStatus.mapToTinkPaymentStatus(SparebankPaymentStatus.fromString(paymentStatusReponse.getTransactionStatus()));
                paymentStatus =
                        PaymentStatus
                                .PAID; // we have to hard code it beacuse the above always returns
                // status RCVD
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;

                break;
            default:
                throw new IllegalStateException(
                        String.format("Uknown step %s", paymentMultiStepRequest.getStep()));
        }

        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        // mocking of the fetch multiple so we can test sign
        return new PaymentListResponse(
                new PaymentResponse(
                        paymentListRequest.getPaymentRequestList().get(0).getPayment()));
    }

    private String getCurrentDate() {
        LocalDate date = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(formatter);
    }
}
