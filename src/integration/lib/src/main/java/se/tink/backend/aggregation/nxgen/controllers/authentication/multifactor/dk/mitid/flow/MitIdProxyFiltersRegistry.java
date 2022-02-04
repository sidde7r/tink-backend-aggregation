package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.ProxyFilterKeys.LISTEN_CODE_APP_POLLING_FINISHED;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.ProxyFilterKeys.LISTEN_WEB_AUTH_FINISHED;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.MitIdCodeAppPollingProxyFilter;
import se.tink.integration.webdriver.service.WebDriverService;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdProxyFiltersRegistry {

    private final WebDriverService driverService;

    private final ProxySaveResponseFilter authFinishProxyFilter;
    private final MitIdCodeAppPollingProxyFilter codeAppPollingProxyFilter;

    /** For consistency, this the place where we should register all proxy filters */
    public void registerFilters() {
        driverService.enableResponseFiltering();
        driverService.registerProxyFilter(LISTEN_WEB_AUTH_FINISHED, authFinishProxyFilter);
        driverService.registerProxyFilter(
                LISTEN_CODE_APP_POLLING_FINISHED, codeAppPollingProxyFilter);
    }
}
