package se.tink.backend.aggregation.agents.nxgen.es.webpage.cajasur;

import lombok.Getter;

@Getter
public class CajasurSessionState {

    private String authenticationRequestBody;

    public CajasurSessionState(String authenticationRequestBody) {
        this.authenticationRequestBody = authenticationRequestBody;
    }
}
