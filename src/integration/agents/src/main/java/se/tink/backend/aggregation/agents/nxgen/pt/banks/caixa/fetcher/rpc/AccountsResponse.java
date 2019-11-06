package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<AccountEntity> accounts;

    private AccountEntity defaultAccount;

    public Optional<List<AccountEntity>> getAccounts() {
        return Optional.ofNullable(accounts);
    }
}
