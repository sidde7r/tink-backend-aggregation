package se.tink.sa.agent.pt.ob.sibs.mapper.transactionalaccount.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
    public void init() {
        rc = RequestCommon.newBuilder().build();
        mappingContext =
                MappingContext.newInstance().put(SibsMappingContextKeys.REQUEST_COMMON, rc);

        ResponseCommon rsc = ResponseCommon.newBuilder().build();
        Mockito.when(requestToResponseCommonMapper.map(Mockito.eq(rc), Mockito.eq(mappingContext)))
                .thenReturn(rsc);
    }

    void verifyCommonMock(T response) {
        assertNotNull(response);
        ArgumentCaptor<RequestCommon> argumentCaptorRequestCommon =
                ArgumentCaptor.forClass(RequestCommon.class);
        ArgumentCaptor<MappingContext> argumentCaptorMappingContext =
                ArgumentCaptor.forClass(MappingContext.class);
        Mockito.verify(requestToResponseCommonMapper, Mockito.times(1))
                .map(argumentCaptorRequestCommon.capture(), argumentCaptorMappingContext.capture());
        assertEquals(rc, argumentCaptorRequestCommon.getValue());
        assertEquals(mappingContext, argumentCaptorMappingContext.getValue());
    }
}
