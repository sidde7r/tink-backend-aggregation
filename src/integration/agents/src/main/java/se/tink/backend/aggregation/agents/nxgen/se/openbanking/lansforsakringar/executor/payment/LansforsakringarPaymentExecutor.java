package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.CrossBorderPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.DomesticPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc.GetPaymentStatusResponse;
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
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;

public class LansforsakringarPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private LansforsakringarApiClient apiClient;

    public LansforsakringarPaymentExecutor(LansforsakringarApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final PaymentType type =
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
        final Payment payment = paymentRequest.getPayment();
        final Creditor paymentCreditor = payment.getCreditor();
        final AccountIbanEntity creditor =
                new AccountIbanEntity(paymentCreditor.getAccountNumber(), payment.getCurrency());

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        final CreditorAddressEntity creditorAddress =
                new CreditorAddressEntity(FormValues.CITY, FormValues.COUNTRY, FormValues.STREET);
        final AccountEntity debtor =
                new AccountEntity(payment.getDebtor().getAccountNumber(), payment.getCurrency());
        final AmountEntity amount = new AmountEntity(payment.getExactCurrencyAmount());
        final CrossBorderPaymentRequest crossBorderPaymentRequest =
                new CrossBorderPaymentRequest(
                        creditor,
                        creditorAddress,
                        paymentCreditor.getName(),
                        debtor,
                        amount,
                        FormKeys.SEPA,
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)));
        final CrossBorderPaymentResponse crossBorderPaymentResponse =
                apiClient.createCrossBorderPayment(crossBorderPaymentRequest);
        final GetPaymentStatusResponse getPaymentStatusResponse =
                apiClient.getPaymentStatus(
                        crossBorderPaymentResponse.getInitiatedCrossBorderPayment().getPaymentId());

        final PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(getPaymentStatusResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return crossBorderPaymentResponse.toTinkPayment(creditor, debtor, status);
    }

    private PaymentResponse createDomesticPayment(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();
        final String currency = payment.getCurrency();
        final AccountEntity creditor =
                new AccountEntity(payment.getCreditor().getAccountNumber(), currency);
        final AccountEntity debtor =
                new AccountEntity(payment.getDebtor().getAccountNumber(), currency);
        final AmountEntity amount = new AmountEntity(payment.getExactCurrencyAmount());
        final DomesticPaymentRequest domesticPaymentRequest =
                new DomesticPaymentRequest(creditor, debtor, amount);

        final DomesticPaymentResponse domesticPaymentResponse =
                apiClient.createDomesticPayment(domesticPaymentRequest);
        final PaymentStatus status =
                LansforsakringarConstants.PAYMENT_STATUS_MAPPER
                        .translate(domesticPaymentResponse.getTransactionStatus())
                        .orElse(PaymentStatus.UNDEFINED);

        return domesticPaymentResponse.toTinkPayment(creditor, debtor, amount.toAmount(), status);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        final String paymentId = paymentRequest.getPayment().getUniqueId();
        final PaymentType type = paymentRequest.getPayment().getType();

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
        final Payment payment = paymentMultiStepRequest.getPayment();
        final String nextStep = AuthenticationStepConstants.STEP_FINALIZE;
        apiClient.signPayment(payment.getUniqueId());

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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
