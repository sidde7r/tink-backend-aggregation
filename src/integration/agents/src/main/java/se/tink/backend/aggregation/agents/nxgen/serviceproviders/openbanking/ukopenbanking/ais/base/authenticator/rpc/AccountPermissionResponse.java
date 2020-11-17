package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.AccountPermissionsDataResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

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
