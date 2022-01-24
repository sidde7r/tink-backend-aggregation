package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;
import se.tink.libraries.i18n.Catalog;

public class MitIdFlowControllerModuleTest {

    @Test
    public void should_initialize_module_without_errors() {
        // given
        MitIdAuthenticator mitIdAuthenticator = mock(MitIdAuthenticator.class);

        ProxySaveResponseMatcher matcher = mock(ProxySaveResponseMatcher.class);
        when(mitIdAuthenticator.getMatcherForAuthenticationFinishResponse()).thenReturn(matcher);

        MitIdLocators locators = mock(MitIdLocators.class);
        when(mitIdAuthenticator.getLocators()).thenReturn(locators);

        // when
        MitIdFlowController flowController =
                MitIdFlowControllerModule.createMitIdFlowController(
                        mock(Catalog.class),
                        mock(StatusUpdater.class),
                        mock(SupplementalInformationController.class),
                        mock(WebDriverService.class),
                        mitIdAuthenticator);

        // then
        assertThat(flowController).isNotNull();
    }
}
