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

    public LogonRequest(String username, String password) {
        this.loginCredentials = LoginCredentials.createLoginCredentials(username, password);
    }

    public LogonRequest(String username, String password, String rememberMeToken) {
        this.loginCredentials =
                LoginCredentials.createLoginCredentials(username, password, rememberMeToken);
    }
}
