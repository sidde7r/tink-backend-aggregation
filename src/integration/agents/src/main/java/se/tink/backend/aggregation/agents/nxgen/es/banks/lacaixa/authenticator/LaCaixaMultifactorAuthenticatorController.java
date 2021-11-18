package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;

@RequiredArgsConstructor
public class LaCaixaMultifactorAuthenticatorController extends StatelessProgressiveAuthenticator {
    private final ImaginBankProxyAuthenticatior imaginBankProxyAuthenticatior;
    private final LaCaixaManualAuthenticator laCaixaManualAuthenticator;
    private final Clock clock;

    @Override
    public List<AuthenticationStep> authenticationSteps() {

        LocalDateTime localDateTime = LocalDateTime.now(clock);
        if (localDateTime.getHour() >= 0 && localDateTime.getHour() < 7) {
            return imaginBankProxyAuthenticatior.getAuthenticationSteps();
        }
        return laCaixaManualAuthenticator.getAuthenticationSteps();
    }
}
