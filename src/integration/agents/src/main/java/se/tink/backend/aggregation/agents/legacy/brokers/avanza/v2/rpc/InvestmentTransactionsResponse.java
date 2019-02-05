package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentTransactionsResponse {
    private String firstTransactionDate;
    private List<OrderbooksEntity> orderbooks;

    public String getFirstTransactionDate() {
        return firstTransactionDate;
    }

    public List<OrderbooksEntity> getOrderbooks() {
        return orderbooks;
    }

    public Map<String, String> getIsinByName() {
        return Optional.ofNullable(orderbooks)
                .map(
                        name ->
                                name.stream()
                                        .collect(
                                                Collectors.toMap(
                                                        OrderbooksEntity::getName,
                                                        OrderbooksEntity::getIsin,
                                                        (isin1, isin2) -> isin1)))
                .orElse(Collections.emptyMap());
    }
}
