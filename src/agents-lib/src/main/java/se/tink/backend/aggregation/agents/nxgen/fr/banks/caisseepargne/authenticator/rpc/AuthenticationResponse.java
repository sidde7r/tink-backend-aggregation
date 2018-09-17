package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericResponse;

public class AuthenticationResponse extends GenericResponse {

    public AuthenticationResponse(Map<String, Object> result) {
        super(result);
    }

}
