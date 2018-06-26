package se.tink.backend.aggregation.agents.nxgen.be.banks.bnppf.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PfmUser {
    private Long smid;
    private String token;
    private String externalUserId;
}
