package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v20.fetcher.authenticator.entities.AccountPermissionsDataResponseEntityV20;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionResponseV20 extends AccountPermissionResponse {
    @JsonProperty("Data")
    private AccountPermissionsDataResponseEntityV20 data;

    @Override
    public AccountPermissionsDataResponseEntityV20 getData() {
        return data;
    }
}
