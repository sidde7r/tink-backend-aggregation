package se.tink.backend.aggregation.agents.banks.swedbank.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EngagementOverviewResponse {
    private List<AccountEntity> cardAccounts;
    private List<AccountEntity> loanAccounts;
    private List<AccountEntity> savingAccounts;
    private List<AccountEntity> transactionAccounts;
    private List<AccountEntity> transactionDisposalAccounts;
    private List<AccountEntity> savingDisposalAccounts;

    public List<AccountEntity> getCardAccounts() {
        return cardAccounts;
    }

    public List<AccountEntity> getLoanAccounts() {
        return loanAccounts;
    }

    public List<AccountEntity> getSavingAccounts() {
        return savingAccounts;
    }

    public List<AccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }

    public List<AccountEntity> getTransactionDisposalAccounts() {
        return transactionDisposalAccounts;
    }

    public void setCardAccounts(List<AccountEntity> cardAccounts) {
        this.cardAccounts = cardAccounts;
    }

    public void setLoanAccounts(List<AccountEntity> loanAccounts) {
        this.loanAccounts = loanAccounts;
    }

    public void setSavingAccounts(List<AccountEntity> savingAccounts) {
        this.savingAccounts = savingAccounts;
    }

    public void setTransactionAccounts(List<AccountEntity> transactionAccounts) {
        this.transactionAccounts = transactionAccounts;
    }

    public void setTransactionDisposalAccounts(List<AccountEntity> transactionDisposalAccounts) {
        this.transactionDisposalAccounts = transactionDisposalAccounts;
    }

    public List<AccountEntity> getSavingDisposalAccounts() {
        return this.savingDisposalAccounts;
    }
    
    public void setSavingDisposalAccounts(List<AccountEntity> savingDisposalAccounts) {
        this.savingDisposalAccounts = savingDisposalAccounts;
    }

}
