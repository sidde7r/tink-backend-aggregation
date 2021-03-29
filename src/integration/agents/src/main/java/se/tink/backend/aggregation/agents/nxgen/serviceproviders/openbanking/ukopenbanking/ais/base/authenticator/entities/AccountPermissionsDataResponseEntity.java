package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AccountPermissionsDataResponseEntity extends AccountPermissionDataEntity {

    private String accountRequestId;
    private String creationDateTime;
    private UkOpenBankingApiDefinitions.ConsentStatus status;
    private String statusUpdateDateTime;

    public boolean isAuthorised() {
        return this.status.equals(UkOpenBankingApiDefinitions.ConsentStatus.AUTHORISED);
    }

    public boolean isNotAuthorised() {
        return !isAuthorised();
    }

    public boolean isAwaitingAuthorisation() {
        return this.status.equals(UkOpenBankingApiDefinitions.ConsentStatus.AWAITING_AUTHORISATION);
    }
}
