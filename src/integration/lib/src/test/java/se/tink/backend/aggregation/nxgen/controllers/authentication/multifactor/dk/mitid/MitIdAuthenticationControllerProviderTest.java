package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.MitIdLocators;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n.Catalog;

public class MitIdAuthenticationControllerProviderTest {

    @Test
    public void should_initialize_controller_without_errors() {
        // given
        MitIdAuthenticator mitIdAuthenticator = mock(MitIdAuthenticator.class);

        ProxySaveResponseMatcher matcher = mock(ProxySaveResponseMatcher.class);
        when(mitIdAuthenticator.getMatcherForAuthenticationFinishResponse()).thenReturn(matcher);

        MitIdLocators locators = mock(MitIdLocators.class);
        when(mitIdAuthenticator.getLocators()).thenReturn(locators);

        // when
        MitIdAuthenticationController authenticationController =
                new MitIdAuthenticationControllerProviderImpl()
                        .createAuthController(
                                mock(Catalog.class),
                                mock(StatusUpdater.class),
                                mock(SupplementalInformationController.class),
                                mock(UserAvailability.class),
                                mock(AgentTemporaryStorage.class),
                                mitIdAuthenticator);

        // then
        assertThat(authenticationController).isNotNull();
    }
}
