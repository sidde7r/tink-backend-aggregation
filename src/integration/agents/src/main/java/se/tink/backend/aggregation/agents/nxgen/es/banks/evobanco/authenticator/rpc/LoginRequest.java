package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class LoginRequest extends AbstractForm {

    public LoginRequest(String username, String password) {
        put(EvoBancoConstants.FormKey.USERNAME, username);
        put(EvoBancoConstants.FormKey.PASSWORD, password);
    }
}
