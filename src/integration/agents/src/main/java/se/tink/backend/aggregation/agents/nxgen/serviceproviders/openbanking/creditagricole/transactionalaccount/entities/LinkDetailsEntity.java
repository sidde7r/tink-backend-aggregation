package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkDetailsEntity {
    private String href;
    private Boolean templated;

    public String getHref() {
        return href;
    }
}
