package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity extends AmountEntity {
    private boolean isMainCurrency;
    private boolean isOriginalCurrency;

    public boolean getMainCurrency() {
        return isMainCurrency;
    }

    public boolean getOriginalCurrency() {
        return isOriginalCurrency;
    }
}
