package se.tink.backend.aggregation.workers.commands.login;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.UserAvailability;
import src.libraries.interaction_counter.InteractionCounter;

@RunWith(JUnitParamsRunner.class)
public class AgentLoginCompletedEventUserInteractionInformationProviderTest {

    private InteractionCounter interactionCounter;
    private CredentialsRequest credentialsRequest;

    @Before
    public void init() {
        this.interactionCounter = mock(InteractionCounter.class);
        this.credentialsRequest = mock(CredentialsRequest.class);
    }

    @Test
    public void shouldReturnAuthenticatedWithoutUserInteractionWhenThereWasNoInteraction() {
        // given
        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(false);

        // when
        UserInteractionInformation userInteractionInformation =
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(interactionCounter, credentialsRequest);

        // then
        assertThat(userInteractionInformation)
                .isEqualTo(UserInteractionInformation.AUTHENTICATED_WITHOUT_USER_INTERACTION);
    }

    @Test
    public void
            shouldReturnAuthenticatedWithoutUserInteractionWhenThereWasForceAuthenticationButUserNotAvailable() {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(false);

        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(true);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        UserInteractionInformation userInteractionInformation =
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(interactionCounter, credentialsRequest);

        // then
        assertThat(userInteractionInformation)
                .isEqualTo(UserInteractionInformation.AUTHENTICATED_WITHOUT_USER_INTERACTION);
    }

    @Test
    public void shouldReturnOneStepInteractionWhenThereWasInteraction() {
        // given
        when(interactionCounter.getNumberInteractions()).thenReturn(1);
        when(credentialsRequest.isCreate()).thenReturn(false);
        when(credentialsRequest.isUpdate()).thenReturn(false);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(false);

        // when
        UserInteractionInformation userInteractionInformation =
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(interactionCounter, credentialsRequest);

        // then
        assertThat(userInteractionInformation)
                .isEqualTo(UserInteractionInformation.ONE_STEP_USER_INTERACTION);
    }

    @Test
    @Parameters({"true, false, false", "false, true, false", "false, false, true"})
    public void shouldReturnOneStepInteractionWhenThereWasCreateOrUpdateOrForceAuthentication(
            Boolean isCreate, Boolean isUpdate, Boolean isForceAuthentication) {
        // given
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(true);

        when(interactionCounter.getNumberInteractions()).thenReturn(0);
        when(credentialsRequest.isCreate()).thenReturn(isCreate);
        when(credentialsRequest.isUpdate()).thenReturn(isUpdate);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(isForceAuthentication);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        UserInteractionInformation userInteractionInformation =
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(interactionCounter, credentialsRequest);

        // then
        assertThat(userInteractionInformation)
                .isEqualTo(UserInteractionInformation.ONE_STEP_USER_INTERACTION);
    }

    @Test
    @Parameters({"true, false, false", "false, true, false", "false, false, true"})
    public void
            shouldReturnMultipleFactorInteractionWhenThereWasCreateOrUpdateOrForceAuthenticationAndInteractionsWereMade(
                    Boolean isCreate, Boolean isUpdate, Boolean isForceAuthentication) {
        // given
        InteractionCounter interactionCounter = mock(InteractionCounter.class);
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserAvailableForInteraction(true);

        when(interactionCounter.getNumberInteractions()).thenReturn(1);
        when(credentialsRequest.isCreate()).thenReturn(isCreate);
        when(credentialsRequest.isUpdate()).thenReturn(isUpdate);
        when(credentialsRequest.isForceAuthenticate()).thenReturn(isForceAuthentication);
        when(credentialsRequest.getUserAvailability()).thenReturn(userAvailability);

        // when
        UserInteractionInformation userInteractionInformation =
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(interactionCounter, credentialsRequest);

        // then
        assertThat(userInteractionInformation)
                .isEqualTo(UserInteractionInformation.MULTIPLE_FACTOR_USER_INTERACTION);
    }
}
