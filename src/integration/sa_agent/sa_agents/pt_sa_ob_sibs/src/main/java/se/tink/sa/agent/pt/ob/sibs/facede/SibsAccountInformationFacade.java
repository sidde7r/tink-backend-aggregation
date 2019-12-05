package se.tink.sa.agent.pt.ob.sibs.facede;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc.AccountsResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc.BalancesResponseMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.SibsAccountInformationClient;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.framework.facade.AccountInformationFacade;
import se.tink.sa.framework.mapper.MappingContext;
import se.tink.sa.services.fetch.account.ExactCurrencyAmount;
import se.tink.sa.services.fetch.account.FetchAccountsRequest;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.trans.FetchTransactionsRequest;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;

@Slf4j
@Component
public class SibsAccountInformationFacade implements AccountInformationFacade {

    private static final String BANK_CODE = "BANK_CODE";
    private static final String IS_MANUAL = "IS_MANUAL";

    @Autowired private SibsAccountInformationClient sibsAccountInformationClient;

    @Autowired private AccountsResponseMapper accountsResponseMapper;

    @Autowired private BalancesResponseMapper balancesResponseMapper;

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
                        .put(SibsMappingContextKeys.ACCOUNTS_BALANCES, balances);

        FetchAccountsResponse fetchAccountsResponse =
                accountsResponseMapper.mapToTransferModel(accountsResponse, mappingContext);

        return fetchAccountsResponse;
    }

    @Override
    public FetchTransactionsResponse fetchCheckingAccountsTransactions(
            FetchTransactionsRequest request) {
        FetchTransactionsResponse response = null;
        return response;
    }

    private ExactCurrencyAmount findAndMapBalance(FetchAccountsRequest request, String accountId) {
        CommonAccountSibsRequestRequest balancesRequest =
                buildCommonAccountSibsRequestRequest(request, accountId);
        BalancesResponse accountBalances =
                sibsAccountInformationClient.getAccountBalances(balancesRequest);
        ExactCurrencyAmount balance = balancesResponseMapper.mapToTransferModel(accountBalances);
        return balance;
    }

    private CommonSibsRequest getCommonRequest(FetchAccountsRequest request) {
        String bankCode = request.getExternalParametersOrDefault(BANK_CODE, null);
        String consentId = request.getSecurityInfo().getSecurityToken();

        return CommonSibsRequest.builder().bankCode(bankCode).consentId(consentId).build();
    }

    private CommonAccountSibsRequestRequest buildCommonAccountSibsRequestRequest(
            FetchAccountsRequest accountsRequest, String accountId) {

        String bankCode = accountsRequest.getExternalParametersOrDefault(BANK_CODE, null);
        String consentId = accountsRequest.getSecurityInfo().getSecurityToken();
        String psuInvolved = accountsRequest.getExternalParametersOrDefault(IS_MANUAL, "false");

        CommonAccountSibsRequestRequest request = new CommonAccountSibsRequestRequest();
        request.setAccountId(accountId);
        request.setIsPsuInvolved(BooleanUtils.toBoolean(psuInvolved));
        request.setConsentId(consentId);
        request.setBankCode(bankCode);
        return request;
    }
}
