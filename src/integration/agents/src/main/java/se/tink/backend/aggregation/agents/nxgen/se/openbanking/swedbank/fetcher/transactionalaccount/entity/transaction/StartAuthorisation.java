package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthorisation {

    private String href;

    public String getHref() {
        return href;
    }

    @Override
    public String toString() {
        return "StartAuthorisation{" + "href = '" + href + '\'' + "}";
    }
}
