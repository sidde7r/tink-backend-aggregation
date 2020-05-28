package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountActionsEntity {
    private AccountApiEntity api;
    private boolean disabled;
    private String featureId;
    private String label;
    private String web;

    public AccountApiEntity getApi() {
        return api;
    }
}
