package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account.IdModuleMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.*;

public class TransactionalAccountResponseMapperTest {

    private static final String MOCK_ID = "MOCK_ID_VALUE";

    @InjectMocks private TransactionalAccountResponseMapper transactionalAccountResponseMapper;

    @Mock private TransactionalAccountTypeResponseMapper transactionalAccountTypeResponseMapper;

    @Mock private IdModuleMapper idModuleMapper;

    private MappingContext mappingContext;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void transactionalAccountResponseMapperTest() {
        Map<String, ExactCurrencyAmount> balances = new HashMap<>();
        ExactCurrencyAmount balance = ExactCurrencyAmount.newBuilder().build();
        balances.put(MOCK_ID, balance);

        mappingContext =
                MappingContext.newInstance()
                        .put(SibsMappingContextKeys.ACCOUNTS_BALANCES, balances);

        AccountEntity source = prepareAccountEntityMock();
        Mockito.when(idModuleMapper.map(Mockito.eq(source), Mockito.eq(mappingContext)))
                .thenReturn(IdModule.newBuilder().build());

        TransactionalAccount result =
                transactionalAccountResponseMapper.map(source, mappingContext);

        TestCase.assertTrue(result != null);
        TestCase.assertEquals(TransactionalAccountType.CHECKING, result.getType());
        TestCase.assertNotNull(result.getFlagsList());
        TestCase.assertEquals(1, result.getFlagsCount());
        TestCase.assertEquals(AccountFlag.PSD2_PAYMENT_ACCOUNT, result.getFlagsList().get(0));

        TestCase.assertNotNull(result.getBalanceModule());
        TestCase.assertEquals(MOCK_ID, result.getApiId());
    }

    private AccountEntity prepareAccountEntityMock() {
        AccountEntity source = new AccountEntity();
        source.setId(MOCK_ID);
        return source;
    }
}
