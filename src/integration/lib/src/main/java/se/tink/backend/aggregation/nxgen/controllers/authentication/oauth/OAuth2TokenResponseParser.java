package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

public interface OAuth2TokenResponseParser {

    OAuth2Token parse(final String accessTokenRawResponse);
}
