package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.rpc.LinksResponse;

public class AccountsResponse extends LinksResponse {
    private List<AccountEntity> accounts;
    private AmountEntity totalBalance;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    public AmountEntity getTotalBalance() {
        return totalBalance;
    }
}
