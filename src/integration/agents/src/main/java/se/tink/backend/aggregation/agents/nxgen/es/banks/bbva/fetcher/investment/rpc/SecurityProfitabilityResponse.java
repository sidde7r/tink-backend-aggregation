package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import io.vavr.control.Option;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.ProfitabilityEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityProfitabilityResponse {
    private List<ProfitabilityEntity> items;

    @JsonIgnore
    public double getTotalProfit() {
        return Option.of(items)
                .getOrElse(List::empty)
                .map(ProfitabilityEntity::getTotalProfit)
                .sum()
                .doubleValue();
    }
}
