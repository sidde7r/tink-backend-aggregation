package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionsDataResponseEntityV31 extends AccountPermissionsDataResponseEntity {
    @JsonProperty("ConsentId")
    private String accountConsentId;

    public String getAccountConsentId() {
        return accountConsentId;
    }
}
