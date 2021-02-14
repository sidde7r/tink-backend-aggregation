package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.RegexValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcAuthenticationData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedData;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence.KbcPersistedDataAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.request.AgentUserInteractionAuthenticationProcessRequest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentFailedAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.result.AgentProceedNextStepAuthenticationResult;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.steps.AgentAuthenticationProcessStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.userinteraction.fielddefinition.IbanFieldDefinition;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.redirect.RedirectPreparationRedirectUrlStep;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;

@AllArgsConstructor
@Slf4j
public class KbcFetchConsentAuthenticationStep
        implements AgentAuthenticationProcessStep<
                AgentUserInteractionAuthenticationProcessRequest> {

    private final URI redirectUrl;
    private final String psuIpAddress;
    private final KbcFetchConsentExternalApiCall fetchConsentExternalApiCall;
    private final KbcPersistedDataAccessorFactory kbcPersistedDataAccessorFactory;

    private static final Pattern IBAN_PATTERN = Pattern.compile(RegexValues.IBAN);

    @Override
    public AgentAuthenticationResult execute(
            AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest) {
        String ibanValue =
                authenticationProcessRequest
                        .getUserInteractionData()
                        .getFieldValue(IbanFieldDefinition.id());

        Matcher ibanMatcher = IBAN_PATTERN.matcher(ibanValue);
        if (!ibanMatcher.matches()) {
            return new AgentFailedAuthenticationResult(
                    new AuthenticationError(
                            new Error(
                                    UUID.randomUUID().toString(),
                                    "IBAN value does not match regex",
                                    "IBAN_REGEX_ERROR")),
                    authenticationProcessRequest.getAuthenticationPersistedData());
        }

        ExternalApiCallResult<String> apiCallResult =
                executeApiCall(authenticationProcessRequest, ibanValue);

        return parseApiCallResult(apiCallResult, authenticationProcessRequest);
    }

    private ExternalApiCallResult<String> executeApiCall(
            AgentUserInteractionAuthenticationProcessRequest authenticationProcessRequest,
            String ibanValue) {
        KbcFetchConsentExternalApiCallParameters fetchConsentExternalApiCallParameters =
                new KbcFetchConsentExternalApiCallParameters(
                        ibanValue, redirectUrl.toString(), psuIpAddress);
        return fetchConsentExternalApiCall.execute(
                fetchConsentExternalApiCallParameters,
                null,
                AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                        authenticationProcessRequest.getAuthenticationPersistedData()));
    }

    private AgentAuthenticationResult parseApiCallResult(
            ExternalApiCallResult<String> apiCallResult,
            AgentUserInteractionAuthenticationProcessRequest authenticationRequest) {
        Optional<AgentBankApiError> optionalBankError = apiCallResult.getAgentBankApiError();
        if (optionalBankError.isPresent()) {
            log.error("Could not fetch consent");
            return new AgentFailedAuthenticationResult(
                    optionalBankError.get(),
                    authenticationRequest.getAuthenticationPersistedData());
        }
        Optional<String> optionalConsentResponse = apiCallResult.getResponse();

        if (!optionalConsentResponse.isPresent()) {
            log.error("Could not find consent response");
            return new AgentFailedAuthenticationResult(
                    new AuthorizationError(),
                    authenticationRequest.getAuthenticationPersistedData());
        }

        String consentId = optionalConsentResponse.get();
        KbcPersistedData persistedData =
                kbcPersistedDataAccessorFactory.createKbcAuthenticationPersistedDataAccessor(
                        authenticationRequest.getAuthenticationPersistedData());
        KbcAuthenticationData kbcAuthenticationData = persistedData.getKbcAuthenticationData();
        kbcAuthenticationData.setConsentId(consentId);
        return new AgentProceedNextStepAuthenticationResult(
                AgentAuthenticationProcessStep.identifier(RedirectPreparationRedirectUrlStep.class),
                persistedData.storeKbcAuthenticationData(kbcAuthenticationData));
    }
}
