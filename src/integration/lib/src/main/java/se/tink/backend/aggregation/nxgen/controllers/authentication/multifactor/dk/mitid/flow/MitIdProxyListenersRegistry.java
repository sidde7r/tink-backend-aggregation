package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.ProxyListenerKeys.LISTEN_CODE_APP_POLLING_FINISHED;
import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.MitIdConstants.ProxyListenerKeys.LISTEN_WEB_AUTH_FINISHED;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow.steps.codeapp.MitIdCodeAppPollingProxyListener;
import se.tink.integration.webdriver.service.WebDriverService;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class MitIdProxyListenersRegistry {

    private final WebDriverService driverService;

    private final MitIdAuthFinishProxyListener authFinishProxyListener;
    private final MitIdCodeAppPollingProxyListener codeAppPollingProxyListener;

    /** For consistency, this the place where we should register all proxy listeners */
    public void registerListeners() {
        driverService.registerProxyListener(LISTEN_WEB_AUTH_FINISHED, authFinishProxyListener);

        driverService.registerProxyListener(
                LISTEN_CODE_APP_POLLING_FINISHED, codeAppPollingProxyListener);
    }
}
