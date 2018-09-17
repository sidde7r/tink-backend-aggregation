package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.rpc;

import java.util.Map;

public abstract class GenericResponse {

    protected final Map<String, Object> result;

    protected GenericResponse(Map<String, Object> result) {
        this.result = result;
    }

    public boolean isResponseOK() {
        if (result instanceof Map) {
            return "0000".equals(result.get("CodeRetour"));
        } else {
            return false;
        }
    }

}
