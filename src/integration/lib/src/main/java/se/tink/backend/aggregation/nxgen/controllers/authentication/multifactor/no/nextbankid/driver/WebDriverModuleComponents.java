package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ProxyManager;

@Getter
@RequiredArgsConstructor
public class WebDriverModuleComponents {
    private final WebDriverService webDriver;
    private final ProxyManager proxyManager;
}
