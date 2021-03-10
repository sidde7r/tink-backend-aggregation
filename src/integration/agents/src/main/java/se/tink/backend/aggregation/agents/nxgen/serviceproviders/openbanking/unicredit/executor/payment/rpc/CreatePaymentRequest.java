package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.executor.payment.entity.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuperBuilder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatePaymentRequest {

    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String creditorName;
    private String remittanceInformationUnstructured;
    // When the bank decide to follow the doc, remove the -"
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "CET")
    private String requestedExecutionDate;
}
