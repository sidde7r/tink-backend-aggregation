package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.steps.logindevice;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.GlobalConstants;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.LoginDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroPersistedDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessState;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance.MetroProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentProceedNextStepAuthenticationRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentSucceededAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

@AllArgsConstructor
@Slf4j
public class LoginDeviceStep
        implements AgentAuthenticationProcessStep<AgentProceedNextStepAuthenticationRequest> {

    private final MetroDataAccessorFactory accessorFactory;

    private final LoginDeviceCall loginDeviceCall;

    @Override
    public AgentAuthenticationResult execute(AgentProceedNextStepAuthenticationRequest request) {
        MetroPersistedDataAccessor persistedDataAccessor =
                accessorFactory.createPersistedDataAccessor(
                        request.getAuthenticationPersistedData());
        MetroProcessStateAccessor processStateAccessor =
                accessorFactory.createProcessStateAccessor(request.getAuthenticationProcessState());

        MetroAuthenticationData authenticationData = persistedDataAccessor.getAuthenticationData();
        MetroProcessState processState = processStateAccessor.getProcessState();

        ExternalApiCallResult<String> result =
                loginDeviceCall.execute(
                        new LoginDeviceParameters(
                                authenticationData.getDeviceId(),
                                processState.getToken(),
                                authenticationData.getUserId(),
                                buildRequest(authenticationData)),
                        request.getAgentExtendedClientInfo(),
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                request.getAuthenticationPersistedData()));

        return result.getResponse()
                .map(response -> nextStep(persistedDataAccessor, authenticationData))
                .orElseGet(
                        () ->
                                new AgentFailedAuthenticationResult(
                                        result.getAgentBankApiError().get(),
                                        persistedDataAccessor.storeAuthenticationData(
                                                authenticationData)));
    }

    private LoginDeviceRequest buildRequest(MetroAuthenticationData authenticationData) {
        return LoginDeviceRequest.builder()
                .internalDeviceId(authenticationData.getInternalDeviceId())
                .deviceName(GlobalConstants.DEVICE_NAME.name())
                .deviceModel(GlobalConstants.DEVICE_MODEL.getValue())
                .deviceManufacturer(GlobalConstants.DEVICE_COMPANY.getValue())
                .appVersion(GlobalConstants.APP_VERSION.getValue())
                .deviceOS(GlobalConstants.PLATFORM.getValue())
                .build();
    }

    private AgentAuthenticationResult nextStep(
            MetroPersistedDataAccessor persistedDataAccessor,
            MetroAuthenticationData authenticationData) {
        return new AgentSucceededAuthenticationResult(
                persistedDataAccessor.storeAuthenticationData(
                        authenticationData.setAlreadyRegistered(true)));
    }
}
