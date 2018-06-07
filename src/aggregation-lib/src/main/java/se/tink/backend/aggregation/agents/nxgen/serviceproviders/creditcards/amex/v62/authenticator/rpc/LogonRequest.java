package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.CardArtRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.LoginCredentials;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonRequest {

    private List<CardArtRequest> cardArtRequest = ImmutableList.of(new CardArtRequest());
    private LoginCredentials loginCredentials;

    public void setLoginCredentials(LoginCredentials loginCredentials) {
        this.loginCredentials = loginCredentials;
    }

    public void setUsernameAndPassword(String username, String password) {
        this.loginCredentials = new LoginCredentials();
        this.loginCredentials.setUsernameAndPassword(username, password);

    }
}
