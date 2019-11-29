package se.tink.sa.agent.pt.ob.sibs.rest.client;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.AbstractSibsRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;

@Component
public class SibsAccountInformationClient extends AbstractSibsRestClient {

    private static final String TRUE = "true";

    @Value("${bank.rest.service.accounts.path}")
    private String accountsPath;

    @Value("${bank.rest.service.accounts.path.id}")
    private String accountDetailsPath;

    @Value("${bank.rest.service.accounts.path.balances}")
    private String accountBalancesPath;

    @Value("${bank.rest.service.accounts.path.transactions}")
    private String accountTransactionsPath;

    public AccountsResponse fetchAccounts(CommonSibsRequestRequest request) {
        String url = prepareUrl(baseUrl, accountsPath);

        Map<String, String> params = sibsParamsSet(request.getBankCode());
        params.put(SibsConstants.QueryKeys.WITH_BALANCE, TRUE);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<AccountsResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, AccountsResponse.class, params);

        return response.getBody();
    }
}
