package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountApiEntity {
    private String href;
    private String method;
    private AccountParamsEntity params;

    public AccountParamsEntity getParams() {
        return params;
    }
}
