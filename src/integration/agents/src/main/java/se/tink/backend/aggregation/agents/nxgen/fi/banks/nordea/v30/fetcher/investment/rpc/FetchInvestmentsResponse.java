package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.rpc;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

@JsonObject
public class FetchInvestmentsResponse {

    private List<InvestmentAccountEntity> accounts;

    public List<InvestmentAccount> toTinkInvestmentAccounts() {

        return accounts.stream()
                .map(InvestmentAccountEntity::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }
}
