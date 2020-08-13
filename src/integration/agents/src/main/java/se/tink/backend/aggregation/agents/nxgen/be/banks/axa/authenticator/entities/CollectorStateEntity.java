package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CollectorStateEntity {

    private static final String ACTIVE = "active";
    private static final String DISABLED = "disabled";

    private String accounts = ACTIVE;
    private String bluetooth = DISABLED;
    private String capabilities = DISABLED;
    private String contacts = ACTIVE;
    private String devicedetails = ACTIVE;
    private String externalsdkdetails = DISABLED;
    private String fidoauthenticators = DISABLED;
    private String hwauthenticators = DISABLED;
    private String largedata = DISABLED;
    private String localenrollments = DISABLED;
    private String location = DISABLED;
    private String owner = ACTIVE;
    private String software = ACTIVE;
}
