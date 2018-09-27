package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities.AccountPermissionDataEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities.RiskEntity;

@JsonObject
public class AccountPermissionRequest {
    @JsonProperty("Data")
    private AccountPermissionDataEntity data;
    @JsonProperty("Risk")
    private RiskEntity risk;

    private AccountPermissionRequest(
            AccountPermissionDataEntity data,
            RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    public static AccountPermissionRequest create(){
        return new AccountPermissionRequest(
                AccountPermissionDataEntity.create(),
                new RiskEntity());
    }
}
