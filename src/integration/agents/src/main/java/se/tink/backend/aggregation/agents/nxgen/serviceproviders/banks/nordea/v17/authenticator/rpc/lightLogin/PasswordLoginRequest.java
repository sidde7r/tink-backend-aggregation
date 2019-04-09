package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator.rpc.lightLogin;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.NordeaV17Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.authenticator.entities.lightLogin.LightLoginBody;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class PasswordLoginRequest extends HttpRequestImpl {
    public PasswordLoginRequest(String username, String password, String marketCode) {
        super(
                HttpMethod.POST,
                NordeaV17Constants.Url.LIGHT_LOGIN.get(),
                LightLoginBody.passwordLogin(username, password, marketCode));
    }
}
