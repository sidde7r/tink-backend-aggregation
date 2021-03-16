package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.proxy.ResponseFromProxy;

@Getter
@EqualsAndHashCode
@Builder
public class BankIdIframeAuthenticationResult {
    private final ResponseFromProxy proxyResponseFromAuthFinishUrl;
    private final BankIdWebDriver webDriver;
}
