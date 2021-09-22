package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.helpers.SwedbankBalticsHelper.createAuthenticationRequest;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsConstants.Steps;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CheckIfAccessTokenIsValidStepTest {

    private CheckIfAccessTokenIsValidStep checkIfAccessTokenIsValidStep;

    private PersistentStorage persistentStorage;
    private OAuth2Token token;

    @Before
    public void setUp() {
        token = mock(OAuth2Token.class);
        persistentStorage = mock(PersistentStorage.class);
        checkIfAccessTokenIsValidStep = new CheckIfAccessTokenIsValidStep(persistentStorage);
    }

    @Test
    public void shouldRefreshAccessTokenWhenTokenIsNotPresented()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldRefreshAccessTokenWhenTokenIsPresentedButNotValid()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();

        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));

        when(token.isValid()).thenReturn(false);

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isFalse();
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }

    @Test
    public void shouldGetAllConsentWhenTokenIsPresentedAndValid()
            throws AuthenticationException, AuthorizationException {

        // given
        final AuthenticationRequest authenticationRequest = createAuthenticationRequest();
        when(token.isValid()).thenReturn(true);
        when(persistentStorage.get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class))
                .thenReturn(Optional.of(token));

        // when
        final AuthenticationStepResponse returnedResponse =
                checkIfAccessTokenIsValidStep.execute(authenticationRequest);

        // then
        assertThat(returnedResponse.getNextStepId().isPresent()).isTrue();
        assertThat(returnedResponse.getNextStepId().get())
                .isEqualTo(Steps.GET_CONSENT_FOR_ALL_ACCOUNTS_STEP);
        assertThat(returnedResponse.isAuthenticationFinished()).isFalse();
        assertThat(returnedResponse.getSupplementInformationRequester().isPresent()).isFalse();
    }
}
