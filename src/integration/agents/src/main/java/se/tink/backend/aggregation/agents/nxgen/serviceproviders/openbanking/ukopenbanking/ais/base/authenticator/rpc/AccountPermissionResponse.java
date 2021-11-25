package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.BaseV31Response;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionResponse
        extends BaseV31Response<AccountPermissionsDataResponseEntity> {

    @JsonProperty("Data")
    public void setData(AccountPermissionsDataResponseEntity data) {
        this.data = data;
    }

    public String getConsentId() {
        return data.getAccountConsentId();
    }

    public Instant getCreationDate() {
        return data.getCreationDateTime();
    }
}
