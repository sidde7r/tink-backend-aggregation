package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.entities.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ScaMethodEntity;

@NoArgsConstructor
@Getter
public class ConsentScaResponse extends ConsentResponse {

    private List<ScaMethodEntity> scaMethods;

    public ConsentScaResponse(
            LinksEntity links,
            String consentId,
            String consentStatus,
            List<ScaMethodEntity> scaMethods) {
        super(links, consentId, consentStatus);
        this.scaMethods = scaMethods;
    }
}
