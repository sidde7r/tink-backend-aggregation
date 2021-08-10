package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.time.Instant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ISOInstantDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountPermissionsDataResponseEntity extends AccountPermissionDataEntity {
    private String accountRequestId;

    @JsonDeserialize(using = ISOInstantDeserializer.class)
    private Instant creationDateTime;

    private UkOpenBankingApiDefinitions.ConsentStatus status;

    private String statusUpdateDateTime;

    @JsonProperty("ConsentId")
    private String accountConsentId;

    public boolean isAuthorised() {
        return this.status.equals(UkOpenBankingApiDefinitions.ConsentStatus.AUTHORISED);
    }

    public boolean isNotAuthorised() {
        return !isAuthorised();
    }

    public boolean isAwaitingAuthorisation() {
        return this.status.equals(UkOpenBankingApiDefinitions.ConsentStatus.AWAITING_AUTHORISATION);
    }

    public String getAccountConsentId() {
        return accountConsentId;
    }

    public Instant getCreationDateTime() {
        return creationDateTime;
    }
}
