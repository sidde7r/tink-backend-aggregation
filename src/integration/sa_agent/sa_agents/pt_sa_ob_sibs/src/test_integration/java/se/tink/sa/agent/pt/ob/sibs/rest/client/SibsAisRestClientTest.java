package se.tink.sa.agent.pt.ob.sibs.rest.client;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountSibsRequestRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonAccountTransactionsSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.CommonSibsRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;
import se.tink.sa.framework.tools.JsonUtils;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@TestPropertySource(
        locations = {"classpath:application.properties", "classpath:secrets.properties"})
@ContextConfiguration(classes = {SibsStandaloneAgent.class})
public class SibsAisRestClientTest extends AbstractRestClientTest {

    @Autowired private SibsAccountInformationClient sibsAccountInformationClient;
    private boolean isManual = true;

    @Test
    public void testAis() {
        String state = generateNewTestState();
        String bankCode = "BCPPT";
        String consentId = getVerifiedConsentId(state, bankCode);
        assertNotNull(consentId);
        AccountsResponse accountsResponse =
                sibsAccountInformationClient.fetchAccounts(getCommonRequest(consentId, bankCode));
        assertNotNull(accountsResponse);
        for (AccountEntity acc : accountsResponse.getAccountList()) {
            CommonAccountSibsRequestRequest balancesRequest =
                    buildCommonAccountSibsRequestRequest(consentId, bankCode, acc.getId());
            BalancesResponse accountBalances =
                    sibsAccountInformationClient.getAccountBalances(balancesRequest);
            assertNotNull(accountBalances);
            assertTrue(CollectionUtils.isNotEmpty(accountBalances.getBalances()));

            CommonAccountTransactionsSibsRequest firstTransactionsRequest =
                    buildFirstTransactionsRequest(consentId, bankCode, acc.getId());
            TransactionsResponse response =
                    sibsAccountInformationClient.getAccountTransactions(firstTransactionsRequest);
            assertNotNull(response);
            while (response.canFetchMore().orElse(Boolean.FALSE)) {
                CommonAccountTransactionsSibsRequest nextTransactionsRequest =
                        buildNextTransactionsRequest(consentId, response.nextKey());
                response =
                        sibsAccountInformationClient.getTransactionsForKey(nextTransactionsRequest);
            }
        }

        log.info("AccountsResponse --> {}", JsonUtils.writeAsJson(accountsResponse));
    }

    private CommonSibsRequest getCommonRequest(String consentId, String bankCode) {
        return CommonSibsRequest.builder().bankCode(bankCode).consentId(consentId).build();
    }

    private CommonAccountTransactionsSibsRequest buildNextTransactionsRequest(
            String consentId, String nextLink) {
        CommonAccountTransactionsSibsRequest request = new CommonAccountTransactionsSibsRequest();
        request.setIsPsuInvolved(true);
        request.setConsentId(consentId);
        request.setNextPageUri(nextLink);
        return request;
    }

    private CommonAccountTransactionsSibsRequest buildFirstTransactionsRequest(
            String consentId, String bankCode, String accId) {
        CommonAccountTransactionsSibsRequest request = new CommonAccountTransactionsSibsRequest();
        request.setDateFromTransactionFetch("1970-01-01");
        request.setAccountId(accId);
        request.setIsPsuInvolved(true);
        request.setBankCode(bankCode);
        request.setConsentId(consentId);
        return request;
    }

    private CommonAccountSibsRequestRequest buildCommonAccountSibsRequestRequest(
            String consentId, String bankCode, String accountId) {
        CommonAccountSibsRequestRequest request = new CommonAccountSibsRequestRequest();
        request.setAccountId(accountId);
        request.setIsPsuInvolved(isManual);
        request.setConsentId(consentId);
        request.setBankCode(bankCode);
        return request;
    }
}
