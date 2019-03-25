package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@JsonObject
public class SecuritiesEntity {
    private AmountEntity amount;
    private List<AccountEntity> accounts;
    private String type;

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList());
    }
}
