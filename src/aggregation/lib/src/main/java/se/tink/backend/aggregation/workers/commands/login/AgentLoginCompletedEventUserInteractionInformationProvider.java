package se.tink.backend.aggregation.workers.commands.login;

import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;

public class AgentLoginCompletedEventUserInteractionInformationProvider {

    public static AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
            userInteractionInformation(
                    SupplementalInformationController supplementalInformationController) {
        if (!supplementalInformationController.isUsed()) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .AUTHENTICATED_WITHOUT_USER_INTERACTION;
        } else if (supplementalInformationController.getInteractionCounter() == 1) {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .ONE_STEP_USER_INTERACTION;
        } else {
            return AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                    .MULTIPLE_FACTOR_USER_INTERACTION;
        }
    }
}
