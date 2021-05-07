package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class IdentityResponse {
    private String name;
    private String firstName;
    private String lastName;
}
