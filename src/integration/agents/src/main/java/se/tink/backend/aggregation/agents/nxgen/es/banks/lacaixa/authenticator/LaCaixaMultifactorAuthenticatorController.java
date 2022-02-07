package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;

@RequiredArgsConstructor
public class LaCaixaMultifactorAuthenticatorController extends StatelessProgressiveAuthenticator {
    private final LaCaixaManualAuthenticator laCaixaManualAuthenticator;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return laCaixaManualAuthenticator.getAuthenticationSteps();
    }
}
