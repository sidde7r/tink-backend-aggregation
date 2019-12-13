package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.FilterInvestmentsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.FilterRangesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialInvestmentRequest {
    private List<FilterRangesEntity> filterRanges;
    private List<FilterInvestmentsEntity> investments;

    public FinancialInvestmentRequest(
            String portfolio,
            String isin,
            String market,
            Date startDate,
            String currency,
            BigDecimal amount) {
        this.filterRanges = Collections.singletonList(new FilterRangesEntity(startDate));
        this.investments =
                Collections.singletonList(
                        new FilterInvestmentsEntity(portfolio, isin, market, currency, amount));
    }
}
