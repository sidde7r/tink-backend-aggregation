package se.tink.backend.aggregation.workers.commands;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import agents_platform_agents_framework.org.springframework.test.util.ReflectionTestUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.libraries.credentials.service.CredentialsRequest;

@RunWith(MockitoJUnitRunner.class)
public class DebugAgentWorkerCommandTest {

    private static final String LOG_CONTENT = "content: dummyString";

    private DebugAgentWorkerCommand underTest;

    private Credentials credentials;

    @Before
    public void init() throws Exception {
        AgentWorkerCommandContext context = mock(AgentWorkerCommandContext.class);
        underTest = mock(DebugAgentWorkerCommand.class);

        LogMasker logMasker = mock(LogMasker.class);

        when(logMasker.mask(any()))
                .thenAnswer((Answer<String>) invocation -> invocation.getArgument(0));

        credentials = mock(Credentials.class);
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        ReflectionTestUtils.setField(underTest, "context", context);
        ReflectionTestUtils.setField(underTest, "logMasker", logMasker);

        when(context.getRequest()).thenReturn(credentialsRequest);
        when(credentialsRequest.getProvider()).thenReturn(getProvider());
    }

    @Test
    public void shouldNotReplaceContentWithEmptyValue() {
        // given
        String expected = "content: dummyString";

        // when
        when(credentials.getField("psu-company-id")).thenReturn("");
        String result =
                ReflectionTestUtils.invokeMethod(
                        underTest, "maskSensitiveOutputLog", LOG_CONTENT, credentials);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void shouldNotReplaceContentWithNullValue() {
        // given
        String expected = "content: dummyString";

        // when
        when(credentials.getField("psu-company-id")).thenReturn(null);
        String result =
                ReflectionTestUtils.invokeMethod(
                        underTest, "maskSensitiveOutputLog", LOG_CONTENT, credentials);

        // then
        assertEquals(expected, result);
    }

    @Test
    public void shouldReplaceContentWhenValueProvided() {
        // given
        String expected = "content: ***psu-company-id***";

        // when
        when(credentials.getField("psu-company-id")).thenReturn("dummyString");
        String result =
                ReflectionTestUtils.invokeMethod(
                        underTest, "maskSensitiveOutputLog", LOG_CONTENT, credentials);

        // then
        assertEquals(expected, result);
    }

    private Provider getProvider() {
        Provider provider = new Provider();
        List<Field> stringList = new ArrayList<>();
        stringList.add(Field.builder().name("psu-company-id").description("sth").build());
        provider.setFields(stringList);
        return provider;
    }
}
