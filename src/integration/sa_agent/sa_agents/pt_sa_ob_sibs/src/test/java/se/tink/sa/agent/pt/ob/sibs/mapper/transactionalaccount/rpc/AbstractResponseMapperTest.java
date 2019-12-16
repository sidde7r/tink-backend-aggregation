package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import junit.framework.TestCase;
import org.junit.Before;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import se.tink.sa.agent.pt.ob.sibs.SibsMappingContextKeys;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.framework.common.mapper.RequestToResponseCommonMapper;
import se.tink.sa.services.common.RequestCommon;
import se.tink.sa.services.common.ResponseCommon;

public abstract class AbstractResponseMapperTest<T> {

    protected RequestCommon rc;
    protected MappingContext mappingContext;

    @Mock protected RequestToResponseCommonMapper requestToResponseCommonMapper;

    @Before
    protected void init() {
        rc = RequestCommon.newBuilder().build();
        mappingContext =
                MappingContext.newInstance().put(SibsMappingContextKeys.REQUEST_COMMON, rc);

        ResponseCommon rsc = ResponseCommon.newBuilder().build();
        Mockito.when(requestToResponseCommonMapper.map(Mockito.eq(rc), Mockito.eq(mappingContext)))
                .thenReturn(rsc);
    }

    protected void verifyCommonMock(T response) {
        TestCase.assertTrue(response != null);
        ArgumentCaptor<RequestCommon> argumentCaptorRequestCommon =
                ArgumentCaptor.forClass(RequestCommon.class);
        ArgumentCaptor<MappingContext> argumentCaptorMappingContext =
                ArgumentCaptor.forClass(MappingContext.class);
        Mockito.verify(requestToResponseCommonMapper, Mockito.times(1))
                .map(argumentCaptorRequestCommon.capture(), argumentCaptorMappingContext.capture());
        TestCase.assertEquals(rc, argumentCaptorRequestCommon.getValue());
        TestCase.assertEquals(mappingContext, argumentCaptorMappingContext.getValue());
    }
}
