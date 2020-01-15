package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;

@NoArgsConstructor
public class ConsentScaResponse extends ConsentResponse {

    private List<ScaMethodEntity> scaMethods;
    @JsonIgnore private ScaMethodEntity chosenEntity;

    public ConsentScaResponse(
            LinksEntity links,
            String consentId,
            String consentStatus,
            List<ScaMethodEntity> scaMethods) {
        super(links, consentId, consentStatus);
        this.scaMethods = scaMethods;
    }

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }
}
