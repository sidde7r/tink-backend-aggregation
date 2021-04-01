package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankIdWebDriverModuleComponents {
    private final BankIdWebDriver webDriver;
    private final ProxyManager proxyManager;
}
