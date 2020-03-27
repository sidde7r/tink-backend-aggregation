package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.InitScaEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.LoginInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.ApprovalResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.InitScaResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities.MetaDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.rpc.ResultEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CommerzbankPhotoTanAuthenticatorTest {

    private static final String OK_STATUS = "OK";
    private static final String NOT_OK_STATUS = "NOT_OK";

    private CommerzbankPhotoTanAuthenticator authenticator;
    private CommerzbankApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(CommerzbankApiClient.class);
        authenticator =
                new CommerzbankPhotoTanAuthenticator(
                        mock(PersistentStorage.class),
                        apiClient,
                        mock(SupplementalRequester.class),
                        100L,
                        3);
    }

    @Test
    public void authenticateShouldRetryApproveIfFirstTimeFail()
            throws AuthenticationException, AuthorizationException {
        // given
        String userName = "userName";
        String password = "password";
        String processContextId = "processContextId";

        when(apiClient.manualLogin(userName, password)).thenReturn(getLoginResponse());

        when(apiClient.initScaFlow()).thenReturn(getInitScaResponse(processContextId));

        when(apiClient.approveSca(processContextId))
                .thenReturn(getApprovalResponse(NOT_OK_STATUS))
                .thenReturn(getApprovalResponse(OK_STATUS));

        // when
        authenticator.authenticate(getCredentials(userName, password));

        // then
        verify(apiClient).manualLogin(userName, password);
        verify(apiClient).initScaFlow();
        verify(apiClient, times(2)).approveSca(processContextId);
    }

    @Test
    public void authenticateShouldFailIfRetryLimitReached() {
        // given
        String userName = "userName";
        String password = "password";
        String processContextId = "processContextId";

        when(apiClient.manualLogin(userName, password)).thenReturn(getLoginResponse());

        when(apiClient.initScaFlow()).thenReturn(getInitScaResponse(processContextId));

        when(apiClient.approveSca(processContextId))
                .thenReturn(getApprovalResponse(NOT_OK_STATUS))
                .thenReturn(getApprovalResponse(NOT_OK_STATUS))
                .thenReturn(getApprovalResponse(NOT_OK_STATUS));

        // when
        Throwable thrown =
                catchThrowable(
                        () -> authenticator.authenticate(getCredentials(userName, password)));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.INCORRECT_CHALLENGE_RESPONSE");
        verify(apiClient).manualLogin(userName, password);
        verify(apiClient).initScaFlow();
        verify(apiClient, times(3)).approveSca(processContextId);
    }

    private LoginResponse getLoginResponse() {
        return new LoginResponse(
                new ResultEntity<>(
                        new LoginInfoEntity(Values.CHALLENGE, Values.TAN_REQUESTED), null));
    }

    private InitScaResponse getInitScaResponse(String processContextId) {
        return new InitScaResponse(
                new ResultEntity<>(
                        new InitScaEntity(Collections.singletonList(ScaMethod.PUSH_PHOTO_TAN)),
                        null),
                null,
                new MetaDataEntity(processContextId));
    }

    private ApprovalResponse getApprovalResponse(String status) {
        return new ApprovalResponse(new ResultEntity<>(new StatusEntity(null, status, null), null));
    }

    private Credentials getCredentials(String userName, String password) {
        Credentials credentials = new Credentials();
        credentials.setFields(
                ImmutableMap.of(
                        Key.USERNAME.getFieldKey(),
                        userName,
                        Key.PASSWORD.getFieldKey(),
                        password));
        return credentials;
    }
}
