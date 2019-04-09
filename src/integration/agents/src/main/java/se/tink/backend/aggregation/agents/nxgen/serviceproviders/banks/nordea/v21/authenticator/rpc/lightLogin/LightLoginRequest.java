package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.authenticator.rpc.lightLogin;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.authenticator.entities.lightLogin.LightLoginBody;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequestImpl;

public class LightLoginRequest extends HttpRequestImpl {
    public LightLoginRequest(String username, String password, String marketCode) {
        super(
                HttpMethod.POST,
                NordeaV21Constants.Url.LIGHT_LOGIN.get(),
                LightLoginBody.passwordLogin(username, password, marketCode));
    }
}
