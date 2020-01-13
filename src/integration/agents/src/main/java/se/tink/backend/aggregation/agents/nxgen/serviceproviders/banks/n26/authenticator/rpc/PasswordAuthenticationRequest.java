package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26Constants;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class PasswordAuthenticationRequest extends AbstractForm {

    public PasswordAuthenticationRequest(String username, String password) {
        this.put(N26Constants.Body.GRANT_TYPE, N26Constants.Body.Password.PASSWORD);
        this.put(N26Constants.Body.Password.USERNAME, username);
        this.put(N26Constants.Body.Password.PASSWORD, password);
    }
}
