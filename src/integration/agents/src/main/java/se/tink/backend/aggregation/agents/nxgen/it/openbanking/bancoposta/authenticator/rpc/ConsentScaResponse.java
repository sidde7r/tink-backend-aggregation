package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.ConsentResponse;

public class ConsentScaResponse extends ConsentResponse {

    private List<ScaMethodEntity> scaMethods;
    @JsonIgnore private ScaMethodEntity chosenEntity;

    public List<ScaMethodEntity> getScaMethods() {
        return scaMethods;
    }
}
