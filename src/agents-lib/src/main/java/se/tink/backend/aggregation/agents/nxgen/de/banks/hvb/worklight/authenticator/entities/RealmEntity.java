package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class RealmEntity {
    private String userId;
    private AttributesEntity attributes;
    private int isUserAuthenticated;
    private String displayName;
}
