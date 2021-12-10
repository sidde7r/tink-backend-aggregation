package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;
import se.tink.integration.webdriver.service.proxy.ProxyResponseMatcher;
import se.tink.integration.webdriver.service.proxy.ProxyResponseMatchers.ProxyResponseUrlSubstringMatcher;

@RequiredArgsConstructor
public class DnbAuthenticator implements BankIdIframeAuthenticator, AutoAuthenticator {

    private final DnbApiClient apiClient;

    @Override
    public ProxyResponseMatcher getMatcherForResponseThatIndicatesAuthenticationWasFinished() {
        return new ProxyResponseUrlSubstringMatcher(DnbConstants.Url.FINISH_LOGIN);
    }

    @Override
    public void handleBankIdAuthenticationResult(
            BankIdIframeAuthenticationResult authenticationResult) {
        List<Cookie> webDriverCookies = authenticationResult.getCookies();
        apiClient.addCookies(webDriverCookies);
    }

    @Override
    public void autoAuthenticate() {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
