package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import se.tink.backend.aggregation.agents.Href;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class AccountsLinksEntity {
    private Href balances;
    private Href transactions;
}
