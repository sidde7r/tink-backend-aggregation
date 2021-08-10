package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;

@Getter
@EqualsAndHashCode
@Builder
public class BankIdIframeAuthenticationResult {
    private final ResponseFromProxy proxyResponseFromAuthFinishUrl;
    private final BankIdWebDriver webDriver;

    public List<Cookie> getCookies() {
        return webDriver.getCookies().stream()
                .map(
                        seleniumCookie -> {
                            BasicClientCookie clientCookie =
                                    new BasicClientCookie(
                                            seleniumCookie.getName(), seleniumCookie.getValue());
                            clientCookie.setDomain(seleniumCookie.getDomain());
                            clientCookie.setPath(seleniumCookie.getPath());
                            clientCookie.setExpiryDate(seleniumCookie.getExpiry());
                            clientCookie.setSecure(seleniumCookie.isSecure());
                            return clientCookie;
                        })
                .collect(Collectors.toList());
    }
}
