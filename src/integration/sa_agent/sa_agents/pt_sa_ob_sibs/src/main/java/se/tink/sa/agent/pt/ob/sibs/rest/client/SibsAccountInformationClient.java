package se.tink.sa.agent.pt.ob.sibs.rest.client;

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
import se.tink.sa.framework.rest.client.RequestUrlBuilder;

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

        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(accountsPath)
                        .queryParam(SibsConstants.QueryKeys.WITH_BALANCE, TRUE)
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ASPSP_CDE, request.getBankCode());

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());

        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<AccountsResponse> response =
                restTemplate.exchange(
                        builder.build(), HttpMethod.GET, entity, AccountsResponse.class);

        return response.getBody();
    }

    public BalancesResponse getAccountBalances(CommonAccountSibsRequestRequest request) {

        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(accountBalancesPath)
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ASPSP_CDE, request.getBankCode())
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ACCOUNT_ID, request.getAccountId())
                        .queryParam(
                                SibsConstants.QueryKeys.PSU_INVOLVED,
                                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<BalancesResponse> response =
                restTemplate.exchange(
                        builder.build(), HttpMethod.GET, entity, BalancesResponse.class);

        return response.getBody();
    }

    public TransactionsResponse getAccountTransactions(
            CommonAccountTransactionsSibsRequest request) {

        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(accountTransactionsPath)
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ASPSP_CDE, request.getBankCode())
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ACCOUNT_ID, request.getAccountId())
                        .queryParam(SibsConstants.QueryKeys.WITH_BALANCE, TRUE)
                        .queryParam(
                                SibsConstants.QueryKeys.BOOKING_STATUS,
                                SibsConstants.QueryValues.BOTH)
                        .queryParam(
                                SibsConstants.QueryKeys.DATE_FROM,
                                request.getDateFromTransactionFetch())
                        .queryParam(
                                SibsConstants.QueryKeys.PSU_INVOLVED,
                                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<TransactionsResponse> response =
                restTemplate.exchange(
                        builder.build(), HttpMethod.GET, entity, TransactionsResponse.class);

        return response.getBody();
    }

    public TransactionsResponse getTransactionsForKey(
            CommonAccountTransactionsSibsRequest request) {
        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(request.getNextPageUri())
                        .pathVariable(
                                SibsConstants.PathParameterKeys.ASPSP_CDE, request.getBankCode())
                        .queryParam(
                                SibsConstants.QueryKeys.PSU_INVOLVED,
                                BooleanUtils.toStringTrueFalse(request.getIsPsuInvolved()));

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.CONSENT_ID, request.getConsentId());
        HttpEntity entity = new HttpEntity(headers);

        ResponseEntity<TransactionsResponse> response =
                restTemplate.exchange(
                        builder.build(), HttpMethod.GET, entity, TransactionsResponse.class);

        return response.getBody();
    }
}
