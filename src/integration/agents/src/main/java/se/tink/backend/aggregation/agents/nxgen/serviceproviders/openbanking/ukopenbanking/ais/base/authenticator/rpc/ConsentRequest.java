package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Set;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.ConsentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.authenticator.entities.RiskEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ConsentRequest {
    @JsonProperty("Data")
    private ConsentEntity data;

    @JsonProperty("Risk")
    private RiskEntity risk;

    private ConsentRequest(ConsentEntity data, RiskEntity risk) {
        this.data = data;
        this.risk = risk;
    }

    public static ConsentRequest create(Set<String> permissions) {
        return new ConsentRequest(ConsentEntity.of(permissions), new RiskEntity());
    }
}
