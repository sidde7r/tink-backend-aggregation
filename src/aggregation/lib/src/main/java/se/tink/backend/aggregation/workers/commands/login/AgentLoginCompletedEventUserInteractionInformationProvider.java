package se.tink.backend.aggregation.workers.commands.login;

import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.libraries.credentials.service.CredentialsRequest;
import src.libraries.interaction_counter.InteractionCounter;

public class AgentLoginCompletedEventUserInteractionInformationProvider {

    public static AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
            userInteractionInformation(
                    InteractionCounter supplementalInformationInteractionCounter,
                    CredentialsRequest credentialsRequest) {
        int interactions = supplementalInformationInteractionCounter.getNumberInteractions();
        boolean createOrUpdateRequest =
                credentialsRequest.isCreate() || credentialsRequest.isUpdate();
        if (wasNoInteractions(interactions, createOrUpdateRequest)) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .AUTHENTICATED_WITHOUT_USER_INTERACTION;
        } else if (wasOneInteraction(interactions, createOrUpdateRequest)) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .ONE_STEP_USER_INTERACTION;
        } else {
            // Let's assume that providing credentials + 2FA is Multiple factor user interaction
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .MULTIPLE_FACTOR_USER_INTERACTION;
        }
    }

    private static boolean wasNoInteractions(int interactions, boolean createOrUpdateRequest) {
        return interactions == 0 && !createOrUpdateRequest;
    }

    private static boolean wasOneInteraction(int interactions, boolean createOrUpdateRequest) {
        return (interactions == 1 && !createOrUpdateRequest)
                || (createOrUpdateRequest && interactions == 0);
    }
}
