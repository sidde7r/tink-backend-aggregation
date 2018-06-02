package se.tink.backend.main.rpc;

import java.util.Optional;
import java.util.Map;
import java.util.Objects;
import se.tink.backend.auth.OAuth2ClientRequest;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.core.Client;
import se.tink.backend.core.SessionTypes;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.main.providers.ClientProvider;

public class OAuth2ClientRequestEnricher {

    private final OAuth2ClientProvider oauth2ClientProvider;
    private final ClientProvider clientProvider;

    public OAuth2ClientRequestEnricher(final OAuth2ClientProvider oauth2ClientProvider, final ClientProvider clientProvider) {
        this.oauth2ClientProvider = oauth2ClientProvider;
        this.clientProvider = clientProvider;
    }

    public OAuth2ClientRequest createEnriched(Optional<String> clientKey, Optional<String> oAuth2ClientId) {

        OAuth2ClientRequest.Builder builder = new OAuth2ClientRequest.Builder();

        // All Link clients provide client key
        if (clientKey.isPresent()) {
            Map<String, Client> clients = clientProvider.get();
            if (clients.containsKey(clientKey.get())) {
                Client client = clients.get(clientKey.get());

                if (Objects.equals(client.getSessionType(), SessionTypes.LINK)) {
                    builder.setLinkClient(client);
                }
            }
        }

        if (oAuth2ClientId.isPresent()) {
            Map<String, OAuth2Client> oAuth2Clients = oauth2ClientProvider.get();
            if (oAuth2Clients.containsKey(oAuth2ClientId.get())) {
                builder.setOAuth2Client(oAuth2Clients.get(oAuth2ClientId.get()));
            }
        }

        return builder.build();
    }
}
