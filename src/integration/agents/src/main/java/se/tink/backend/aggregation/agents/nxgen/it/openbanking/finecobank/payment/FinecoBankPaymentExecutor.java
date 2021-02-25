package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.FinecoBankSignSteps;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class FinecoBankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private FinecoBankApiClient apiClient;
    private SessionStorage sessionStorage;

    public FinecoBankPaymentExecutor(FinecoBankApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amountEntity =
                new AmountEntity(paymentRequest.getPayment().getExactCurrencyAmountFromField());

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        CreatePaymentRequest requestBody =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorEntity)
                        .debtorAccount(debtorEntity)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .instructedAmount(amountEntity)
                        .remittanceInformationUnstructured(remittanceInformation.getValue())
                        .build();

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(getPaymentProduct(paymentRequest), requestBody);
        sessionStorage.put(
                createPaymentResponse.getPaymentId(), createPaymentResponse.getScaRedirectLink());
        return createPaymentResponse.toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {

        return apiClient
                .getPayment(
                        getPaymentProduct(paymentRequest),
                        paymentRequest.getPayment().getUniqueId())
                .toTinkPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        PaymentStatus paymentStatus;
        String nextStep;
        Payment payment = paymentMultiStepRequest.getPayment();
        switch (paymentMultiStepRequest.getStep()) {
            case FinecoBankSignSteps.SAMPLE_STEP:
                GetPaymentStatusResponse responseStatus =
                        apiClient.getPaymentStatus(
                                getPaymentProduct(paymentMultiStepRequest), payment.getUniqueId());
                paymentStatus =
                        FinecoBankPaymentStatus.mapToTinkPaymentStatus(
                                FinecoBankPaymentStatus.fromString(responseStatus.getStatus()));
                nextStep = AuthenticationStepConstants.STEP_FINALIZE;
                break;
            default:
                throw new IllegalStateException(
                        String.format(
                                ErrorMessages.UNKNOWN_SIGNING_STEP,
                                paymentMultiStepRequest.getStep()));
        }
        payment.setStatus(paymentStatus);
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBenficiary is not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new IllegalStateException(
                "cancel is not implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        List<PaymentResponse> paymentResponseList = new ArrayList<>();
        paymentListRequest
                .getPaymentRequestList()
                .forEach(paymentRequest -> paymentResponseList.add(fetch(paymentRequest)));
        return new PaymentListResponse(paymentResponseList);
    }

    private String getPaymentProduct(PaymentRequest paymentRequest) {
        return paymentRequest.getPayment().isSepa()
                ? FinecoBankPaymentProduct.SEPA_CREDIT_TRANSFER.getValue()
                : FinecoBankPaymentProduct.CROSS_BORDER_CREDIT_TRANSFER.getValue();
    }
}
