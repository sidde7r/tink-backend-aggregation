package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment;

import java.util.Locale;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.PaymentDetails;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.CreditorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.rpc.CreatePaymentRequest;
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
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;

public class SantanderPaymentExecutorSelector implements FetchablePaymentExecutor, PaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private SantanderApiClient apiClient;

    public SantanderPaymentExecutorSelector(SantanderApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        PaymentType type =
                SantanderConstants.PAYMENT_TYPE_MAPPER
                        .translate(payment.getCreditorAndDebtorAccountType())
                        .orElse(PaymentType.UNDEFINED);

        AmountEntity amount =
                new AmountEntity(
                        payment.getExactCurrencyAmount().getCurrencyCode(),
                        String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()));

        DebtorEntity debtor =
                new DebtorEntity(payment.getDebtor().getAccountNumber(), payment.getCurrency());
        CreditorEntity creditor =
                new CreditorEntity(payment.getCreditor().getAccountNumber(), payment.getCurrency());

        CreatePaymentRequest request =
                new CreatePaymentRequest.Builder()
                        .withCreditor(creditor)
                        .withDebtor(debtor)
                        .withAmount(amount)
                        .withCreditorName(PaymentDetails.CREDITOR_NAME)
                        .withCreditorAgent(PaymentDetails.CREDITOR_AGENT)
                        .withEndToEndIdentification(PaymentDetails.END_TO_END_IDENTIFICATION)
                        .withRemittanceInformationUnstructured(
                                PaymentDetails.REMMITANCE_INFORMATION)
                        .withRequestedExecutionDate(payment.getExecutionDate().toString())
                        .build();

        return apiClient
                .createSepaPayment(request)
                .toTinkPayment(payment.getDebtor(), payment.getCreditor(), type);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        String paymentId = paymentRequest.getPayment().getUniqueId();
        PaymentType type = paymentRequest.getPayment().getType();

        return apiClient.fetchPayment(paymentId).toTinkPayment(paymentId, type);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        throw new NotImplementedException(
                "Sign not yet implemented for " + this.getClass().getName());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "Create beneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "Cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        throw new NotImplementedException(
                "Fetch multiple not yet implemented for " + this.getClass().getName());
    }
}
