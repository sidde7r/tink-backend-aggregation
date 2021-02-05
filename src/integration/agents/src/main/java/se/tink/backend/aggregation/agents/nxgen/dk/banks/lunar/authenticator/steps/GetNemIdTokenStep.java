package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import agents_platform_framework.org.springframework.web.server.ResponseStatusException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.AuthenticationExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
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
                    AuthenticationExceptionHandler.toKnownErrorFromResponseOrDefault(
                            e, new AuthorizationError()),
                    request.getAuthenticationPersistedData());
        }

        LunarNemIdParametersFetcher parametersFetcher = iframeAttributes.getParametersFetcher();
        parametersFetcher.setNemIdParameters(
                SerializationUtils.serializeToString(nemIdParamsResponse));
        processState.setChallenge(nemIdParamsResponse.getChallenge());
        authData.setDeviceId(deviceId);

        try {
            return loginToNemIdResult(
                    processState,
                    iframeAttributes.getNemIdIFrameController(),
                    authDataAccessor,
                    authData,
                    processStateAccessor);
        } catch (LoginException e) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationExceptionHandler.toErrorFromLoginException(e),
                    request.getAuthenticationPersistedData());
        } catch (NemIdException e) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationExceptionHandler.toErrorFromNemIdException(e),
                    request.getAuthenticationPersistedData());
        } catch (SupplementalInfoException e) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationExceptionHandler.toErrorFromSupplementalInfoException(e),
                    request.getAuthenticationPersistedData());
        }
    }

    private AgentUserInteractionDefinitionResult loginToNemIdResult(
            LunarProcessState processState,
            NemIdIFrameController iFrameController,
            LunarAuthDataAccessor authDataAccessor,
            LunarAuthData authData,
            LunarProcessStateAccessor processStateAccessor) {

        String b64Token = iFrameController.doLoginWith(iframeAttributes.getCredentials());

        processState.setNemIdToken(decodeToken(b64Token));

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStep.identifier(GetLunarAccessTokenStep.class),
                authDataAccessor.storeData(authData),
                processStateAccessor.storeState(processState));
    }

    private String decodeToken(String b64Token) {
        byte[] decode = Base64.getDecoder().decode(b64Token);
        return new String(decode, StandardCharsets.UTF_8);
    }
}
