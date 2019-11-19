package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreatePaymentRequest {
    private AccountEntity creditorAccount;
    private AccountEntity debtorAccount;
    private AmountEntity instructedAmount;
    private String creditorFriendlyName;
    //    private String creditorAddress;
    //    private String creditorName;
    //    private String remittanceInformationUnstructured;
    //    private String priority;
    //    private String chargeBearer;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtorAccount,
            AmountEntity instructedAmount,
            String creditorAddress,
            String creditorName,
            String remittanceInformationUnstructured,
            String priority,
            String chargeBearer,
            String creditorFriendlyName) {
        this.creditorAccount = creditorAccount;
        this.debtorAccount = debtorAccount;
        this.instructedAmount = instructedAmount;
        //        this.creditorAddress = creditorAddress;
        //        this.creditorName = creditorName;
        //        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        //        this.priority = priority;
        //        this.chargeBearer = chargeBearer;
        this.creditorFriendlyName = creditorFriendlyName;
    }
}
