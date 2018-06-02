package se.tink.backend.core.oauth2;

import java.util.Optional;
import se.tink.backend.auth.OAuth2ClientRequest;

public class OAuth2Utils {

    public static Optional<OAuth2Client> getOAuth2Client(OAuth2ClientRequest oauth2ClientRequest) {
        return getOAuth2Client(Optional.ofNullable(oauth2ClientRequest));
    }

    public static Optional<OAuth2Client> getOAuth2Client(Optional<OAuth2ClientRequest> oauth2ClientRequest) {
        if (oauth2ClientRequest.isPresent()) {
            return oauth2ClientRequest.get().getoAuth2Client();
        }

        return Optional.empty();
    }

    public static Optional<String> getPayloadValue(Optional<OAuth2Client> oAuth2Client, String key) {
        if (oAuth2Client.isPresent()) {
            return oAuth2Client.get().getPayloadValue(key);
        }

        return Optional.empty();
    }
}
