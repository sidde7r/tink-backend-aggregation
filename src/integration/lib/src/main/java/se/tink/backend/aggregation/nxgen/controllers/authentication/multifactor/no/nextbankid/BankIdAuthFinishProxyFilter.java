package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.integration.webdriver.service.proxy.ProxySaveResponseFilter;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;

public class BankIdAuthFinishProxyFilter extends ProxySaveResponseFilter {

    public BankIdAuthFinishProxyFilter(ProxySaveResponseMatcher proxySaveResponseMatcher) {
        super(proxySaveResponseMatcher);
    }
}
