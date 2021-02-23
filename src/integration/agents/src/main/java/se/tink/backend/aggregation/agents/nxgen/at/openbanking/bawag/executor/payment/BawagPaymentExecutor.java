package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.rpc.CreatePaymentRequest;
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
import se.tink.libraries.payment.rpc.Payment;

public class BawagPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private final BawagApiClient apiClient;

    public BawagPaymentExecutor(BawagApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        CreditorAccountRequest creditorAccount =
                CreditorAccountRequest.builder()
                        .iban(payment.getCreditor().getAccountNumber())
                        .build();

        DebtorAccountRequest debtorAccount =
                DebtorAccountRequest.builder().iban(payment.getDebtor().getAccountNumber()).build();

        InstructedAmountRequest instructedAmount =
                InstructedAmountRequest.builder()
                        .amount(payment.getExactCurrencyAmount().getDoubleValue())
                        .currency(payment.getCurrency())
                        .build();

        CreatePaymentRequest createPaymentRequest =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorAccount)
                        .debtorAccount(debtorAccount)
                        .instructedAmount(instructedAmount)
                        .creditorName(payment.getCreditor().getName())
                        .requestedExecutionDate(
                                payment.getExecutionDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .isSepa(payment.isSepa())
                        .build();

        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPayment(
                        creditorAccount,
                        debtorAccount,
                        payment.getExactCurrencyAmount(),
                        payment.getExecutionDate(),
                        payment.getType());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPayment(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment().getType());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        throw new NotImplementedException(
                "sign not yet implemented for " + this.getClass().getName());
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }
}
