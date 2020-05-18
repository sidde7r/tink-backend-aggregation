package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountGroupEntity {
    @JsonProperty("libelleFamilleCompte")
    private String accountGroupLabel;

    @JsonProperty("idFamilleCompte")
    private int accountGroupId;

    @JsonProperty("compte")
    private List<AccountEntity> accounts;

    public String getAccountGroupLabel() {
        return accountGroupLabel;
    }

    public int getAccountGroupId() {
        return accountGroupId;
    }

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public boolean isSavingsAccounts() {
        return accountGroupId == BnpParibasConstants.AccountType.SAVINGS_ACCOUNT_GROUP_NUMBER;
    }

    public boolean isCheckingAccount() {
        return accountGroupId == BnpParibasConstants.AccountType.CHECKING_ACCOUNT_GROUP_NUMBER;
    }

    public boolean isInvestmentAccount() {
        return accountGroupId == BnpParibasConstants.AccountType.INVESTMENT_ACCOUNT_GROUP_NUMBER;
    }
}
