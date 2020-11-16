package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.util.HashMap;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.IbanFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.framework.http.ExternalApiCallResult;

public class KbcFetchConsentAuthenticationStepTest {

    @Test
    public void shouldFetchConsentAndStoreThemInStorageAndRequestForRedirectStep() {
        // given
        final String iban = "BE68539007547034";
        final String consent = "1234567890";
        final URI redirectUrl = URI.create("http://testhost");
        final String psuIdAddress = "0.0.0.0";
        AgentAuthenticationPersistedData persistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        KbcFetchConsentExternalApiCallParameters fetchConsentExternalApiCallParameters =
                new KbcFetchConsentExternalApiCallParameters(
                        iban, redirectUrl.toString(), psuIdAddress);
        KbcFetchConsentExternalApiCall fetchConsentExternalApiCall =
                Mockito.mock(KbcFetchConsentExternalApiCall.class);
        ExternalApiCallResult<String> fetchConsentExternalApiCallResult =
                Mockito.mock(ExternalApiCallResult.class);
        Mockito.when(fetchConsentExternalApiCallResult.getResponse())
                .thenReturn(Optional.of(consent));
        Mockito.when(
                        fetchConsentExternalApiCall.execute(
                                Mockito.eq(fetchConsentExternalApiCallParameters)))
                .thenReturn(fetchConsentExternalApiCallResult);
        AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest =
                Mockito.mock(AgentUserInteractionAuthenticationProcessRequest.class);
        AgentUserInteractionData agentUserInteractionData =
                Mockito.mock(AgentUserInteractionData.class);
        Mockito.when(agentUserInteractionData.getFieldValue(IbanFieldDefinition.id()))
                .thenReturn(iban);
        Mockito.when(authenticationProcessRequest.getUserInteractionData())
                .thenReturn(agentUserInteractionData);
        Mockito.when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(persistedData);
        KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory =
                new KbcPersistedDataAccessorFactory(new ObjectMapper());
        KbcFetchConsentAuthenticationStep objectUnderTest =
                new KbcFetchConsentAuthenticationStep(
                        redirectUrl,
                        psuIdAddress,
                        fetchConsentExternalApiCall,
                        kbcPersistedDataAccessorFactory);
        // when
        AgentProceedNextStepAuthenticationResult result =
                (AgentProceedNextStepAuthenticationResult)
                        objectUnderTest.execute(authenticationProcessRequest);
        // then
        KbcPersistedData kbcPersistedData =
                kbcPersistedDataAccessorFactory.createKbcAuthenticationPersistedDataAccessor(
                        result.getAuthenticationPersistedData());
        Assertions.assertThat(result.getAuthenticationProcessStepIdentifier().get())
                .isEqualTo(
                        AgentAuthenticationProcessStep.identifier(
                                RedirectPreparationRedirectUrlStep.class));
        Assertions.assertThat(kbcPersistedData.getKbcAuthenticationData().getConsentId())
                .isEqualTo(consent);
    }
}
