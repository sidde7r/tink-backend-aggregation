package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.IsinMap;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities.OrderbookEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentTransactionsResponse {
    private String firstTransactionDate;
    private List<OrderbookEntity> orderbooks;

    public String getFirstTransactionDate() {
        return firstTransactionDate;
    }

    public List<OrderbookEntity> getOrderbooks() {
        return Optional.ofNullable(orderbooks).orElseGet(Collections::emptyList);
    }

    public IsinMap toIsinMap() {
        return new IsinMap(
                getOrderbooks().stream()
                        .collect(
                                Collectors.toMap(
                                        OrderbookEntity::getName,
                                        OrderbookEntity::getIsin,
                                        (isin1, isin2) -> isin1)));
    }
}
