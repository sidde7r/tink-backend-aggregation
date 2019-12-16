package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.rpc.TransactionsResponse;
import se.tink.sa.services.fetch.trans.FetchTransactionsResponse;

public class TransactionsResponseMapperTest
        extends AbstractResponseMapperTest<FetchTransactionsResponse> {

    @InjectMocks private TransactionsResponseMapper transactionsResponseMapper;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        super.init();
    }

    @Test
    public void transactionsResponseMapperTest() {
        TransactionsResponse source = new TransactionsResponse();
        FetchTransactionsResponse result = transactionsResponseMapper.map(source, mappingContext);
        verifyCommonMock(result);
    }
}
