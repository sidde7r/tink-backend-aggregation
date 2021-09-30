package se.tink.backend.aggregation.workers.commands.login;

import lombok.experimental.UtilityClass;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.libraries.interaction_counter.InteractionCounter;

@UtilityClass
public class AgentLoginCompletedEventUserInteractionInformationProvider {

    public static AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
            userInteractionInformation(
                    InteractionCounter supplementalInformationInteractionCounter,
                    CredentialsRequest credentialsRequest) {
        int interactions = supplementalInformationInteractionCounter.getNumberInteractions();
        boolean createOrUpdateOrForceAuthenticationRequest =
                isCreateUpdateOrForceAuthenticationRequest(credentialsRequest);
        if (wasNoInteractions(interactions, createOrUpdateOrForceAuthenticationRequest)) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .AUTHENTICATED_WITHOUT_USER_INTERACTION;
        } else if (wasOneInteraction(interactions, createOrUpdateOrForceAuthenticationRequest)) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .ONE_STEP_USER_INTERACTION;
        } else {
            // Let's assume that providing credentials + 2FA is Multiple factor user interaction
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .MULTIPLE_FACTOR_USER_INTERACTION;
        }
    }

    private static boolean isCreateUpdateOrForceAuthenticationRequest(
            CredentialsRequest credentialsRequest) {
        return credentialsRequest.isCreate()
                || credentialsRequest.isUpdate()
                || wasAuthenticationForced(credentialsRequest);
    }

    private static boolean wasAuthenticationForced(CredentialsRequest credentialsRequest) {
        return credentialsRequest.isForceAuthenticate()
                && credentialsRequest.getUserAvailability().isUserAvailableForInteraction();
    }

    private static boolean wasNoInteractions(
            int interactions, boolean createOrUpdateOrForceAuthenticationRequest) {
        return interactions == 0 && !createOrUpdateOrForceAuthenticationRequest;
    }

    private static boolean wasOneInteraction(
            int interactions, boolean createOrUpdateOrForceAuthenticationRequest) {
        return (interactions == 1 && !createOrUpdateOrForceAuthenticationRequest)
                || (createOrUpdateOrForceAuthenticationRequest && interactions == 0);
    }
}
