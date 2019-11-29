package se.tink.sa.agent.pt.ob.sibs.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import se.tink.sa.framework.rest.client.AbstractBusinessRestClient;

@Component
public class SibsAccountInformationClient extends AbstractBusinessRestClient {

    @Value("${bank.rest.service.consents.path}")
    private String consentsBasePath;

    @Value("${bank.rest.service.consents.path.status}")
    private String consentsStatusPath;

    //    public AccountsResponse fetchAccounts() {
    //        URL accounts = createUrl(SibsConstants.Urls.ACCOUNTS);
    //        return client.request(accounts)
    //                .queryParam(SibsConstants.QueryKeys.WITH_BALANCE, TRUE)
    //                .header(SibsConstants.HeaderKeys.CONSENT_ID, userState.getConsentId())
    //                .get(AccountsResponse.class);
    //    }

}
