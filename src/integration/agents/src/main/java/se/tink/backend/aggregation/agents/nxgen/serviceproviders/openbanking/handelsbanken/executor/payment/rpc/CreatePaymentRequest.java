package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.Creditor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformation;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {

    private RemittanceInformation remittanceInformation;

    @JsonProperty("creditor")
    private Creditor creditorInfo;

    private CreditorAgent creditorAgent;

    private AccountEntity creditorAccount;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtor,
            AmountEntity amount,
            RemittanceInformation remittanceInformation,
            Creditor creditorInfo,
            CreditorAgent creditorAgent) {
        this.creditorAccount = creditorAccount;
        this.debtor = debtor;
        this.amount = amount;
        this.remittanceInformation = remittanceInformation;
        this.creditorInfo = creditorInfo;
        this.creditorAgent = creditorAgent;
    }

    public CreatePaymentRequest(
            AccountEntity creditorAccount, AccountEntity debtor, AmountEntity amount) {
        this.creditorAccount = creditorAccount;
        this.debtor = debtor;
        this.amount = amount;
    }
}
