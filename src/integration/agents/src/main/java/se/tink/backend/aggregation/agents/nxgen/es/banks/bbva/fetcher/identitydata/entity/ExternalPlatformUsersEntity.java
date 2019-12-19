package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExternalPlatformUsersEntity {
    private String externalPlatformCode;
    private String userExternalId;
}
