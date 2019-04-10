package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities.InstrumentGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PortfolioResponse extends CrossKeyResponse {

    @JsonProperty("account")
    private PortfolioEntity portfolio;

    private List<InstrumentGroupEntity> instrumentGroups;

    public PortfolioEntity getPortfolio() {
        return portfolio;
    }

    public List<InstrumentGroupEntity> getInstrumentGroups() {
        return instrumentGroups;
    }

    private String buildRawType(CrossKeyAccount account) {
        return account.getAccountType() + ":" + account.getAccountTypeName();
    }

    public Portfolio toTinkPortfolio(CrossKeyAccount account) {
        Portfolio tinkPortfolio = new Portfolio();
        tinkPortfolio.setUniqueIdentifier(portfolio.getPortfolioId());
        tinkPortfolio.setTotalValue(portfolio.getMarketValue());
        tinkPortfolio.setRawType(buildRawType(account));
        tinkPortfolio.setType(account.getPortfolioType());
        tinkPortfolio.setTotalProfit(portfolio.getTotalProfit());
        return tinkPortfolio;
    }
}
