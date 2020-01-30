package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class FetchIdentityDataResponse {
    // e.g. 199201234567
    @JsonProperty("customer_id")
    private String customerId;

    private String segment;

    @JsonProperty("loyalty_group")
    private String loyaltyGroup;

    // e.g. 199201234567
    @JsonProperty("person_id")
    private String personId;

    // e.g, 1992-01-23
    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("phone_number")
    private String phoneNumber;

    private boolean employee;

    @JsonProperty("us_resident")
    private boolean usResident;

    public IdentityData toTinkIdentityData() {
        return SeIdentityData.of(firstName, lastName, personId);
    }
}
