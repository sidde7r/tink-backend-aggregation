package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.rpc;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc.GenericResponse;

public class ClientInformationResponse extends GenericResponse {

    public ClientInformationResponse(Map<String, Object> result) {
        super(result);
    }

}
