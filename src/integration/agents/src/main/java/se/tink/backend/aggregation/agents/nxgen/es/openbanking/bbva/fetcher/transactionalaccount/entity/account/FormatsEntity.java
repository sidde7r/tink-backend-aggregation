package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FormatsEntity {
    private String iban;
    private String ccc;

    public String getIban() {
        return iban;
    }

    public String getCcc() {
        return ccc;
    }
}
