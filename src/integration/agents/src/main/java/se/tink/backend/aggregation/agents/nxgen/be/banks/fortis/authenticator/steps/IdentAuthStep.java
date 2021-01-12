package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities.SignatureEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessState;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.InitializeLoginResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginDescriptionAgentField;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

@RequiredArgsConstructor
public class IdentAuthStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final AgentPlatformFortisApiClient apiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        FortisAuthData fortisAuthData = authDataAccessor.get();

        FortisProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        FortisProcessState fortisProcessState = processStateAccessor.get();

        InitializeLoginResponse initializeLoginResponse =
                apiClient.initializeLoginTransaction(
                        fortisAuthData.getUsername(), fortisAuthData.getClientNumber());

        if (initializeLoginResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(initializeLoginResponse, true),
                    request.getAuthenticationPersistedData());
        }

        fortisProcessState.setLoginSessionId(initializeLoginResponse.getLoginSessionId());
        fortisProcessState.setCardFrameId(
                initializeLoginResponse.getValue().getCardInfo().getCardFrameId());

        String challenge = findChallenge(initializeLoginResponse);

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        CheckLoginResultStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                processStateAccessor.store(fortisProcessState),
                new CardReaderLoginDescriptionAgentField(challenge),
                new CardReaderLoginInputAgentField());
    }

    private String findChallenge(InitializeLoginResponse response) {
        if (response.getValue() == null
                || response.getValue().getUcr() == null
                || response.getValue().getUcr().getSignature() == null) {
            throw new IllegalArgumentException("No signature in response");
        }

        SignatureEntity signatureEntity = response.getValue().getUcr().getSignature();

        if (signatureEntity.getChallenges() != null) {
            return signatureEntity.getChallenges().stream()
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No Challenge received"));
        }

        throw new IllegalArgumentException("No signature in response");
    }
}
