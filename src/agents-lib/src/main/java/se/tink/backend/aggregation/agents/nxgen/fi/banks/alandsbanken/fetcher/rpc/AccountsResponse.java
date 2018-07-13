package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.rpc;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenAccount;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

public class AccountsResponse extends AlandsBankenResponse {

    //    Also a field that is sent from backend:
    //    private List<SortSetting> sortSettings;

    private List<AlandsBankenAccount> accounts;

    public Collection<TransactionalAccount> getTransactionalAccounts() {
        return accounts.stream()
                .filter(AlandsBankenAccount::isTransactionalAccount)
                .map(AlandsBankenAccount::toTransactionalAccount)
                .collect(Collectors.toList());
    }

    public List<AlandsBankenAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(
            List<AlandsBankenAccount> accounts) {
        this.accounts = accounts;
    }
}
