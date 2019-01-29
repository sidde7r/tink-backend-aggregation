package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class FetchInvestmentResponse {
    @JsonProperty
    private List<InvestmentAccountEntity> accounts;

    public List<InvestmentAccount> toTinkInvestmentAccounts() {
        return accounts.stream()
                .filter(InvestmentAccountEntity::hasHoldings)
                .map(InvestmentAccountEntity::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }
}
