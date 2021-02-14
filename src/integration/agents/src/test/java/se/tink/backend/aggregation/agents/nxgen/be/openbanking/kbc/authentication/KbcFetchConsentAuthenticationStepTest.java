package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.IbanFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

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
                mock(KbcFetchConsentExternalApiCall.class);
        ExternalApiCallResult<String> fetchConsentExternalApiCallResult =
                mock(ExternalApiCallResult.class);
        when(fetchConsentExternalApiCallResult.getResponse()).thenReturn(Optional.of(consent));
        when(fetchConsentExternalApiCall.execute(
                        Mockito.eq(fetchConsentExternalApiCallParameters),
                        Mockito.any(),
                        Mockito.any()))
                .thenReturn(fetchConsentExternalApiCallResult);
        AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest =
                mock(AgentUserInteractionAuthenticationProcessRequest.class);
        AgentUserInteractionData agentUserInteractionData = mock(AgentUserInteractionData.class);
        when(agentUserInteractionData.getFieldValue(IbanFieldDefinition.id())).thenReturn(iban);
        when(authenticationProcessRequest.getUserInteractionData())
                .thenReturn(agentUserInteractionData);
        when(authenticationProcessRequest.getAuthenticationPersistedData())
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

    @Test
    public void shouldFailWithInvalidIban() {
        // given
        final String iban = "BE1234";
        final URI redirectUrl = URI.create("http://testhost");
        final String psuIdAddress = "0.0.0.0";
        AgentAuthenticationPersistedData persistedData =
                new AgentAuthenticationPersistedData(new HashMap<>());
        AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest =
                mock(AgentUserInteractionAuthenticationProcessRequest.class);
        AgentUserInteractionData agentUserInteractionData = mock(AgentUserInteractionData.class);
        when(agentUserInteractionData.getFieldValue(IbanFieldDefinition.id())).thenReturn(iban);
        when(authenticationProcessRequest.getUserInteractionData())
                .thenReturn(agentUserInteractionData);
        when(authenticationProcessRequest.getAuthenticationPersistedData())
                .thenReturn(persistedData);

        KbcFetchConsentExternalApiCall fetchConsentExternalApiCall =
                mock(KbcFetchConsentExternalApiCall.class);
        KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory =
                new KbcPersistedDataAccessorFactory(new ObjectMapper());
        KbcFetchConsentAuthenticationStep objectUnderTest =
                new KbcFetchConsentAuthenticationStep(
                        redirectUrl,
                        psuIdAddress,
                        fetchConsentExternalApiCall,
                        kbcPersistedDataAccessorFactory);
        // when
        AgentAuthenticationResult result = objectUnderTest.execute(authenticationProcessRequest);
        // then
        assertTrue(result instanceof AgentFailedAuthenticationResult);
        AgentFailedAuthenticationResult agentFailedAuthenticationResult =
                (AgentFailedAuthenticationResult) result;
        Error errorDetails = agentFailedAuthenticationResult.getError().getDetails();
        Assertions.assertThat(errorDetails.getErrorCode()).isEqualTo("IBAN_REGEX_ERROR");
    }
}
