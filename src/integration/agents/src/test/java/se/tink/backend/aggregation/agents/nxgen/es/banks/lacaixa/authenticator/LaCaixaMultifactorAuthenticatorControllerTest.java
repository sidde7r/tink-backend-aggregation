package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.Catalog;

public class LaCaixaMultifactorAuthenticatorControllerTest {
    private LaCaixaManualAuthenticator laCaixaManualAuthenticator;
    private ImaginBankProxyAuthenticatior imaginBankProxyAuthenticatior;

    @Before
    public void setUp() {
        imaginBankProxyAuthenticatior =
                new ImaginBankProxyAuthenticatior(
                        mock(LaCaixaApiClient.class), mock(LogMasker.class));
        laCaixaManualAuthenticator =
                new LaCaixaManualAuthenticator(
                        mock(LaCaixaApiClient.class),
                        mock(PersistentStorage.class),
                        mock(LogMasker.class),
                        mock(SupplementalInformationFormer.class),
                        mock(Catalog.class),
                        mock(Credentials.class),
                        mock(SupplementalInformationHelper.class));
    }

    @Test
    public void should_route_auth_to_imaginBankProxyAuthenticator_if_hours_between_0_and_7_am() {
        // given

        Clock clock = Clock.fixed(Instant.ofEpochMilli(1654909320000l), ZoneId.of("Europe/Madrid"));

        LaCaixaMultifactorAuthenticatorController controller =
                new LaCaixaMultifactorAuthenticatorController(
                        imaginBankProxyAuthenticatior, laCaixaManualAuthenticator, clock);

        // when
        List<AuthenticationStep> steps = controller.authenticationSteps();

        // then
        assertThat(steps).hasSize(1);
    }

    @Test
    public void should_route_auth_to_LaCaixaManualAuthenticator_if_hours_between_0_and_7_am() {
        // given
        Clock clock = Clock.fixed(Instant.ofEpochMilli(1637245414000l), ZoneId.of("Europe/Madrid"));

        LaCaixaMultifactorAuthenticatorController controller =
                new LaCaixaMultifactorAuthenticatorController(
                        imaginBankProxyAuthenticatior, laCaixaManualAuthenticator, clock);

        // when
        List<AuthenticationStep> steps = controller.authenticationSteps();

        // then
        assertThat(steps).hasSize(7);
    }
}
