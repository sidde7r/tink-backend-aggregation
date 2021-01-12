package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.steps;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.AgentPlatformFortisApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.FortisConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.AuthenticationErrorHandler;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.fields.PhonenumberInputField;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthData;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisAuthDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence.FortisDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.CheckLoginResultResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc.legacy.UserInfoResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessStepIdentifier;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentUserInteractionDefinitionResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.CardReaderLoginInputAgentField;

@RequiredArgsConstructor
public class CheckLoginResultStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final AgentPlatformFortisApiClient fortisApiClient;
    private final FortisDataAccessorFactory dataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest request) {

        String signCode =
                request.getUserInteractionData()
                        .getFieldValue(CardReaderLoginInputAgentField.id())
                        .replace(" ", "");

        FortisAuthDataAccessor authDataAccessor =
                dataAccessorFactory.createAuthDataAccessor(
                        request.getAuthenticationPersistedData());

        FortisAuthData fortisAuthData = authDataAccessor.get();

        CheckLoginResultResponse checkLoginResponse =
                fortisApiClient.checkLoginResult(fortisAuthData.getClientNumber(), signCode);
        if (checkLoginResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(checkLoginResponse, true),
                    request.getAuthenticationPersistedData());
        }

        // Mandatory void calls
        fortisApiClient.doEbewAppLogin(
                fortisAuthData.getClientNumber(), FortisConstants.MeanIds.UCR);

        UserInfoResponse userInfoResponse = fortisApiClient.getUserInfo();
        if (userInfoResponse.isError()) {
            return new AgentFailedAuthenticationResult(
                    AuthenticationErrorHandler.getError(userInfoResponse),
                    request.getAuthenticationPersistedData());
        }

        fortisApiClient.getCountryList();

        return new AgentUserInteractionDefinitionResult(
                AgentAuthenticationProcessStepIdentifier.of(
                        EasyPinCreateStep.class.getSimpleName()),
                request.getAuthenticationPersistedData(),
                request.getAuthenticationProcessState(),
                new PhonenumberInputField());
    }
}
