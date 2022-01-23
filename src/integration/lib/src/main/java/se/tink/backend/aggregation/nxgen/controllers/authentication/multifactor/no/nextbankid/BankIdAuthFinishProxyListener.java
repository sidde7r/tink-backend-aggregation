package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import se.tink.integration.webdriver.service.proxy.ProxySaveResponseListener;
import se.tink.integration.webdriver.service.proxy.ProxySaveResponseMatcher;

public class BankIdAuthFinishProxyListener extends ProxySaveResponseListener {

    public BankIdAuthFinishProxyListener(ProxySaveResponseMatcher proxySaveResponseMatcher) {
        super(proxySaveResponseMatcher);
    }
}
