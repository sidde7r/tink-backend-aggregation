package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorAgentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.CreditorNameEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.executor.payment.entities.RemittanceInformationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class CreatePaymentRequest {

    private RemittanceInformationEntity remittanceInformation;

    @JsonProperty("creditor")
    private CreditorNameEntity creditorInfo;

    private CreditorAgentEntity creditorAgent;

    private AccountEntity creditorAccount;

    @JsonProperty("debtorAccount")
    private AccountEntity debtor;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    public CreatePaymentRequest(
            AccountEntity creditorAccount,
            AccountEntity debtor,
            AmountEntity amount,
            RemittanceInformationEntity remittanceInformation,
            CreditorNameEntity creditorInfo,
            CreditorAgentEntity creditorAgentEntity) {
        this.creditorAccount = creditorAccount;
        this.debtor = debtor;
        this.amount = amount;
        this.remittanceInformation = remittanceInformation;
        this.creditorInfo = creditorInfo;
        this.creditorAgent = creditorAgentEntity;
    }
}
