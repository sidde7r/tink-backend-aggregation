package se.tink.sa.agent.pt.ob.sibs.facede;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.mapper.SibsApspCodeMappinngs;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc.AccountsResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc.BalancesResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc.TransactionsResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsAccountInformationClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountTransactionsSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.facade.AccountInformationFacade;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;

@Slf4j
@Component
public class SibsAccountInformationFacade implements AccountInformationFacade {

    private static final String BANK_CODE = "BANK_CODE";
    private static final String NEXT_TR_PAGE_LINK = "NEXT_TR_PAGE_LINK";
    private static final String FIRST_TRANSACTION_FETCH_DATE = "1970-01-01";

    @Autowired private SibsAccountInformationClient sibsAccountInformationClient;

    @Autowired private AccountsResponseMapper accountsResponseMapper;

    @Autowired private BalancesResponseMapper balancesResponseMapper;

    @Autowired private TransactionsResponseMapper transactionsResponseMapper;

    @Override
    public FetchAccountsResponse fetchCheckingAccounts(FetchAccountsRequest request) {
        FetchAccountsResponse response = null;

        CommonSibsRequest commonRequest = getCommonRequest(request);
        AccountsResponse accountsResponse =
                sibsAccountInformationClient.fetchAccounts(commonRequest);

        Map<String, ExactCurrencyAmount> balances = new HashMap<>();
        if (CollectionUtils.isNotEmpty(accountsResponse.getAccountList())) {
            accountsResponse
                    .getAccountList()
                    .forEach(
                            account -> {
                                ExactCurrencyAmount balance =
                                        findAndMapBalance(request, account.getId());
                                balances.put(account.getId(), balance);
                            });
        }

        MappingContext mappingContext =
                MappingContext.newInstance()
                        .put(SibsMappingContextKeys.ACCOUNTS_BALANCES, balances)
                        .put(SibsMappingContextKeys.REQUEST_COMMON, request.getRequestCommon());

        FetchAccountsResponse fetchAccountsResponse =
                accountsResponseMapper.map(accountsResponse, mappingContext);

        return fetchAccountsResponse;
    }

    @Override
    public FetchTransactionsResponse fetchCheckingAccountsTransactions(
            FetchTransactionsRequest request) {
        FetchTransactionsResponse response = null;
        TransactionsResponse transactionsResponse = null;
        String nextPageLink = request.getExternalParametersOrDefault(NEXT_TR_PAGE_LINK, null);

        if (StringUtils.isBlank(nextPageLink)) {
            CommonAccountTransactionsSibsRequest sibsRequest =
                    buildFirstTransactionsRequest(request);
            transactionsResponse = sibsAccountInformationClient.getAccountTransactions(sibsRequest);
        } else {
            CommonAccountTransactionsSibsRequest sibsRequest =
                    buildNextTransactionsRequest(request, nextPageLink);
            transactionsResponse = sibsAccountInformationClient.getTransactionsForKey(sibsRequest);
        }

        response = transactionsResponseMapper.map(transactionsResponse);

        return response;
    }

    private se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountTransactionsSibsRequest
            buildFirstTransactionsRequest(FetchTransactionsRequest request) {
        CommonAccountTransactionsSibsRequest sibsRequest =
                new CommonAccountTransactionsSibsRequest();
        sibsRequest.setDateFromTransactionFetch(FIRST_TRANSACTION_FETCH_DATE);
        sibsRequest.setAccountId(request.getAccountId());
        sibsRequest.setIsPsuInvolved(request.getRequestCommon().getManual());
        sibsRequest.setBankCode(
                SibsApspCodeMappinngs.findCodeByProviderName(
                        request.getRequestCommon().getProviderName()));
        sibsRequest.setConsentId(request.getRequestCommon().getSecurityInfo().getConsentId());
        return sibsRequest;
    }

    private CommonAccountTransactionsSibsRequest buildNextTransactionsRequest(
            FetchTransactionsRequest request, String nextLink) {
        CommonAccountTransactionsSibsRequest sibsRequest =
                new CommonAccountTransactionsSibsRequest();
        sibsRequest.setIsPsuInvolved(request.getRequestCommon().getManual());
        sibsRequest.setConsentId(request.getRequestCommon().getSecurityInfo().getConsentId());
        sibsRequest.setNextPageUri(nextLink);
        return sibsRequest;
    }

    private ExactCurrencyAmount findAndMapBalance(FetchAccountsRequest request, String accountId) {
        CommonAccountSibsRequestRequest balancesRequest =
                buildCommonAccountSibsRequestRequest(request, accountId);
        BalancesResponse accountBalances =
                sibsAccountInformationClient.getAccountBalances(balancesRequest);
        ExactCurrencyAmount balance = balancesResponseMapper.map(accountBalances);
        return balance;
    }

    private CommonSibsRequest getCommonRequest(FetchAccountsRequest request) {
        CommonSibsRequest sibsRequest = new CommonSibsRequest();
        sibsRequest.setBankCode(
                SibsApspCodeMappinngs.findCodeByProviderName(
                        request.getRequestCommon().getProviderName()));
        sibsRequest.setConsentId(request.getRequestCommon().getSecurityInfo().getConsentId());
        return sibsRequest;
    }

    private CommonAccountSibsRequestRequest buildCommonAccountSibsRequestRequest(
            FetchAccountsRequest request, String accountId) {
        CommonAccountSibsRequestRequest sibsRequest = new CommonAccountSibsRequestRequest();
        sibsRequest.setAccountId(accountId);
        sibsRequest.setIsPsuInvolved(request.getRequestCommon().getManual());
        sibsRequest.setConsentId(request.getRequestCommon().getSecurityInfo().getConsentId());
        sibsRequest.setBankCode(
                SibsApspCodeMappinngs.findCodeByProviderName(
                        request.getRequestCommon().getProviderName()));
        return sibsRequest;
    }
}
