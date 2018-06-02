package se.tink.backend.grpc.v1.converter.authentication;

import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.core.Client;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.grpc.v1.converter.Converter;
import se.tink.backend.rpc.InitiateBankIdAuthenticationCommand;
import se.tink.grpc.v1.rpc.InitiateBankIdAuthenticationRequest;

public class InitiateBankIdAuthenticationRequestConverter implements
        Converter<InitiateBankIdAuthenticationRequest, InitiateBankIdAuthenticationCommand> {

    private AuthenticationContext authenticationContext;

    public InitiateBankIdAuthenticationRequestConverter(AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    @Override
    public InitiateBankIdAuthenticationCommand convertFrom(InitiateBankIdAuthenticationRequest input) {
        return InitiateBankIdAuthenticationCommand.builder()
                .withClient(authenticationContext.getClient().map(Client::getId).orElse(null))
                .withDeviceId(authenticationContext.getUserDeviceId().orElse(null))
                .withMarket(input.getMarketCode())
                .withOauth2ClientId(authenticationContext.getOAuth2Client().map(OAuth2Client::getId).orElse(null))
                .withNationalId(input.getNationalId())
                .withAuthenticationToken(input.getAuthenticationToken())
                .build();
    }
}
