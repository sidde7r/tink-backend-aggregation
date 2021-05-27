package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n.Catalog;

public class BankIdIframeModuleTest {

    private Catalog catalog;
    private StatusUpdater statusUpdater;
    private SupplementalInformationController supplementalInformationController;
    private BankIdWebDriver bankIdWebDriver;
    private BankIdAuthenticationState authenticationState;

    @Before
    public void setup() {
        catalog = mock(Catalog.class);
        statusUpdater = mock(StatusUpdater.class);
        supplementalInformationController = mock(SupplementalInformationController.class);
        bankIdWebDriver = mock(BankIdWebDriver.class);
        authenticationState = mock(BankIdAuthenticationState.class);
    }

    @Test
    public void should_initialize_iframe_module_without_errors() {
        // when
        BankIdIframeController iframeController =
                BankIdIframeModule.initializeIframeController(
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        bankIdWebDriver,
                        authenticationState);

        // then
        assertThat(iframeController).isNotNull();
    }
}
