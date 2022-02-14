package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites;

import java.math.BigDecimal;
import java.util.Objects;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountsEntity {
    private ExecutedEntity executed;

    public boolean isExecutedEntityNull() {
        return Objects.isNull(executed);
    }

    public BigDecimal getValue() {
        return executed.getValue();
    }

    public String getCurrency() {
        return executed.getCurrency();
    }
}
