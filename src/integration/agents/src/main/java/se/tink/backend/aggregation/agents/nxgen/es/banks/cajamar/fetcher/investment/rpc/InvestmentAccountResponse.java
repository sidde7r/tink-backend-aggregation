package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.rpc;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentAccountResponse {
    private String account;
    private String currency;
    private BigDecimal investmentFundTotal;
    private List<PortfolioEntity> portfolioValues;
}
