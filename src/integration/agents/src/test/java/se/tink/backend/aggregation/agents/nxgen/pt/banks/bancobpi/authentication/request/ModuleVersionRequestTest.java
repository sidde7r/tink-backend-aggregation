package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ModuleVersionRequestTest {

    private static final String HEADER_OS_VISITOR = "osVisitor";

    private ModuleVersionRequest objectUnderTest;
    private RequestBuilder requestBuilder;
    private TinkHttpClient tinkHttpClient;
    private BancoBpiAuthContext userState;

    @Before
    public void init() {
        objectUnderTest = null;
        userState = Mockito.mock(BancoBpiAuthContext.class);
        tinkHttpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
    }

    @Test
    public void executeShouldReturnModuleVersion() throws RequestException {
        // given
        final String versionToken = "YmoUqw7QhA7_gxgqkajUrQ";
        Mockito.when(requestBuilder.get(String.class))
                .thenReturn("{\"versionToken\": \"" + versionToken + "\"}");
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        String result = objectUnderTest.execute(requestBuilder);
        // then
        Assert.assertEquals(versionToken, result);
    }

    @Test(expected = RequestException.class)
    public void executeShouldThrowLoginExceptionWhenResponseHasUnexpectedFormat()
            throws RequestException {
        // given
        Mockito.when(requestBuilder.get(String.class)).thenReturn("{}");
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        String result = objectUnderTest.execute(requestBuilder);
        // then
        // exception should be thrown
    }

    @Test
    public void withSpecificHeadersShouldSetOsVisitorCookie() {
        // given
        final String osVisitor = "1234567890";
        Mockito.when(requestBuilder.cookie(HEADER_OS_VISITOR, osVisitor))
                .thenReturn(requestBuilder);
        Mockito.when(userState.getDeviceUUID()).thenReturn(osVisitor);
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        objectUnderTest.withHeaders(requestBuilder);
        // then
        Mockito.verify(requestBuilder).cookie(HEADER_OS_VISITOR, osVisitor);
    }

    @Test
    public void withHeadersShouldNotSetOsVisitorCookie() {
        // given
        Mockito.when(requestBuilder.cookie(Mockito.eq(HEADER_OS_VISITOR), Mockito.any()))
                .thenReturn(requestBuilder);
        Mockito.when(userState.getDeviceUUID()).thenReturn(null);
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        objectUnderTest.withHeaders(requestBuilder);
        // then
        Mockito.verify(requestBuilder, Mockito.never())
                .cookie(Mockito.eq(HEADER_OS_VISITOR), Mockito.any());
    }
}
