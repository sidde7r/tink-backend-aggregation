package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;

@JsonObject
public class CustomerEntity {
    @JsonProperty private String customerId = "";
    @JsonProperty private String firstName = "";
    @JsonProperty private String lastName = "";
    @JsonProperty private String memberNumber = "";
    @JsonProperty private String analyticsUserId = "";
    @JsonProperty private String gender = "";
    @JsonProperty private String coOpName = "";
    @JsonProperty private String coOpMembershipRole = "";
    @JsonProperty private Boolean isCoOpMember;
    @JsonProperty private String status = "";
    @JsonProperty private Boolean isPersonnel;
    @JsonProperty private Boolean personnel;
    @JsonProperty private Boolean coOpMember;
    @JsonProperty private String birthDate;

    @JsonIgnore
    public String getCustomerId() {
        return customerId;
    }

    @JsonIgnore
    public String getAnalyticsUserId() {
        return analyticsUserId;
    }

    @JsonIgnore
    public IdentityData toTinkIdentity() {
        return IdentityData.builder()
                .addFirstNameElement(firstName)
                .addSurnameElement(lastName)
                .setDateOfBirth(LocalDate.parse(birthDate))
                .build();
    }
}
