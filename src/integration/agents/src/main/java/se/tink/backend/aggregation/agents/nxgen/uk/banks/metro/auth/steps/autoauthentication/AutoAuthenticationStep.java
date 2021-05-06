package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.autoauthentication;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.UserIdHeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.CapabilitiesEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.CollectionResultEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.CollectionResultEntity.MetadataRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.CollectorStateEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ContentEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ContentEntity.LocalEnrollments;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ContentEntity.Pin;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.DeviceDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.LocationEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.OperationDataEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.entity.tlc.device.ParametersEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.AuthorizationOperationResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.tlc.device.DeviceOperationRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.challange.authentication.AuthenticationChallengeStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@AllArgsConstructor
@Slf4j
public class AutoAuthenticationStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private static final String SESSION_ID = "session_id";

    private final MetroDataAccessorFactory metroDataAccessorFactory;

    private final AutoAuthenticationCall autoAuthenticationCall;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                metroDataAccessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                metroDataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();
        DeviceOperationRequest requestBody = buildRequestBody(authenticationData.getUserId());

        ExternalApiCallResult<AuthorizationOperationResponse> result =
                autoAuthenticationCall.execute(
                        new AutoAuthenticationParameters(
                                authenticationData.getDeviceId(),
                                authenticationData.getSigningKeyPair().getPrivate(),
                                requestBody),
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        return result.getResponse()
                .map(
                        response ->
                                nextStep(
                                        persistedDataAccessor,
                                        processStateAccessor,
                                        authenticationData,
                                        response))
                .orElseGet(
                        () ->
                                new AgentFailedAuthenticationResult(
                                        result.getAgentBankApiError().get(),
                                        persistedDataAccessor.storeAuthenticationData(
                                                authenticationData)));
    }

    private DeviceOperationRequest buildRequestBody(String userId) {
        OperationDataEntity requestEntity =
                OperationDataEntity.builder()
                        .collectionResult(
                                CollectionResultEntity.builder()
                                        .collectorState(
                                                ContentEntity.builder()
                                                        .deviceDetails(
                                                                DeviceDetailsEntity.getDefault())
                                                        .location(new LocationEntity(false))
                                                        .capabilities(
                                                                CapabilitiesEntity.getDefault())
                                                        .collectorState(
                                                                CollectorStateEntity.getDefault())
                                                        .installedPackages(new Object[0])
                                                        .localEnrollments(
                                                                LocalEnrollments.builder()
                                                                        .pin(
                                                                                new Pin(
                                                                                        "registered",
                                                                                        "validated"))
                                                                        .build())
                                                        .build())
                                        .metadata(MetadataRequest.createInstance())
                                        .build())
                        .parameters(
                                ParametersEntity.builder()
                                        .appVersion(GlobalConstants.APP_VERSION.getValue())
                                        .build())
                        .policyRequestId("login")
                        .build();
        return new DeviceOperationRequest(new UserIdHeaderEntity(userId), requestEntity);
    }

    private AgentAuthenticationResult nextStep(
            MetroPersistedDataAccessor persistedDataAccessor,
            MetroProcessStateAccessor processStateAccessor,
            MetroAuthenticationData authenticationData,
            AuthorizationOperationResponse res) {
        MetroProcessState processState =
                processStateAccessor
                        .getProcessState()
                        .setAssertionId(res.getAssertionId())
                        .setChallenge(res.getChallenge())
                        .setSessionId(res.getHeader(SESSION_ID));
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        AuthenticationChallengeStep.class.getSimpleName()),
                processStateAccessor.storeProcessState(processState),
                persistedDataAccessor.storeAuthenticationData(authenticationData));
    }
}
