package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.RetrieveAccessTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.steps.ThirdPartyAppAuthenticationStepCreator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;

public class OAuth2BasedTokenAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;

    public OAuth2BasedTokenAuthenticator(
            AccessTokenFetcher accessTokenFetcher,
            ThirdPartyAppAuthenticationStepCreator thirdPartyAppAuthenticationStepCreator) {
        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(accessTokenFetcher),
                        new RefreshAccessTokenStep(accessTokenFetcher),
                        thirdPartyAppAuthenticationStepCreator.create(),
                        new RetrieveAccessTokenStep(accessTokenFetcher));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }
}
