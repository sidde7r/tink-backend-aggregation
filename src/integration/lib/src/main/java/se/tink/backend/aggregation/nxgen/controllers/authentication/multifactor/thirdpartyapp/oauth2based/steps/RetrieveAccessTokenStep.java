package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class RetrieveAccessTokenStep implements AuthenticationStep {

    private final AccessTokenFetcher accessTokenFetcher;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        accessTokenFetcher.retrieveAccessToken();

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    @Override
    public String getIdentifier() {
        return "retrieve_access_token_step";
    }
}
