package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity.CreditorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity.DebtorAccountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.entity.InstructedAmountRequest;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.executor.payment.rpc.CreatePaymentRequest;
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

public class ErstebankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private final ErstebankApiClient apiClient;

    public ErstebankPaymentExecutor(ErstebankApiClient apiClient) {
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

        final CreditorAccountRequest creditorAccount =
                CreditorAccountRequest.builder()
                        .iban(payment.getCreditor().getAccountNumber())
                        .build();

        final DebtorAccountRequest debtorAccount =
                DebtorAccountRequest.builder().iban(payment.getDebtor().getAccountNumber()).build();

        final InstructedAmountRequest instructedAmount =
                InstructedAmountRequest.builder()
                        .amount(payment.getAmount().doubleValue())
                        .currency(payment.getCurrency())
                        .build();

        final CreatePaymentRequest createPaymentRequest =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditorAccount)
                        .debtorAccount(debtorAccount)
                        .instructedAmount(instructedAmount)
                        .creditorName(payment.getCreditor().getName())
                        .requestedExecutionDate(
                                payment.getExecutionDate()
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        .build();

        return apiClient
                .createPayment(createPaymentRequest)
                .toTinkPaymentResponse(
                        creditorAccount,
                        debtorAccount,
                        payment.getExactCurrencyAmount(),
                        payment.getExecutionDate());
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient.fetchPayment(paymentRequest).toTinkPaymentResponse(paymentRequest);
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
