package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.rpc;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@NoArgsConstructor
@JsonObject
public class DomesticPaymentRequest {

    public DomesticPaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount,
            String requestedExecutionDate,
            String remittanceInformationUnstructured) {
        endToEndIdentification = RandomUtils.generateRandomAlphabeticString(35);
        this.debtorAccount = debtorAccount;
        this.creditorAccount = creditorAccount;
        this.instructedAmount = instructedAmount;
        this.requestedExecutionDate = requestedExecutionDate;
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    private String endToEndIdentification;
    private AccountEntity debtorAccount;
    private AccountEntity creditorAccount;
    private AmountEntity instructedAmount;
    private String requestedExecutionDate;
    private String remittanceInformationUnstructured;
}
