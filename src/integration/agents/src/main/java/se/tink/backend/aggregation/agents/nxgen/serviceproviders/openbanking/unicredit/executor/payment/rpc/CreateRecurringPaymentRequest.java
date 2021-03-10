package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CreateRecurringPaymentRequest extends CreatePaymentRequest {
    private String startDate;
    private String endDate;
    private String frequency;
    private String executionRule;
    private String dayOfExecution;
}
