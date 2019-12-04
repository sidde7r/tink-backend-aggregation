package se.tink.sa.agent.pt.ob.sibs.rest.client;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.AbstractSibsRestClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountTransactionsSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;

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

    public AccountsResponse fetchAccounts(CommonSibsRequest request) {
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

    public BalancesResponse getAccountBalances(CommonAccountSibsRequestRequest request) {

        String url = prepareUrl(baseUrl, accountBalancesPath);

        Map<String, String> params = sibsParamsSet(request.getBankCode());
        params.put(
                SibsConstants.QueryKeys.PSU_INVOLVED,
                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));
        params.put(SibsConstants.PathParameterKeys.ACCOUNT_ID, request.getAccountId());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<BalancesResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, BalancesResponse.class, params);

        return response.getBody();
    }

    public TransactionsResponse getAccountTransactions(
            CommonAccountTransactionsSibsRequest request) {

        String url = prepareUrl(baseUrl, accountTransactionsPath);

        Map<String, String> params = sibsParamsSet(request.getBankCode());
        params.put(
                SibsConstants.QueryKeys.PSU_INVOLVED,
                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));
        params.put(SibsConstants.PathParameterKeys.ACCOUNT_ID, request.getAccountId());
        params.put(SibsConstants.QueryKeys.WITH_BALANCE, TRUE);
        params.put(SibsConstants.QueryKeys.BOOKING_STATUS, SibsConstants.QueryValues.BOTH);
        params.put(SibsConstants.QueryKeys.DATE_FROM, request.getDateFromTransactionFetch());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<TransactionsResponse> response =
                restTemplate.exchange(
                        url, HttpMethod.GET, entity, TransactionsResponse.class, params);

        return response.getBody();
    }

    public TransactionsResponse getTransactionsForKey(
            CommonAccountTransactionsSibsRequest request) {
        String url = prepareUrl(baseUrl, request.getNextPageUri());

        Map<String, String> params = new HashMap<>();
        params.put(
                SibsConstants.QueryKeys.PSU_INVOLVED,
                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<TransactionsResponse> response =
                restTemplate.exchange(
                        url, HttpMethod.GET, entity, TransactionsResponse.class, params);

        return response.getBody();
    }
}
