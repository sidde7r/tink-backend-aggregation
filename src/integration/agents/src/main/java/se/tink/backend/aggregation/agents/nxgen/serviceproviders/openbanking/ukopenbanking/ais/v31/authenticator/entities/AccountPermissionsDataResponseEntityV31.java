package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionsDataResponseEntityV31 extends AccountPermissionsDataResponseEntity {
    @JsonProperty("ConsentId")
    private String accountConsentId;

    public String getAccountConsentId() {
        return accountConsentId;
    }
}
