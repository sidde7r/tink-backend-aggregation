package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private SelfLinksEntity self;
    private MovementsLinksEntity movements;
    private AccountLinksEntity account;
}
