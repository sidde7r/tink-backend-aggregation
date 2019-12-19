package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.InvestmentsSummaryEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.SummariesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialInvestmentResponse {
    private List<InvestmentsSummaryEntity> investments;

    public Double getTotalProfit() {
        return Optional.ofNullable(investments).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(InvestmentsSummaryEntity::getSummaries)
                .orElse(Collections.emptyList())
                .stream()
                .sorted()
                .map(SummariesEntity::getProfitAmount)
                .filter(Objects::nonNull)
                .map(number -> number.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue())
                .reduce((first, second) -> second)
                .orElse(null);
    }
}
