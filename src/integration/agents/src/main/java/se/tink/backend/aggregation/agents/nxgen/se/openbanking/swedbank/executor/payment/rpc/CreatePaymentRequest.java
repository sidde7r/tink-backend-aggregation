package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.RemittanceInformationStructuredEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@Builder
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private String creditorFriendlyName;
    private String debtorAccountStatementText;
    private String executionDate;
    private AmountEntity instructedAmount;
    private RemittanceInformationStructuredEntity remittanceInformationStructured;
    private String remittanceInformationUnstructured;
}
