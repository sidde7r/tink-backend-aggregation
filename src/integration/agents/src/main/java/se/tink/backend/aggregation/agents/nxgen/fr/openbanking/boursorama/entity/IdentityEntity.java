package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class IdentityEntity {

    private String connectedPsu;

    public String getConnectedPsu() {
        return connectedPsu;
    }
}
