package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment;

import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc.DomesticPaymentResponse;
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
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SkandiaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private SkandiaApiClient apiClient;

    public SkandiaPaymentExecutor(SkandiaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {

        final Payment payment = paymentRequest.getPayment();
        final AmountEntity amount = new AmountEntity(payment.getExactCurrencyAmount());
        final AccountEntity debtor = new AccountEntity(payment.getDebtor().getAccountNumber());

        AccountIdentifierType accountIdentifierType =
                paymentRequest.getPayment().getCreditor().getAccountIdentifierType();

        // add DebtorAccount validation

        switch (accountIdentifierType) {
            case SE:
                return createDomesticPayment(payment, debtor, amount);
            case SE_BG:
            case SE_PG:
                // not supported
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    private PaymentResponse createDomesticPayment(
            Payment payment, AccountEntity debtor, AmountEntity amount) throws PaymentException {

        AccountEntity creditor = new AccountEntity(payment.getCreditor().getAccountNumber());

        final DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(
                        creditor,
                        debtor,
                        amount,
                        payment.getExecutionDate().toString(),
                        payment.getRemittanceInformation().getValue());

        DomesticPaymentResponse domesticPaymentResponse = new DomesticPaymentResponse();

        try {
            domesticPaymentResponse = apiClient.createDomesticPayment(domesticPaymentRequest);
        } catch (HttpResponseException ex) {
            HttpResponseExceptionHandler.checkForErrors(ex.getMessage());
        }

        final PaymentStatus status =
                SkandiaConstants.PAYMENT_STATUS_MAPPER
                        .translate(domesticPaymentResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return domesticPaymentResponse.toTinkPayment(creditor, debtor, amount.toAmount(), status);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        final String paymentId = paymentRequest.getPayment().getUniqueId();
        final PaymentType type = paymentRequest.getPayment().getType();

        switch (type) {
            case DOMESTIC:
                return apiClient.getDomesticPayment(paymentId).toTinkPayment(paymentId);
            case SEPA:
            default:
                throw new IllegalStateException(ErrorMessages.UNSUPPORTED_PAYMENT_TYPE);
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {

        throw new NotImplementedException("To be implemented");
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
