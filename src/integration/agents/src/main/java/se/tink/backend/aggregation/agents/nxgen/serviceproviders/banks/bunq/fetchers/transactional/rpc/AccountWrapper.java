package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

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

    public List<TransactionalAccount> toTinkAccounts() {

        List<Optional<TransactionalAccount>> accountList = new ArrayList<>();

        if (account != null) {
            accountList.add(account.toTinkAccount(TransactionalAccountType.CHECKING));
        }

        if (joinAccount != null) {
            accountList.add(joinAccount.toTinkAccount(TransactionalAccountType.CHECKING));
        }

        if (lightAccount != null) {
            accountList.add(lightAccount.toTinkAccount(TransactionalAccountType.CHECKING));
        }

        if (savingAccount != null) {
            accountList.add(savingAccount.toTinkAccount(TransactionalAccountType.SAVINGS));
        }

        return accountList.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
