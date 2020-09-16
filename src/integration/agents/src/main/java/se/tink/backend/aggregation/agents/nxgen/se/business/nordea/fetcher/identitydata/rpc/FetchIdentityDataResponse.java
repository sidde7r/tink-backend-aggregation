package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.identitydata.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
public class FetchIdentityDataResponse {

    // e.g. 199201234567
    @JsonProperty("customer_id")
    private String customerId;

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

    @JsonProperty("us_resident")
    private boolean usResident;

    public IdentityData toTinkIdentityData() {
        return IdentityData.builder()
                .addFirstNameElement(firstName)
                .addSurnameElement(lastName)
                .setDateOfBirth(null)
                .build();
    }
}
