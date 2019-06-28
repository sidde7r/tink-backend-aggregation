package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;

@JsonObject
public class AccountWrapper {
    @JsonProperty("MonetaryAccountBank")
    private AccountEntity account;

    @JsonProperty("MonetaryAccountJoint")
    private AccountEntity joinAccount;

    @JsonProperty("MonetaryAccountLight")
    private AccountEntity lightAccount;

    @JsonProperty("MonetaryAccountSavings")
    private AccountEntity savingAccount;

    public AccountEntity getAccount() {
        return account;
    }

    public AccountEntity getJoinAccount() {
        return joinAccount;
    }

    public AccountEntity getLightAccount() {
        return lightAccount;
    }

    public AccountEntity getSavingAccount() {
        return savingAccount;
    }

    public List<CheckingAccount> toTinkAccounts() {

        AccountEntity[] accountArray = {account, joinAccount, lightAccount, savingAccount};

        List<AccountEntity> accountList =
                Arrays.stream(accountArray).filter(s -> (s != null)).collect(Collectors.toList());

        return accountList.stream()
                .map(AccountEntity::toTinkCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
