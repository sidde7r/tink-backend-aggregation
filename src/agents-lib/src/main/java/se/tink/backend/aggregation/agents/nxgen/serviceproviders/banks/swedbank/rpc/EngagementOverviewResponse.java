package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EngagementOverviewResponse {
    private List<TransactionalAccountEntity> transactionAccounts;
    private List<TransactionDisposalAccountEntity> transactionDisposalAccounts;
    private List<LoanAccountEntity> loanAccounts;
    private List<SavingAccountEntity> savingAccounts;
    private List<CardAccountEntity> cardAccounts;
    private boolean accessToHSB;
    private boolean showCreditCardLink;
    private boolean errorFetchingCreditCards;
    private boolean showCreditCardIncreaseLimitLink;
    private boolean eligibleForOverdraftLimit;

    public boolean hasTransactionAccount(AbstractAccountEntity account) {
        if (Objects.isNull(account)) {
            return false;
        }

        return transactionAccounts.stream().anyMatch(account::isSameAccount);
    }

    public List<TransactionalAccountEntity> getTransactionAccounts() {
        return transactionAccounts;
    }

    public List<TransactionDisposalAccountEntity> getTransactionDisposalAccounts() {
        return transactionDisposalAccounts;
    }

    public List<LoanAccountEntity> getLoanAccounts() {
        return loanAccounts;
    }

    public List<SavingAccountEntity> getSavingAccounts() {
        return savingAccounts;
    }

    public List<CardAccountEntity> getCardAccounts() {
        return cardAccounts;
    }

    public boolean isAccessToHSB() {
        return accessToHSB;
    }

    public boolean isShowCreditCardLink() {
        return showCreditCardLink;
    }

    public boolean isErrorFetchingCreditCards() {
        return errorFetchingCreditCards;
    }

    public boolean isShowCreditCardIncreaseLimitLink() {
        return showCreditCardIncreaseLimitLink;
    }

    public boolean isEligibleForOverdraftLimit() {
        return eligibleForOverdraftLimit;
    }
}
