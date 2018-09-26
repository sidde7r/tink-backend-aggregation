package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.rpc.CredentialsRequest;

public class OAuth2AuthenticationFlow {
    public static Authenticator create(CredentialsRequest request, AgentContext context,
            SupplementalInformationController supplementalInformationController, String redirectUri,
            OAuth2Authenticator authenticator) {

        // Both manual and auto authenticator.
        OAuth2AuthenticationController oAuth2AuthenticationController = new OAuth2AuthenticationController(
                supplementalInformationController,
                authenticator,
                redirectUri
        );

        /*
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController,
                        supplementalInformationController
                ),
                oAuth2AuthenticationController
        );
        */
        return null;
    }
}
