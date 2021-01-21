package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.await_user_confirmation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.N26ProcessStateData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.validate_consent.N26ValidateConsentStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentUserInteractionDefinitionStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.AgentNonEditableTextFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ThirdPartyAppCancelledError;

@RequiredArgsConstructor
public class N26AwaitUserConfirmationStep
        extends AgentUserInteractionDefinitionStep<AgentProceedNextStepAuthenticationRequest> {

    private static final String FIELD_ID_BASE = "consentConfirmationAwait";
    private static final String FIELD_LABEL_1 =
            "Please confirm the login in your N26 app and then click \"Submit\".";
    private static final String FIELD_LABEL_2 =
            "You haven't confirmed your login in the N26 app yet. Please confirm it and click \"Submit\".";
    private static final short MAX_RETRY = 3;

    private final ObjectMapper objectMapper;

    @Override
    protected AgentUserInteractionDefinitionResult defineInteraction(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {

        N26ProcessStateAccessor n26ProcessStateAccessor =
                new N26ProcessStateAccessor(
                        authenticationProcessRequest.getAuthenticationProcessState(), objectMapper);

        N26ProcessStateData n26ProcessStateData = n26ProcessStateAccessor.getN26ProcessStateData();
        n26ProcessStateData.incrementConsentRetryCounter();

        AgentAuthenticationProcessState agentAuthenticationProcessState =
                n26ProcessStateAccessor.storeN26ProcessStateData(n26ProcessStateData);

        return new AgentUserInteractionDefinitionResult(
                        AgentAuthenticationProcessStepIdentifier.of(
                                N26ValidateConsentStep.class.getSimpleName()),
                        authenticationProcessRequest.getAuthenticationPersistedData(),
                        agentAuthenticationProcessState)
                .requireField(
                        new AgentNonEditableTextFieldDefinition(
                                FIELD_ID_BASE + n26ProcessStateData.getConsentRetryCounter(),
                                n26ProcessStateData.getConsentRetryCounter() == 1
                                        ? FIELD_LABEL_1
                                        : FIELD_LABEL_2));
    }

    @Override
    public AgentAuthenticationResult execute(
            AgentProceedNextStepAuthenticationRequest authenticationProcessRequest) {
        N26ProcessStateAccessor n26ProcessStateAccessor =
                new N26ProcessStateAccessor(
                        authenticationProcessRequest.getAuthenticationProcessState(), objectMapper);

        N26ProcessStateData n26ProcessStateData = n26ProcessStateAccessor.getN26ProcessStateData();

        if (n26ProcessStateData.getConsentRetryCounter() == MAX_RETRY) {
            return new AgentFailedAuthenticationResult(
                    new ThirdPartyAppCancelledError(),
                    new AgentAuthenticationPersistedData(Collections.emptyMap()));
        }

        return super.execute(authenticationProcessRequest);
    }
}
