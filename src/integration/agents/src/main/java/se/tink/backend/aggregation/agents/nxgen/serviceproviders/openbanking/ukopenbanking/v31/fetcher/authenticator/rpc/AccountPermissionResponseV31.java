package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.entities.AccountPermissionsDataResponseEntityV31;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionResponseV31 extends AccountPermissionResponse {
    @JsonProperty("Data")
    private AccountPermissionsDataResponseEntityV31 data;

    @Override
    public AccountPermissionsDataResponseEntityV31 getData() {
        return data;
    }
}
