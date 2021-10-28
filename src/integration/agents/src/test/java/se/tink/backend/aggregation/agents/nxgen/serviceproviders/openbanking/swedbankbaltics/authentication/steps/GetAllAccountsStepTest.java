package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createErrorResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createHttpResponse;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetAllAccountsStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class GetAllAccountsStepTest {

    private GetAllAccountsStep getAllAccountsStep;

    private SwedbankBalticsApiClient apiClient;
    private StepDataStorage stepDataStorage;
    private PersistentStorage persistentStorage;

    @Before
    public void setUp() {
        stepDataStorage = mock(StepDataStorage.class);
        apiClient = mock(SwedbankBalticsApiClient.class);
        persistentStorage = mock(PersistentStorage.class);

        getAllAccountsStep = new GetAllAccountsStep(apiClient, stepDataStorage, persistentStorage);
    }

    @Test
    public void shouldExecuteNextStep() throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        FetchAccountResponse resp = mock(FetchAccountResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(resp);

        doNothing().when(stepDataStorage).putAccountResponse(resp);

        // when
        final AuthenticationStepResponse returnedResponse =
                getAllAccountsStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldThrowUnauthorizedException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        FetchAccountResponse resp = mock(FetchAccountResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(resp);

        doThrow(ThirdPartyAppError.AUTHENTICATION_ERROR.exception())
                .when(stepDataStorage)
                .putAccountResponse(resp);

        // when
        final Throwable thrown =
                catchThrowable(() -> getAllAccountsStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Cause: ThirdPartyAppError.AUTHENTICATION_ERROR");
    }

    @Test
    public void shouldThrowAccountBlockedException()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        FetchAccountResponse resp = mock(FetchAccountResponse.class);
        when(apiClient.fetchAccounts()).thenReturn(resp);

        HttpResponseException e = mock(HttpResponseException.class);
        createHttpResponse(e);

        doThrow(e).when(stepDataStorage).putAccountResponse(resp);
        GenericResponse errorResponse = createErrorResponse(e);

        doThrow(AuthorizationError.ACCOUNT_BLOCKED.exception()).when(errorResponse).isKycError();

        // when
        final Throwable thrown =
                catchThrowable(() -> getAllAccountsStep.execute(authenticationRequest));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(AuthorizationException.class)
                .hasMessage("Cause: AuthorizationError.ACCOUNT_BLOCKED");
    }
}
