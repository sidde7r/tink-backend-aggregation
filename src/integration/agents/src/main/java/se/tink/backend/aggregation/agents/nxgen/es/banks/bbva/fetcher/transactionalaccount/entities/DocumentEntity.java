package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DocumentEntity {
    private String rel;
    private String href;

    public String getRel() {
        return rel;
    }

    public String getHref() {
        return href;
    }
}
