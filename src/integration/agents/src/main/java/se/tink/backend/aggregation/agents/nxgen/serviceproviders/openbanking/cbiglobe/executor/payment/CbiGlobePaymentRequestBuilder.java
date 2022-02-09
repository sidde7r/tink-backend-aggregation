package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.executor.payment.rpc.CreateRecurringPaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CbiGlobePaymentRequestBuilder {

    public CreatePaymentRequest getCreatePaymentRequest(Payment payment) {
        return CreatePaymentRequest.builder()
                .debtorAccount(
                        getAccountEntity(
                                payment.getDebtor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(
                        getAccountEntity(
                                payment.getCreditor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .build();
    }

    public CreatePaymentRequest getCreateRecurringPaymentRequest(Payment payment) {
        return CreateRecurringPaymentRequest.builder()
                .debtorAccount(
                        getAccountEntity(
                                payment.getDebtor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .instructedAmount(getInstructedAmountEntity(payment))
                .creditorAccount(
                        getAccountEntity(
                                payment.getCreditor()
                                        .getAccountIdentifier(IbanIdentifier.class)
                                        .getIban()))
                .creditorName(payment.getCreditor().getName())
                .remittanceInformationUnstructured(getRemittanceInformation(payment).getValue())
                .transactionType(FormValues.TRANSACTION_TYPE)
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate().toString())
                // optional attributes
                .endDate(payment.getEndDate() != null ? payment.getEndDate().toString() : null)
                .executionRule(
                        payment.getExecutionRule() != null
                                ? mapExecutionRule(payment.getExecutionRule())
                                : null)
                .dayOfExecution(getDayOfExecution(payment))
                .build();
    }

    private String mapExecutionRule(ExecutionRule rule) {
        // Bank API has a typo, we need to have a typo as well.
        if (rule == ExecutionRule.PRECEDING) {
            return "preceeding";
        } else {
            return rule.toString();
        }
    }

    private RemittanceInformation getRemittanceInformation(Payment payment) {
        RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);
        return remittanceInformation;
    }

    private InstructedAmountEntity getInstructedAmountEntity(Payment payment) {
        return new InstructedAmountEntity(
                payment.getExactCurrencyAmount().getCurrencyCode(),
                String.valueOf(payment.getExactCurrencyAmount().getDoubleValue()));
    }

    private AccountEntity getAccountEntity(String accountNumber) {
        return new AccountEntity(accountNumber);
    }

    private String getDayOfExecution(Payment payment) {
        switch (payment.getFrequency()) {
            case WEEKLY:
                return String.valueOf(payment.getDayOfWeek().getValue());
            case MONTHLY:
                return payment.getDayOfMonth() != null
                        ? payment.getDayOfMonth().toString()
                        : null; // Credem hates this parameter
            default:
                throw new IllegalArgumentException(
                        "Frequency is not supported: " + payment.getFrequency());
        }
    }
}
