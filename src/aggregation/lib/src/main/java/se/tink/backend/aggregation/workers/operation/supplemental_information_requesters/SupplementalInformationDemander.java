package se.tink.backend.aggregation.workers.operation.supplemental_information_requesters;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentEventListener;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.aggregationcontroller.ControllerWrapper;
import se.tink.backend.aggregationcontroller.v1.rpc.credentialsservice.UpdateCredentialsSupplementalInformationRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import src.libraries.interaction_counter.InteractionCounter;

public class SupplementalInformationDemander implements SupplementalInformationRequester {

    private static final Logger logger =
            LoggerFactory.getLogger(SupplementalInformationDemander.class);
    private static final MetricId SUPPLEMENTAL_INFO_AND_USER_STATE =
            MetricId.newId("aggregation_supplemental_info_user_state");
    private static final String AGENT = "agent";

    private final InteractionCounter supplementalInteractionCounter;
    protected final MetricRegistry metricRegistry;
    protected final CredentialsRequest request;
    private final List<AgentEventListener> eventListeners;
    private final String refreshId;
    private final ControllerWrapper controllerWrapper;

    public SupplementalInformationDemander(
            InteractionCounter supplementalInteractionCounter,
            MetricRegistry metricRegistry,
            CredentialsRequest request,
            List<AgentEventListener> eventListeners,
            String refreshId,
            ControllerWrapper controllerWrapper) {
        this.supplementalInteractionCounter = supplementalInteractionCounter;
        this.metricRegistry = metricRegistry;
        this.request = request;
        this.eventListeners = eventListeners;
        this.refreshId = refreshId;
        this.controllerWrapper = controllerWrapper;
    }

    @Override
    public void requestSupplementalInformation(String mfaId, Credentials credentials) {
        supplementalInteractionCounter.inc();

        metricRegistry
                .meter(
                        SUPPLEMENTAL_INFO_AND_USER_STATE
                                .label(AGENT, request.getProvider().getClassName())
                                .label("operation", request.getType().toString())
                                .label(
                                        "userAvailableForInteraction",
                                        request.getUserAvailability()
                                                .isUserAvailableForInteraction())
                                // irrelevant, added just for debugging
                                .label("userPresent", request.getUserAvailability().isUserPresent())
                                // redundant, added just for debugging
                                .label("manual", request.isManual()))
                .inc();

        if (!request.getUserAvailability().isUserAvailableForInteraction()) {
            logger.error(
                    "Supplemental Information requested when user is not available for interaction. SesionHandler and/or AuthenticationController needs to be fixed.");
            throw SessionError.SESSION_EXPIRED.exception(
                    "Cannot start SCA when user is not available for interaction!");
        }

        // Execute any event-listeners; this tells the signable operation to update status
        for (AgentEventListener eventListener : eventListeners) {
            eventListener.onUpdateCredentialsStatus();
        }

        UpdateCredentialsSupplementalInformationRequest suppInfoRequest =
                new UpdateCredentialsSupplementalInformationRequest();
        suppInfoRequest.setMfaId(mfaId);
        suppInfoRequest.setCredentialsId(credentials.getId());
        suppInfoRequest.setStatus(credentials.getStatus());
        suppInfoRequest.setSupplementalInformation(credentials.getSupplementalInformation());
        suppInfoRequest.setUserId(credentials.getUserId());
        suppInfoRequest.setRequestType(request.getType());
        suppInfoRequest.setOperationId(request.getOperationId());
        suppInfoRequest.setManual(request.isManual());
        suppInfoRequest.setRefreshId(refreshId);

        controllerWrapper.updateSupplementalInformation(suppInfoRequest);
    }
}
