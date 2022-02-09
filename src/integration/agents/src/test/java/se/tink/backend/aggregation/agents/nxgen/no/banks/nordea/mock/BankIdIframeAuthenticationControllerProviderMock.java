package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.mock;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdTestUtils.mockProxyResponseWithHeaders;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationControllerProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.BankIdIframeController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxyManager;
import se.tink.integration.webdriver.service.proxy.ProxyResponse;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n_aggregation.Catalog;

/**
 * The goal of this mock is to return {@link BankIdIframeAuthenticationController} that will not use
 * {@link org.openqa.selenium.WebDriver} at all. Additionally, we mock authorization code returned
 * from {@link ProxyManager}.
 */
@Ignore
public class BankIdIframeAuthenticationControllerProviderMock
        implements BankIdIframeAuthenticationControllerProvider {

    @Override
    public BankIdIframeAuthenticationController createAuthController(
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            BankIdIframeInitializer iframeInitializer,
            BankIdIframeAuthenticator iframeAuthenticator,
            UserAvailability userAvailability,
            AgentTemporaryStorage agentTemporaryStorage) {

        WebDriverService bankIdWebDriver = mock(WebDriverService.class);

        ProxySaveResponseFilter authFinishProxyFilter = mock(ProxySaveResponseFilter.class);
        ProxyResponse proxyResponse =
                mockProxyResponseWithHeaders(
                        ImmutableMap.of(
                                "someKey1",
                                "someValue1",
                                "Location",
                                "http://redirect.url?key1=value1&code=AUTH_CODE&key2=value2",
                                "someKey2",
                                "someValue2"));
        when(authFinishProxyFilter.waitForResponse(anyInt(), any()))
                .thenReturn(Optional.of(proxyResponse));

        BankIdAuthenticationState authenticationState = mock(BankIdAuthenticationState.class);

        BankIdIframeController iframeController = mock(BankIdIframeController.class);

        return new BankIdIframeAuthenticationController(
                bankIdWebDriver,
                agentTemporaryStorage,
                authenticationState,
                iframeInitializer,
                iframeAuthenticator,
                authFinishProxyFilter,
                iframeController,
                userAvailability);
    }
}
