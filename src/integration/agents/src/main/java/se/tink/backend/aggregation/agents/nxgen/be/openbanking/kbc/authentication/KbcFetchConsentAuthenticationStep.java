package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.net.URI;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.IbanFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;

@AllArgsConstructor
public class KbcFetchConsentAuthenticationStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final URI redirectUrl;
    private final String psuIpAddress;
    private final KbcFetchConsentExternalApiCall fetchConsentExternalApiCall;
    private final KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory;

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest) {
        KbcFetchConsentExternalApiCallParameters fetchConsentExternalApiCallParameters =
                new KbcFetchConsentExternalApiCallParameters(
                        authenticationProcessRequest
                                .getUserInteractionData()
                                .getFieldValue(IbanFieldDefinition.id()),
                        redirectUrl.toString(),
                        psuIpAddress);
        final String consentId =
                fetchConsentExternalApiCall
                        .execute(fetchConsentExternalApiCallParameters)
                        .getResponse()
                        .get();
        KbcPersistedData persistedData =
                kbcPersistedDataAccessorFactory.createKbcAuthenticationPersistedDataAccessor(
                        authenticationProcessRequest.getAuthenticationPersistedData());
        KbcAuthenticationData kbcAuthenticationData = persistedData.getKbcAuthenticationData();
        kbcAuthenticationData.setConsentId(consentId);
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(RedirectPreparationRedirectUrlStep.class),
                persistedData.storeKbcAuthenticationData(kbcAuthenticationData));
    }
}
