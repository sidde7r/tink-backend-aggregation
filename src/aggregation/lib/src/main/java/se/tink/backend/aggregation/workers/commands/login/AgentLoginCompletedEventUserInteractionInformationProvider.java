package se.tink.backend.aggregation.workers.commands.login;

import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import src.libraries.interaction_counter.InteractionCounter;

public class AgentLoginCompletedEventUserInteractionInformationProvider {

    public static AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
            userInteractionInformation(
                    InteractionCounter supplementalInformationInteractionCounter) {
        int interactions = supplementalInformationInteractionCounter.getNumberInteractions();
        if (interactions == 0) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .AUTHENTICATED_WITHOUT_USER_INTERACTION;
        } else if (interactions == 1) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .ONE_STEP_USER_INTERACTION;
        } else {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .MULTIPLE_FACTOR_USER_INTERACTION;
        }
    }
}
