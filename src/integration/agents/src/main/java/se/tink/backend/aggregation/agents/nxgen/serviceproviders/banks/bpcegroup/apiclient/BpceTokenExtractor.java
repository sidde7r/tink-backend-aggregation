package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient;

import java.util.Arrays;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;

public class BpceTokenExtractor {

    OAuth2Token extractToken(HttpResponse httpResponse) {
        final String location = httpResponse.getLocation().toString();
        final OAuth2Token oAuth2Token = new OAuth2Token();

        Arrays.stream(location.split("#"))
                .map(string -> string.split("&"))
                .flatMap(Arrays::stream)
                .map(string -> string.split("="))
                .filter(element -> element.length == 2)
                .forEach(
                        element -> {
                            final String key = element[0];
                            final String value = element[1];
                            if ("access_token".equalsIgnoreCase(key)) {
                                oAuth2Token.setAccessToken(value);
                            } else if ("token_type".equalsIgnoreCase(key)) {
                                oAuth2Token.setTokenType(value);
                            } else if ("expires_in".equalsIgnoreCase(key)) {
                                oAuth2Token.setExpiresInSeconds(Long.parseLong(value));
                            }
                        });

        return oAuth2Token;
    }
}
