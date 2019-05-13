package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class FetchInvestmentResponse {
    @JsonProperty private List<InvestmentAccountEntity> accounts;

    public List<InvestmentAccountEntity> getAccounts() {
        return accounts;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<InvestmentAccountEntity> accounts) {
        this.accounts = accounts;
    }

    public List<InvestmentAccount> toTinkInvestmentAccounts() {
        return getAccounts().stream()
                .map(InvestmentAccountEntity::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }
}
