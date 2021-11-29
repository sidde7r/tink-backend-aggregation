package se.tink.agent.sdk.authentication.authenticators.oauth2;

public interface ExchangeAuthorizationCode {
    RefreshableAccessTokenAndConsentLifetime exchangeAuthorizationCode(String authorizationCode);
}
