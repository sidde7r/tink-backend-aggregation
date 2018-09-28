package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.entities.RiskEntity;

@JsonObject
public class AccountPermissionResponse {
    @JsonProperty("Data")
    private AccountPermissionsDataResponseEntity data;
    @JsonProperty("Risk")
    private RiskEntity risk;
    @JsonProperty("Links")
    private Map<String, String> links;
    @JsonProperty("Meta")
    private Map<String, Object> meta;

    public AccountPermissionsDataResponseEntity getData() {
        return data;
    }

    public RiskEntity getRisk() {
        return risk;
    }

    public Map<String, String> getLinks() {
        return links;
    }

    public Map<String, Object> getMeta() {
        return meta;
    }
}
