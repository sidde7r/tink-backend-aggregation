package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FormatsEntity {
    private String bocf;
    private String iuc;

    public String getBocf() {
        return bocf;
    }

    public String getIuc() {
        return iuc;
    }
}
