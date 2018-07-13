package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserInfoEntity {
    @JsonProperty("AccessAgreement")
    private String accessAgreement;
    @JsonProperty("UserFirstname")
    private String firstName;
    @JsonProperty("UserLastname")
    private String lastname;
    @JsonProperty("UserSegment")
    private String segment;
    @JsonProperty("CustomerSegment")
    private String customerSegment;

    public String getAccessAgreement() {
        return accessAgreement;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastname() {
        return lastname;
    }

    public String getSegment() {
        return segment;
    }

    public String getCustomerSegment() {
        return customerSegment;
    }
}
