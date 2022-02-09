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
import se.tink.libraries.i18n_aggregation.Catalog;

public class MitIdScreenFlowControllerModuleTest {

    @Test
    public void should_initialize_module_without_errors() {
        // given
        MitIdAuthenticator mitIdAuthenticator = mock(MitIdAuthenticator.class);

        ProxySaveResponseMatcher matcher = mock(ProxySaveResponseMatcher.class);
        when(mitIdAuthenticator.getMatcherForAuthenticationFinishResponse()).thenReturn(matcher);

        MitIdLocatorsElements locatorsElements = mock(MitIdLocatorsElements.class);
        when(mitIdAuthenticator.getLocatorsElements()).thenReturn(locatorsElements);

        // when
        MitIdScreenFlowController flowController =
                MitIdScreenFlowControllerModule.createMitIdScreenFlowController(
                        mock(Catalog.class),
                        mock(StatusUpdater.class),
                        mock(SupplementalInformationController.class),
                        mock(WebDriverService.class),
                        mitIdAuthenticator);

        // then
        assertThat(flowController).isNotNull();
    }
}
