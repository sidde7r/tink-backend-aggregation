package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.entities.AccountPermissionDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountPermissionRequest {
    @JsonProperty("Data")
    private AccountPermissionDataEntity data;

    @JsonProperty("Risk")
    private RiskEntity risk;

    private AccountPermissionRequest(AccountPermissionDataEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    public static AccountPermissionRequest create() {
        return new AccountPermissionRequest(AccountPermissionDataEntity.create(), new RiskEntity());
    }
}
