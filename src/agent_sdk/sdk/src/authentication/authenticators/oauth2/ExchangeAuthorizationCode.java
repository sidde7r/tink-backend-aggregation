package se.tink.agent.sdk.authentication.authenticators.oauth2;

public interface ExchangeAuthorizationCode {
    AccessTokenAndConsentLifetime exchangeAuthorizationCode(String authorizationCode);
}
