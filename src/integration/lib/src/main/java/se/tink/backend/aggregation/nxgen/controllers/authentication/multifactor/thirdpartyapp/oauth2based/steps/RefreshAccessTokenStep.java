package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenRefreshStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;

@RequiredArgsConstructor
public class RefreshAccessTokenStep implements AuthenticationStep {

    private final AccessTokenFetcher accessTokenFetcher;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final AccessTokenRefreshStatus accessTokenRefreshStatus =
                accessTokenFetcher.refreshAccessToken();

        return (accessTokenRefreshStatus == AccessTokenRefreshStatus.SUCCESS)
                ? AuthenticationStepResponse.authenticationSucceeded()
                : AuthenticationStepResponse.executeStepWithId(
                        ThirdPartyAppAuthenticationStepCreator.STEP_NAME);
    }

    @Override
    public String getIdentifier() {
        return "refresh_access_token_step";
    }
}
