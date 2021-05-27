package se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.authenticator;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.http.cookie.Cookie;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnbbankid.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticationResult;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdIframeAuthenticator;

@RequiredArgsConstructor
public class DnbAuthenticator implements BankIdIframeAuthenticator {

    private final DnbApiClient apiClient;

    @Override
    public String getSubstringOfUrlIndicatingAuthenticationFinish() {
        return DnbConstants.Url.FINISH_LOGIN;
    }

    @Override
    public void handleBankIdAuthenticationResult(
            BankIdIframeAuthenticationResult authenticationResult) {
        List<Cookie> webDriverCookies = authenticationResult.getCookies();
        apiClient.addCookies(webDriverCookies);
    }
}
