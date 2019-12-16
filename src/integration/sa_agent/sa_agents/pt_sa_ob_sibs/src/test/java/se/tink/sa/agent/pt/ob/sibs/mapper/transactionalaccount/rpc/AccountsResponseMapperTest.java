package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.AccountsResponse;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.FetchAccountsResponse;
import se.tink.sa.services.fetch.account.TransactionalAccount;

public class AccountsResponseMapperTest extends AbstractResponseMapperTest<AccountsResponse> {

    @InjectMocks private AccountsResponseMapper accountsResponseMapper;

    @Mock private TransactionalAccountResponseMapper transactionalAccountResponseMapper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        super.init();
    }

    @Test
    public void transactionsResponseMapperTest() {
        AccountsResponse source = buildMockFetchAccountsResponse();
        Mockito.when(
                        transactionalAccountResponseMapper.map(
                                Mockito.any(AccountEntity.class), Mockito.eq(mappingContext)))
                .thenReturn(TransactionalAccount.newBuilder().build());

        FetchAccountsResponse result = accountsResponseMapper.map(source, mappingContext);

        TestCase.assertTrue(result != null);

        // verify business mappings
        ArgumentCaptor<AccountEntity> argumentCaptorAccount =
                ArgumentCaptor.forClass(AccountEntity.class);
        ArgumentCaptor<MappingContext> argumentCaptorMappingContextForAccounts =
                ArgumentCaptor.forClass(MappingContext.class);
        Mockito.verify(transactionalAccountResponseMapper, Mockito.times(5))
                .map(
                        argumentCaptorAccount.capture(),
                        argumentCaptorMappingContextForAccounts.capture());

        TestCase.assertEquals(5, result.getAccountList().size());

        TestCase.assertEquals(5, argumentCaptorAccount.getAllValues().size());
        TestCase.assertEquals(5, argumentCaptorMappingContextForAccounts.getAllValues().size());

        for (int i = 0; i < 5; i++) {
            TestCase.assertEquals(
                    mappingContext, argumentCaptorMappingContextForAccounts.getAllValues().get(i));
            TestCase.assertEquals(
                    source.getAccountList().get(i).getId(),
                    argumentCaptorAccount.getAllValues().get(i).getId());
        }
    }

    private AccountsResponse buildMockFetchAccountsResponse() {
        List<AccountEntity> accountList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            AccountEntity accountEntity = new AccountEntity();
            accountEntity.setId("" + i);
            accountList.add(accountEntity);
        }
        AccountsResponse resp = new AccountsResponse();
        resp.setAccountList(accountList);
        return resp;
    }
}
