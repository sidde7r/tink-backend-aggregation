package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsLinksEntity {
    private Href balances;
    private Href transactions;
}
