package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk.mitid.flow;

import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;

public class MitIdAuthFinishProxyFilter extends ProxySaveResponseFilter {

    public MitIdAuthFinishProxyFilter(ProxySaveResponseMatcher proxySaveResponseMatcher) {
        super(proxySaveResponseMatcher);
    }
}
