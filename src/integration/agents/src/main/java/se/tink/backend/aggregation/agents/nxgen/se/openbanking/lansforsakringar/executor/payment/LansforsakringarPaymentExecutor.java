package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class LansforsakringarPaymentExecutor implements PaymentExecutor {
    private LansforsakringarApiClient apiClient;

    public LansforsakringarPaymentExecutor(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        PaymentType type =
                LansforsakringarConstants.PAYMENT_TYPE_MAPPER
                        .translate(paymentRequest.getPayment().getCreditorAndDebtorAccountType())
                        .orElse(PaymentType.UNDEFINED);
        switch (type) {
            case DOMESTIC:
                return createDomesticPayment(paymentRequest);
            case SEPA:
                return createCrossBorderPayment(paymentRequest);
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    private PaymentResponse createCrossBorderPayment(PaymentRequest paymentRequest) {
        AccountIbanEntity creditor =
                new AccountIbanEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());

        CreditorAddressEntity creditorAddress =
                new CreditorAddressEntity(
                        paymentRequest.getPayment().getFromTemporaryStorage(StorageKeys.CITY),
                        paymentRequest.getPayment().getFromTemporaryStorage(StorageKeys.COUNTRY),
                        paymentRequest.getPayment().getFromTemporaryStorage(StorageKeys.STREET));
        AccountEntity debtor =
                new AccountEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());
        AmountEntity amount = new AmountEntity(paymentRequest.getPayment().getAmount());
        CrossBorderPaymentRequest crossBorderPaymentRequest =
                new CrossBorderPaymentRequest(
                        creditor,
                        creditorAddress,
                        paymentRequest.getPayment().getCreditor().getName(),
                        debtor,
                        amount,
                        FormKeys.SEPA,
                        paymentRequest
                                .getPayment()
                                .getExecutionDate()
                                .format(DateTimeFormatter.ofPattern("dd MM yyyy")));
        CrossBorderPaymentResponse crossBorderPaymentResponse =
                apiClient.createCrossBorderPayment(crossBorderPaymentRequest);
        GetPaymentStatusResponse getPaymentStatusResponse =
                apiClient.getPaymentStatus(
                        crossBorderPaymentResponse.getInitiatedCrossBorderPayment().getPaymentId());

        PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(getPaymentStatusResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return crossBorderPaymentResponse.toTinkPayment(creditor, debtor, status);
    }

    private PaymentResponse createDomesticPayment(PaymentRequest paymentRequest) {
        AccountEntity creditor =
                new AccountEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());
        AccountEntity debtor =
                new AccountEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(),
                        paymentRequest.getPayment().getCurrency());
        AmountEntity amount = new AmountEntity(paymentRequest.getPayment().getAmount());
        DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(creditor, debtor, amount);

        DomesticPaymentResponse domesticPaymentResponse =
                apiClient.createDomesticPayment(domesticPaymentRequest);
        PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(domesticPaymentResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return domesticPaymentResponse.toTinkPayment(creditor, debtor, amount.toAmount(), status);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        switch (type) {
            case DOMESTIC:
                return apiClient.getDomesticPayment(paymentId).toTinkPayment(paymentId);
            case SEPA:
                return apiClient.getCrossBorderPayment(paymentId).toTinkPayment(paymentId);
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        String nextStep;
        nextStep = AuthenticationStepConstants.STEP_FINALIZE;
        apiClient.signPayment(paymentMultiStepRequest.getPayment().getUniqueId());
        Payment payment = paymentMultiStepRequest.getPayment();

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
    public PaymentListResponse fetchMultiple(PaymentRequest paymentRequest)
            throws PaymentException {
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
