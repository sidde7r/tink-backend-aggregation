package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.rpc;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities.GirosCreditorAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class DomesticGirosPaymentRequest {

    private GirosCreditorAccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String requestedExecutionDate;
    private String remittanceInformationUnstructured;
    private String remittanceInformationStructured;
}
