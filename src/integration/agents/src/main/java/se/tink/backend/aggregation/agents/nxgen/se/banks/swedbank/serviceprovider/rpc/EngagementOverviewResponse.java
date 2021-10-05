package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.List;
import java.util.Objects;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
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
}
