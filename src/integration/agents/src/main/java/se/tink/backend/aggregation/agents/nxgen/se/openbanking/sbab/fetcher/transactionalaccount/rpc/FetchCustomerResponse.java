package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCustomerResponse {

    @JsonProperty("personal_identity_number")
    private String personalIdentityNumber;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @JsonIgnore
    public String getCustomerName() {
        return Optional.ofNullable(getFullName()).orElse("");
    }
}
