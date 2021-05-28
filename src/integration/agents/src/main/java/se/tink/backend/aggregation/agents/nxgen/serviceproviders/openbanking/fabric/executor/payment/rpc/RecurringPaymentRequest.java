package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.rpc;

import static java.util.Objects.isNull;

import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.executor.payment.entities.InstructedAmountEntity;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.ExecutionRule;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RecurringPaymentRequest extends FabricPaymentRequest {
    String startDate;
    String endDate;
    String frequency;
    String executionRule;
    String dayOfExecution;

    public static FabricPaymentRequest createFrom(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        AccountEntity creditorEntity = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtorEntity = AccountEntity.debtorOf(paymentRequest);
        InstructedAmountEntity instructedAmountEntity = InstructedAmountEntity.of(paymentRequest);

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        RecurringPaymentRequestBuilder createRecurringPaymentRequest =
                RecurringPaymentRequest.builder()
                        .debtorAccount(debtorEntity)
                        .instructedAmount(instructedAmountEntity)
                        .creditorAccount(creditorEntity)
                        .creditorName(paymentRequest.getPayment().getCreditor().getName())
                        .remittanceInformationUnstructured(remittanceInformation.getValue())
                        .frequency(payment.getFrequency().toString())
                        .startDate(payment.getStartDate().toString())
                        .dayOfExecution(
                                isNull(payment.getDayOfExecution())
                                        ? null
                                        : String.valueOf(payment.getDayOfExecution()));
        // optional attributes
        if (Optional.ofNullable(payment.getEndDate()).isPresent()) {
            createRecurringPaymentRequest.endDate(payment.getEndDate().toString());
        }
        if (Optional.ofNullable(payment.getExecutionRule()).isPresent()) {
            createRecurringPaymentRequest.executionRule(
                    mapExecutionRule(payment.getExecutionRule()));
        }

        return createRecurringPaymentRequest.build();
    }

    private static String mapExecutionRule(ExecutionRule rule) {
        // Bank API has a typo, we need to have a typo as well.
        if (rule == ExecutionRule.PRECEDING) {
            return "preceeding";
        } else {
            return rule.toString();
        }
    }
}
