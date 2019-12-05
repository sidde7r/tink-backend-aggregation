package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.authentication.request;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiUserState;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class ModuleVersionRequestTest {

    private static final String HEADER_OS_VISITOR = "osVisitor";

    private ModuleVersionRequest objectUnderTest;
    private RequestBuilder requestBuilder;
    private TinkHttpClient tinkHttpClient;
    private BancoBpiUserState userState;

    @Before
    public void init() {
        objectUnderTest = null;
        userState = Mockito.mock(BancoBpiUserState.class);
        tinkHttpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
    }

    @Test
    public void executeShouldReturnModuleVersion() throws LoginException {
        // given
        final String versionToken = "YmoUqw7QhA7_gxgqkajUrQ";
        Mockito.when(requestBuilder.get(String.class))
                .thenReturn("{\"versionToken\": \"" + versionToken + "\"}");
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        String result = objectUnderTest.execute(requestBuilder, tinkHttpClient);
        // then
        Assert.assertEquals(versionToken, result);
    }

    @Test(expected = LoginException.class)
    public void executeShouldThrowLoginExceptionWhenResponseHasUnexpectedFormat()
            throws LoginException {
        // given
        Mockito.when(requestBuilder.get(String.class)).thenReturn("{}");
        objectUnderTest = new ModuleVersionRequest(userState);
        // when
        String result = objectUnderTest.execute(requestBuilder, tinkHttpClient);
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
        objectUnderTest.withHeaders(tinkHttpClient, requestBuilder);
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
        objectUnderTest.withHeaders(tinkHttpClient, requestBuilder);
        // then
        Mockito.verify(requestBuilder, Mockito.never())
                .cookie(Mockito.eq(HEADER_OS_VISITOR), Mockito.any());
    }
}
