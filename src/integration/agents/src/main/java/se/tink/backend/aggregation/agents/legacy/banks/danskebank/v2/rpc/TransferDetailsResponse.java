package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferDetailsResponse extends AbstractResponse {
    @JsonProperty("TransactionType")
    private String transactionType;

    @JsonProperty("UseGlobalPayment")
    private boolean useGlobalPayment;

    @JsonProperty("ToAccounts")
    private List<TransferAccountEntity> toAccounts;

    @JsonProperty("ToAccountsBankGiro")
    private List<TransferAccountEntity> toAccountsBankGiro;

    @JsonProperty("ToAccountsPlusGiro")
    private List<TransferAccountEntity> toAccountsPlusGiro;

    @JsonProperty("FromAccounts")
    private List<TransferAccountEntity> fromAccounts;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public boolean isUseGlobalPayment() {
        return useGlobalPayment;
    }

    public void setUseGlobalPayment(boolean useGlobalPayment) {
        this.useGlobalPayment = useGlobalPayment;
    }

    public List<TransferAccountEntity> getToAccounts() {
        return toAccounts;
    }

    public void setToAccounts(List<TransferAccountEntity> toAccounts) {
        this.toAccounts = toAccounts;
    }

    public List<TransferAccountEntity> getFromAccounts() {
        return fromAccounts;
    }

    public void setFromAccounts(List<TransferAccountEntity> fromAccounts) {
        this.fromAccounts = fromAccounts;
    }

    public List<TransferAccountEntity> getToAccountsBankGiro() {
        return toAccountsBankGiro;
    }

    public void setToAccountsBankGiro(List<TransferAccountEntity> toAccountsBankGiro) {
        this.toAccountsBankGiro = toAccountsBankGiro;
    }

    public List<TransferAccountEntity> getToAccountsPlusGiro() {
        return toAccountsPlusGiro;
    }

    public void setToAccountsPlusGiro(List<TransferAccountEntity> toAccountsPlusGiro) {
        this.toAccountsPlusGiro = toAccountsPlusGiro;
    }
}
