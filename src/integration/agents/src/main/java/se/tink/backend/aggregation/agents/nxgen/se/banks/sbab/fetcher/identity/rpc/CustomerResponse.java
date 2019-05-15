package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.identity.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class CustomerResponse {
    @JsonProperty("personal_identity_number")
    private String personalIdentityNumber;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("first_name")
    private String firstName;

    public IdentityData toTinkIdentity() {
        return SeIdentityData.of(firstName, lastName, personalIdentityNumber);
    }
}
