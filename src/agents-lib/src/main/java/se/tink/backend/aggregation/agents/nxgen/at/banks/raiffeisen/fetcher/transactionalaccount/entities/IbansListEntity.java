package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IbansListEntity {
    private List<String> ibans;

    public IbansListEntity(String iban) {
        this.ibans = Collections.singletonList(iban);
    }
}
