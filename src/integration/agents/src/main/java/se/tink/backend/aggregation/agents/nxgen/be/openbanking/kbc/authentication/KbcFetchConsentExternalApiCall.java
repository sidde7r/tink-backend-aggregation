package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.errors.rpc.KbcErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthenticationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.agentsplatform.framework.error.Error;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class KbcFetchConsentExternalApiCall
        extends AgentSimpleExternalApiCall<
                KbcFetchConsentExternalApiCallParameters, String, ConsentBaseRequest, String> {

    private final String urlScheme;

    public KbcFetchConsentExternalApiCall(AgentHttpClient httpClient, String kbcUrlScheme) {
        super(httpClient, String.class);
        urlScheme = kbcUrlScheme;
    }

    @Override
    protected RequestEntity<ConsentBaseRequest> prepareRequest(
            KbcFetchConsentExternalApiCallParameters arg, AgentExtendedClientInfo clientInfo) {
        final List<String> ibanList = Collections.singletonList(arg.getIban());
        final AccessEntity accessEntity =
                new AccessEntity.Builder()
                        .withBalances(ibanList)
                        .withTransactions(ibanList)
                        .build();
        final ConsentBaseRequest consentsRequest = new ConsentBaseRequest();
        consentsRequest.setAccess(accessEntity);

        return RequestEntity.post(URI.create(urlScheme + KbcConstants.Urls.CONSENT))
                .header(BerlinGroupConstants.HeaderKeys.X_REQUEST_ID, UUID.randomUUID().toString())
                .header(BerlinGroupConstants.HeaderKeys.TPP_REDIRECT_URI, arg.getRedirectUrl())
                .header(BerlinGroupConstants.HeaderKeys.PSU_IP_ADDRESS, arg.getPsuIpAddress())
                .contentType(MediaType.APPLICATION_JSON)
                .body(consentsRequest);
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(ResponseEntity<String> httpResponse) {
        HttpStatus statusCode = httpResponse.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            return handleSuccessfulResponse(httpResponse);
        } else if (HttpStatus.UNAUTHORIZED.equals(statusCode)) {
            return handleUnauthorizedResponse(httpResponse);
        } else if (statusCode.is5xxServerError()) {
            return new ExternalApiCallResult<>(new ServerError());
        }
        return new ExternalApiCallResult<>(new InvalidRequestError());
    }

    private ExternalApiCallResult<String> handleSuccessfulResponse(
            ResponseEntity<String> httpResponse) {
        ConsentBaseResponse consentBaseResponse =
                SerializationUtils.deserializeFromString(
                        httpResponse.getBody(), ConsentBaseResponse.class);
        return new ExternalApiCallResult<>(consentBaseResponse.getConsentId());
    }

    private ExternalApiCallResult<String> handleUnauthorizedResponse(
            ResponseEntity<String> httpResponse) {
        KbcErrorMessage errorResponse =
                SerializationUtils.deserializeFromString(
                        httpResponse.getBody(), KbcErrorMessage.class);
        if (KbcAuthenticationUtils.doesResponseContainCode(
                errorResponse, ErrorCodes.CONSENT_INVALID)) {
            return new ExternalApiCallResult<>(
                    new AuthenticationError(
                            Error.builder()
                                    .uniqueId(UUID.randomUUID().toString())
                                    .errorCode(AgentError.INVALID_CREDENTIALS.getCode())
                                    .errorMessage(ErrorCodes.CONSENT_INVALID)
                                    .build()));
        }
        return new ExternalApiCallResult<>(new AuthorizationError());
    }
}
