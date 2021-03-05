package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.transactional.entity;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.BbvaMxConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class DetailEntity {
    private String level;
    private List<SpecificAmountsItemEntity> specificAmounts;
    private List<IndicatorsItemEntity> indicators;

    public ExactCurrencyAmount getCheckingBalance() {
        return specificAmounts.stream()
                .filter(x -> x.getId().equalsIgnoreCase(BbvaMxConstants.VALUES.CURRENT_BALANCE))
                .map(x -> x.getAmounts().get(0).getAmount())
                .findFirst()
                .get();
    }
}
