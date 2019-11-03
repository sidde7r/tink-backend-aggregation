package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.authenticator.entities.AccountPermissionsDataResponseEntityV30;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionResponseV30 extends AccountPermissionResponse {
    @JsonProperty("Data")
    private AccountPermissionsDataResponseEntityV30 data;

    @Override
    public AccountPermissionsDataResponseEntityV30 getData() {
        return data;
    }
}
