package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.identity.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class IdentityResponse {
    private String name;
    private String firstName;
    private String lastName;
}
