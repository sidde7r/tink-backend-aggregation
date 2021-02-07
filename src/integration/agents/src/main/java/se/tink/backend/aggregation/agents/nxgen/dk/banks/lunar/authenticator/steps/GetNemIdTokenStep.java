package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.AuthenticationExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RequiredArgsConstructor
@Slf4j
public class GetNemIdTokenStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AuthenticationApiClient apiClient;
    private final NemIdIframeAttributes iframeAttributes;
    private final RandomValueGenerator randomValueGenerator;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        LunarAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());
        LunarAuthData authData = authDataAccessor.get();

        LunarProcessStateAccessor processStateAccessor =
                dataAccessorFactory.createProcessStateAccessor(
                        request.getAuthenticationProcessState());
        LunarProcessState processState = processStateAccessor.get();

        String deviceId = randomValueGenerator.getUUID().toString();

        NemIdParamsResponse nemIdParamsResponse;
        try {
            nemIdParamsResponse = apiClient.getNemIdParameters(deviceId);
        } catch (ResponseStatusException e) {
            log.error("Could not get NemId Iframe parameters");
            return new AgentFailedAuthenticationResult(
                    LunarAuthExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new AuthorizationError()),
                    authDataAccessor.clearData());
        }

        LunarNemIdParametersFetcher parametersFetcher = iframeAttributes.getParametersFetcher();
        parametersFetcher.setNemIdParameters(
                SerializationUtils.serializeToString(nemIdParamsResponse));
        processState.setChallenge(nemIdParamsResponse.getChallenge());
        authData.setDeviceId(deviceId);

        try {
            NemIdIFrameController iFrameController = iframeAttributes.getNemIdIFrameController();
            String b64Token =
                    iFrameController.logInWithCredentials(iframeAttributes.getCredentials());
            processState.setNemIdToken(decodeToken(b64Token));
        } catch (AuthenticationException e) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationExceptionHandler.toError(e), authDataAccessor.clearData());
        }

        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(GetLunarAccessTokenStep.class),
                processStateAccessor.storeState(processState),
                authDataAccessor.storeData(authData));
    }

    private String decodeToken(String b64Token) {
        byte[] decode = Base64.getDecoder().decode(b64Token);
        return new String(decode, StandardCharsets.UTF_8);
    }
}
