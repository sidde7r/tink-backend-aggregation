package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import agents_platform_agents_framework.org.apache.http.client.utils.URIBuilder;
import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import javax.ws.rs.core.HttpHeaders;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.ErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.errors.rpc.KbcErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.QueryValues;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AuthorizationError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidRequestError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.SessionExpiredError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class KbcConsentValidationCall
        extends AgentSimpleExternalApiCall<KbcConsentValidationParameters, String, Void, String> {

    public KbcConsentValidationCall(AgentHttpClient httpClient) {
        super(httpClient, String.class);
    }

    @SneakyThrows
    @Override
    protected RequestEntity<Void> prepareRequest(
            KbcConsentValidationParameters arg, AgentExtendedClientInfo clientInfo) {
        URI uri =
                new URIBuilder(Urls.BASE_URL + Urls.ACCOUNTS)
                        .addParameter(QueryKeys.WITH_BALANCE, QueryValues.TRUE)
                        .build();

        return RequestEntity.get(uri)
                .header(Psd2Headers.Keys.X_REQUEST_ID, Psd2Headers.getRequestId())
                .header(Psd2Headers.Keys.PSU_IP_ADDRESS, arg.getPsuIpAddress())
                .header(HeaderKeys.CONSENT_ID, arg.getConsentId())
                .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", arg.getAccessToken()))
                .build();
    }

    @Override
    protected ExternalApiCallResult<String> parseResponse(ResponseEntity<String> httpResponse) {
        HttpStatus statusCode = httpResponse.getStatusCode();
        if (statusCode.is2xxSuccessful()) {
            return new ExternalApiCallResult<>(httpResponse.getBody());
        } else if (HttpStatus.UNAUTHORIZED.equals(statusCode)) {
            return handleUnauthorizedResponse(httpResponse);
        } else if (statusCode.is5xxServerError()) {
            return new ExternalApiCallResult<>(new ServerError());
        }
        return new ExternalApiCallResult<>(new InvalidRequestError());
    }

    private ExternalApiCallResult<String> handleUnauthorizedResponse(
            ResponseEntity<String> httpResponse) {
        KbcErrorMessage errorResponse =
                SerializationUtils.deserializeFromString(
                        httpResponse.getBody(), KbcErrorMessage.class);
        if (KbcAuthenticationUtils.doesResponseContainCode(
                errorResponse, ErrorCodes.CONSENT_EXPIRED)) {
            return new ExternalApiCallResult<>(new SessionExpiredError());
        }
        return new ExternalApiCallResult<>(new AuthorizationError());
    }
}
