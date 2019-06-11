package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentCoverageRequestResourceEntity {
    @JsonProperty("paymentCoverageRequestId")
    private String paymentCoverageRequestId = null;

    @JsonProperty("payee")
    private String payee = null;

    @JsonProperty("instructedAmount")
    private AmountTypeEntity instructedAmount = null;

    @JsonProperty("accountId")
    private AccountIdentificationEntity accountId = null;

    public String getPaymentCoverageRequestId() {
        return paymentCoverageRequestId;
    }

    public void setPaymentCoverageRequestId(String paymentCoverageRequestId) {
        this.paymentCoverageRequestId = paymentCoverageRequestId;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public AmountTypeEntity getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(AmountTypeEntity instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public AccountIdentificationEntity getAccountId() {
        return accountId;
    }

    public void setAccountId(AccountIdentificationEntity accountId) {
        this.accountId = accountId;
    }
}
