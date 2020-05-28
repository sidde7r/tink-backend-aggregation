package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.rpc;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.CardArtRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.authenticator.entities.LoginCredentials;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LogonRequest {
    private String amexPayKey;
    private List<CardArtRequest> cardArtRequest = ImmutableList.of(new CardArtRequest());
    private boolean checkSmsRedundancy = false;
    private LoginCredentials loginCredentials;

    public LogonRequest(String amexPayKey, String username, String password) {
        this.amexPayKey = amexPayKey;
        this.loginCredentials = LoginCredentials.createLoginCredentials(username, password);
    }

    public LogonRequest(
            String amexPayKey, String username, String password, String rememberMeToken) {
        this.amexPayKey = amexPayKey;
        this.loginCredentials =
                LoginCredentials.createLoginCredentials(username, password, rememberMeToken);
    }
}
