package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.entities.AlandsBankenAccount;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.InstrumentGroupEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class PortfolioResponse extends AlandsBankenResponse {

    @JsonProperty("account")
    private PortfolioEntity portfolio;

    private List<InstrumentGroupEntity> instrumentGroups;

    public PortfolioEntity getPortfolio() {
        return portfolio;
    }

    public List<InstrumentGroupEntity> getInstrumentGroups() {
        return instrumentGroups;
    }

    private String buildRawType(AlandsBankenAccount account) {
        return account.getAccountType() + ":" + account.getAccountTypeName();
    }

    public Portfolio toTinkPortfolio(AlandsBankenAccount account) {
        Portfolio tinkPortfolio = new Portfolio();
        tinkPortfolio.setUniqueIdentifier(portfolio.getPortfolioId());
        tinkPortfolio.setTotalValue(portfolio.getMarketValue());
        tinkPortfolio.setRawType(buildRawType(account));
        tinkPortfolio.setType(account.getPortfolioType());
        tinkPortfolio.setTotalProfit(portfolio.getTotalProfit());
        return tinkPortfolio;
    }
}
