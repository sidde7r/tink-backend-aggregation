package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.entities;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLinkEntity {

    private Href account;

    public Href getAccount() {
        return account;
    }
}
