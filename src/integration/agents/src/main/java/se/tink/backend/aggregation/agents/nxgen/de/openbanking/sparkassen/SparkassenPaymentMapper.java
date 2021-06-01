package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen;

import static java.util.Objects.nonNull;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentMapper;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SparkassenPaymentMapper implements PaymentMapper<CreatePaymentRequest> {

    @Override
    public CreatePaymentRequest getPaymentRequest(Payment payment) {
        return CreatePaymentRequest.builder()
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .requestedExecutionDate(payment.getExecutionDate())
                .build();
    }

    @Override
    public CreatePaymentRequest getRecurringPaymentRequest(Payment payment) {
        return CreateRecurringPaymentRequest.builder()
                .creditorAccount(getAccountEntity(payment.getCreditor().getAccountNumber()))
                .debtorAccount(getAccountEntity(payment.getDebtor().getAccountNumber()))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate())
                // optional attributes
                .endDate(payment.getEndDate())
                .executionRule(
                        payment.getExecutionRule() != null
                                ? payment.getExecutionRule().toString()
                                : null)
                .dayOfExecution(
                        nonNull(payment.getDayOfExecution())
                                ? String.valueOf(payment.getDayOfExecution())
                                : null)
                .build();
    }

    private AmountEntity getAmountEntity(Payment payment) {
        return new AmountEntity(
                String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()),
                payment.getExactCurrencyAmount().getCurrencyCode());
    }

    private String getUnstructuredRemittance(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();

        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        return Optional.ofNullable(remittanceInformation.getValue()).orElse("");
    }

    public AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }
}
