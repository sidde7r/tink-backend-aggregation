package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
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
        this.scaMethods = Objects.requireNonNull(scaMethods);
    }

    public ScaMethodEntity getScaMethod() {
        /* Banco Posta Auth flow works the same whatever method is chosen. Hence to improve UX,
        choosing method step is removed and chosen method is set as the first element from possible sca methods */
        return scaMethods.stream()
                .findFirst()
                .orElseThrow(() -> LoginError.NO_AVAILABLE_SCA_METHODS.exception());
    }
}
