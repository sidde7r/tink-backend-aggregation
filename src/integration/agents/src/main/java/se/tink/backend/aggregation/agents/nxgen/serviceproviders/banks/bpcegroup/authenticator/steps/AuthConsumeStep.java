package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.BpceApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.Saml2PostDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.storage.BpceStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class AuthConsumeStep extends AbstractAuthenticationStep {

    public static final String STEP_ID = "authConsumeStep";

    private final BpceApiClient bpceApiClient;
    private final BpceStorage bpceStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        final Saml2PostDto saml2PostDto = bpceStorage.getSamlPostAction();

        final OAuth2Token oAuth2Token = bpceApiClient.oAuth2Consume(saml2PostDto);

        bpceStorage.storeOAuth2Token(oAuth2Token);

        return AuthenticationStepResponse.executeNextStep();
    }

    @Override
    public String getIdentifier() {
        return STEP_ID;
    }
}
