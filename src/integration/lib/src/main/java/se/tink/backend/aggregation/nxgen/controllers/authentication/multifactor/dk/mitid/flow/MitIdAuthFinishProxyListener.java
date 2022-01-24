package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import se.tink.integration.webdriver.service.proxy.ProxySaveResponseListener;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;

public class MitIdAuthFinishProxyListener extends ProxySaveResponseListener {

    public MitIdAuthFinishProxyListener(ProxySaveResponseMatcher proxySaveResponseMatcher) {
        super(proxySaveResponseMatcher);
    }
}
