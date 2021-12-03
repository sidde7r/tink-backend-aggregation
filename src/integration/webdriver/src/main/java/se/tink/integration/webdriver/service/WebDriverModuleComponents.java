package se.tink.integration.webdriver.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.integration.webdriver.service.proxy.ProxyManager;

@Getter
@RequiredArgsConstructor
public class WebDriverModuleComponents {
    private final WebDriverService webDriver;
    private final ProxyManager proxyManager;
}
