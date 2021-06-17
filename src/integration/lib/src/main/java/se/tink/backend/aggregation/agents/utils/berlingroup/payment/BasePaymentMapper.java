package se.tink.backend.aggregation.agents.utils.berlingroup.payment;

import java.util.Optional;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BasePaymentMapper implements PaymentMapper<CreatePaymentRequest> {

    @Override
    public CreatePaymentRequest getPaymentRequest(Payment payment) {
        return CreatePaymentRequest.builder()
                .creditorAccount(getCreditorAccountEntity(payment))
                .debtorAccount(getDebtorAccountEntity(payment))
                .instructedAmount(getAmountEntity(payment))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getUnstructuredRemittance(payment))
                .requestedExecutionDate(payment.getExecutionDate())
                .build();
    }

    @Override
    public CreatePaymentRequest getRecurringPaymentRequest(Payment payment) {
        return CreateRecurringPaymentRequest.builder()
                .creditorAccount(getCreditorAccountEntity(payment))
                .debtorAccount(getDebtorAccountEntity(payment))
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
                .dayOfExecution(getDayOfExecution(payment))
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

    protected AccountEntity getDebtorAccountEntity(Payment payment) {
        return getAccountEntity(payment.getDebtor().getAccountNumber());
    }

    protected AccountEntity getCreditorAccountEntity(Payment payment) {
        return getAccountEntity(payment.getCreditor().getAccountNumber());
    }

    protected AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }

    private String getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return String.valueOf(payment.getDayOfWeek().getValue());
            case MONTHLY:
                return payment.getDayOfMonth().toString();
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }
}
