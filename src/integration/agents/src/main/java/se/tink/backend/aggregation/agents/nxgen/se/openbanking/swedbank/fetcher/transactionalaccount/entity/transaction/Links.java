package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Links {

    private StartAuthorisation startAuthorisation;

    private Status status;

    public StartAuthorisation getStartAuthorisation() {
        return startAuthorisation;
    }

    public Status getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "Links{"
                + "startAuthorisation = '"
                + startAuthorisation
                + '\''
                + ",status = '"
                + status
                + '\''
                + "}";
    }
}
