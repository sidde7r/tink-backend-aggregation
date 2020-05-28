package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountParamsEntity {
    private String continuationToken;
    private String resourceId;

    public String getContinuationToken() {
        return continuationToken;
    }
}
