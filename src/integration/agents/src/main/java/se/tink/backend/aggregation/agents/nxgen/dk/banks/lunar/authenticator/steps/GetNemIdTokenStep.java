package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.steps;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.AgentPlatformLunarApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametrsFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeControllerAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.exception.LunarAuthenticationExceptionHandler;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthData;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessState;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;

@RequiredArgsConstructor
@Slf4j
public class GetNemIdTokenStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final LunarDataAccessorFactory dataAccessorFactory;
    private final AgentPlatformLunarApiClient apiClient;
    private final NemIdIframeControllerAttributes nemIdIframeControllerAttributes;
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

        NemIdIFrameController iFrameController = getNemIdIFrameController(processState, deviceId);
        authData.setDeviceId(deviceId);

        try {
            return loginToNemIdResult(
                    processState,
                    iFrameController,
                    authDataAccessor,
                    authData,
                    processStateAccessor);
        } catch (LoginException e) {
            return new AgentFailedAuthenticationResult(
                    LunarAuthenticationExceptionHandler.toErrorFromLoginException(e),
                    request.getAuthenticationPersistedData());
        } catch (NemIdException e) {
            return new AgentFailedAuthenticationResult(
                    LunarAuthenticationExceptionHandler.toErrorFromNemIdException(e),
                    request.getAuthenticationPersistedData());
        } catch (SupplementalInfoException e) {
            return new AgentFailedAuthenticationResult(
                    LunarAuthenticationExceptionHandler.toErrorFromSupplementalInfoException(e),
                    request.getAuthenticationPersistedData());
        }
    }

    private AgentUserInteractionDefinitionResult loginToNemIdResult(
            LunarProcessState processState,
            NemIdIFrameController iFrameController,
            LunarAuthDataAccessor authDataAccessor,
            LunarAuthData authData,
            LunarProcessStateAccessor processStateAccessor) {

        String b64Token =
                iFrameController.doLoginWith(nemIdIframeControllerAttributes.getCredentials());

        processState.setNemIdToken(decodeToken(b64Token));

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        GetLunarAccessTokenStep.class.getSimpleName()),
                authDataAccessor.storeData(authData),
                processStateAccessor.storeState(processState));
    }

    private String decodeToken(String b64Token) {
        byte[] decode = Base64.getDecoder().decode(b64Token);
        return new String(decode, StandardCharsets.UTF_8);
    }

    private NemIdIFrameController getNemIdIFrameController(
            LunarProcessState processState, String deviceId) {
        return NemIdIFrameControllerInitializer.initNemIdIframeController(
                new LunarNemIdParametrsFetcher(processState, apiClient, deviceId),
                nemIdIframeControllerAttributes.getCatalog(),
                nemIdIframeControllerAttributes.getStatusUpdater(),
                nemIdIframeControllerAttributes.getSupplementalRequester(),
                nemIdIframeControllerAttributes.getMetricContext());
    }
}
