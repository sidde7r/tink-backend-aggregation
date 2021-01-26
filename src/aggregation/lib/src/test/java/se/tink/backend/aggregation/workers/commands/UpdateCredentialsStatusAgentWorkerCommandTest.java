package se.tink.backend.aggregation.workers.commands;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentMatcher;
import org.modelmapper.TypeMap;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.utils.mappers.CoreCredentialsMapper;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregation.aggregationcontroller.v1.rpc.UpdateCredentialsStatusRequest;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class UpdateCredentialsStatusAgentWorkerCommandTest {
    private UpdateCredentialsStatusAgentWorkerCommand command;
    private AgentWorkerCommandContext context;

    private ControllerWrapper controllerWrapper;
    private Credentials credentials;
    private Provider provider;
    private Predicate<AgentWorkerCommandContext> setStatusUpdatedPredicate;

    @Before
    public void setUp() {
        context = mock(AgentWorkerCommandContext.class, Answers.RETURNS_DEEP_STUBS);
        controllerWrapper = mock(ControllerWrapper.class, Answers.RETURNS_DEEP_STUBS);
        credentials = mock(Credentials.class, Answers.RETURNS_DEEP_STUBS);
        provider = mock(Provider.class, Answers.RETURNS_DEEP_STUBS);
        setStatusUpdatedPredicate = mock(Predicate.class, Answers.RETURNS_DEEP_STUBS);

        command =
                new UpdateCredentialsStatusAgentWorkerCommand(
                        controllerWrapper,
                        credentials,
                        provider,
                        context,
                        setStatusUpdatedPredicate);
    }

    @Test
    public void doExecuteShouldNotUpdateCredentialStatusWhenItIsSetToAuthenticating()
            throws Exception {
        // given
        given(credentials.getStatus()).willReturn(CredentialsStatus.AUTHENTICATING);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        verifyZeroInteractions(controllerWrapper);
        verifyZeroInteractions(provider);
        verifyZeroInteractions(context);
        // and
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
    }

    @Test
    public void doExecuteShouldUpdateCredentialsWithNoSensitiveInformation() throws Exception {
        // given
        given(credentials.getStatus()).willReturn(CredentialsStatus.CREATED);
        Credentials credentialsCopy = mock(Credentials.class);
        given(credentials.clone()).willReturn(credentialsCopy);
        // and
        TypeMap typeMap = setUpCoreCredentialsMapperInstance();
        // and
        se.tink.libraries.credentials.rpc.Credentials mappedCredentials =
                mock(se.tink.libraries.credentials.rpc.Credentials.class);
        given(typeMap.map(credentialsCopy)).willReturn(mappedCredentials);

        // when
        AgentWorkerCommandResult result = command.doExecute();

        // then
        verify(credentials).setStatus(CredentialsStatus.AUTHENTICATING);
        verify(credentialsCopy).clearSensitiveInformation(provider);
        // and
        assertThat(result).isEqualTo(AgentWorkerCommandResult.CONTINUE);
        // and
        verify(typeMap).map(credentialsCopy);
        // and
        verify(controllerWrapper)
                .updateCredentials(
                        argThat(new UpdateCredentialsStatusRequestMatcher(mappedCredentials)));
    }

    @Test
    public void doPostProcessShouldNotProcessIfPredicateFails() throws Exception {
        // given
        given(setStatusUpdatedPredicate.test(any())).willReturn(false);

        // when
        command.doPostProcess();

        // then
        verifyZeroInteractions(controllerWrapper);
        verifyZeroInteractions(provider);
        verifyZeroInteractions(context);
    }

    @Test
    public void doPostProcessShouldNotUpdateCredentialsWhenStausIsInFailedOperationStatuses()
            throws Exception {
        // given
        given(setStatusUpdatedPredicate.test(any())).willReturn(true);
        // and
        given(credentials.getStatus()).willReturn(CredentialsStatus.UNCHANGED);

        // when
        command.doPostProcess();

        // then
        verifyZeroInteractions(controllerWrapper);
        verifyZeroInteractions(provider);
        verifyZeroInteractions(context);
    }

    @Test
    public void doPostProcessShouldClearSensitiveInfoAndUpdateCredentials() throws Exception {
        // given
        given(setStatusUpdatedPredicate.test(any())).willReturn(true);
        // and
        given(credentials.getStatus()).willReturn(CredentialsStatus.UPDATING);
        // and
        Credentials credentialsCopy = mock(Credentials.class);
        given(credentials.clone()).willReturn(credentialsCopy);
        // and
        TypeMap typeMap = setUpCoreCredentialsMapperInstance();
        // and
        se.tink.libraries.credentials.rpc.Credentials mappedCredentials =
                mock(se.tink.libraries.credentials.rpc.Credentials.class);
        given(typeMap.map(credentialsCopy)).willReturn(mappedCredentials);

        // when
        command.doPostProcess();

        // then
        verify(credentials).setStatus(CredentialsStatus.UPDATED);
        verify(credentialsCopy).clearSensitiveInformation(provider);
        // and
        verify(typeMap).map(credentialsCopy);
        // and
        verify(controllerWrapper)
                .updateCredentials(
                        argThat(new UpdateCredentialsStatusRequestMatcher(mappedCredentials)));
    }

    private TypeMap setUpCoreCredentialsMapperInstance()
            throws NoSuchFieldException, IllegalAccessException {
        CoreCredentialsMapper mapper = mock(CoreCredentialsMapper.class);
        Field singleton = CoreCredentialsMapper.class.getDeclaredField("singleton");
        singleton.setAccessible(true);
        singleton.set(null, mapper);

        TypeMap typeMap = mock(TypeMap.class);
        Field fromAggregationMap =
                CoreCredentialsMapper.class.getDeclaredField("fromAggregationMap");
        fromAggregationMap.setAccessible(true);
        fromAggregationMap.set(mapper, typeMap);

        return typeMap;
    }

    private static class UpdateCredentialsStatusRequestMatcher
            implements ArgumentMatcher<UpdateCredentialsStatusRequest> {

        private final se.tink.libraries.credentials.rpc.Credentials credentials;

        UpdateCredentialsStatusRequestMatcher(
                se.tink.libraries.credentials.rpc.Credentials credentials) {
            this.credentials = credentials;
        }

        @Override
        public boolean matches(UpdateCredentialsStatusRequest argument) {
            return argument.getCredentials().equals(credentials);
        }
    }
}
