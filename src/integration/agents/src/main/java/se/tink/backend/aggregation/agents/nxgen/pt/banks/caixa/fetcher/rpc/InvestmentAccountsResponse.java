package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentAccountsResponse {

    private List<InvestmentAccountEntity> accounts;

    private InvestmentAccountEntity defaultAccount;

    public Optional<List<InvestmentAccountEntity>> getAccounts() {
        return Optional.ofNullable(accounts);
    }
}
