package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class AmountFilterEntity {
    private BigDecimal from;
    private BigDecimal to;

    public AmountFilterEntity() {
        from = null;
        to = null;
    }
}
