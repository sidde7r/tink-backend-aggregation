package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

@JsonObject
public class InvestmentAccountResponse extends SpankkiResponse {
    @JsonProperty private Boolean userHasInvestments;

    @JsonProperty("positionReport")
    private InvestmentAccountEntity investmentsAccount;

    public Boolean getUserHasInvestments() {
        return userHasInvestments;
    }

    public InvestmentAccountEntity getInvestmentsAccount() {
        return investmentsAccount;
    }

    @JsonIgnore
    public List<InvestmentAccount> toTinkInvestmentAccount(
            List<PortfolioModule> portfolioModule, String customerUserId) {
        return Collections.singletonList(
                investmentsAccount.toTinkInvestmentAccount(portfolioModule, customerUserId));
    }
}
