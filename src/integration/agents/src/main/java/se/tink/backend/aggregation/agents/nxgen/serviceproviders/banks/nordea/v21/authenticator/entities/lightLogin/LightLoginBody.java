package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.authenticator.entities.lightLogin;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LightLoginBody {
    private LightLoginRequestEntity lightLoginRequest;

    public LightLoginRequestEntity getLightLoginRequest() {
        return lightLoginRequest;
    }

    public void setLightLoginRequest(LightLoginRequestEntity lightLoginRequest) {
        this.lightLoginRequest = lightLoginRequest;
    }

    @JsonIgnore
    public static LightLoginBody passwordLogin(
            String username, String password, String marketCode) {
        LightLoginRequestEntity loginEntity = new LightLoginRequestEntity();

        loginEntity.setUserId(username);
        loginEntity.setPassword(password);
        loginEntity.setLightLoginType(marketCode);

        LightLoginBody body = new LightLoginBody();
        body.setLightLoginRequest(loginEntity);

        return body;
    }
}
