package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.entity.account.AmountEntityMapper;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AmountEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.BalanceEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.BalanceSingleEntity;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.BalancesResponse;
import se.tink.sa.common.mapper.MappingContext;

public class BalancesResponseMapperTest {

    @InjectMocks private BalancesResponseMapper balancesResponseMapper;

    @Mock private AmountEntityMapper amountEntityMapper;

    private MappingContext mappingContext;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        mappingContext = MappingContext.newInstance();
    }

    @Test
    public void balancesResponseMapperTest() {
        BalanceSingleEntity balanceSingleEntity = new BalanceSingleEntity();

        List<BalanceEntity> balances = new ArrayList<>();
        BalanceEntity be = Mockito.mock(BalanceEntity.class);
        Mockito.when(be.getInterimAvailable()).thenReturn(balanceSingleEntity);
        balances.add(be);

        BalancesResponse source = new BalancesResponse();
        AmountEntity amountEntity = new AmountEntity();
        balanceSingleEntity.setAmount(amountEntity);

        source.setBalances(balances);

        balancesResponseMapper.map(source, mappingContext);

        Mockito.verify(be, Mockito.times(1)).getInterimAvailable();

        ArgumentCaptor<AmountEntity> argumentCaptor = ArgumentCaptor.forClass(AmountEntity.class);
        ArgumentCaptor<MappingContext> argumentCaptorMappingContextForAccounts =
                ArgumentCaptor.forClass(MappingContext.class);
        Mockito.verify(amountEntityMapper, Mockito.times(1))
                .map(argumentCaptor.capture(), argumentCaptorMappingContextForAccounts.capture());

        assertEquals(amountEntity, argumentCaptor.getValue());
    }
}
