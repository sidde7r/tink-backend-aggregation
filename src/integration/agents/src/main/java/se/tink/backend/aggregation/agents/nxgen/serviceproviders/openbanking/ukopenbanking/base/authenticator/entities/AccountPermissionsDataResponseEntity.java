package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionsDataResponseEntity extends AccountPermissionDataEntity {

    @JsonProperty("AccountRequestId")
    private String accountRequestId;

    @JsonProperty("ConsentId")
    private String accountConsentId;

    @JsonProperty("CreationDateTime")
    private String creationDateTime;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("StatusUpdateDateTime")
    private String statusUpdateDateTime;

    public String getAccountRequestId() {
        return accountConsentId;
    }

    public String getCreationDateTime() {
        return creationDateTime;
    }

    public String getStatus() {
        return status;
    }

    public String getStatusUpdateDateTime() {
        return statusUpdateDateTime;
    }
}
