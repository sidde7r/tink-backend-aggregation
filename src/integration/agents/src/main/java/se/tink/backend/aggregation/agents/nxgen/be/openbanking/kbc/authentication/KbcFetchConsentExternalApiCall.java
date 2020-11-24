package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import agents_platform_agents_framework.org.springframework.http.MediaType;
import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.KbcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.AgentExtendedClientInfo;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentSimpleExternalApiCall;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;

public class KbcFetchConsentExternalApiCall
        extends AgentSimpleExternalApiCall<
                KbcFetchConsentExternalApiCallParameters,
                String,
                ConsentBaseRequest,
                ConsentBaseResponse> {

    private final String urlScheme;

    public KbcFetchConsentExternalApiCall(AgentHttpClient httpClient, String kbcUrlScheme) {
        super(httpClient, ConsentBaseResponse.class);
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
    protected ExternalApiCallResult<String> parseResponse(
            ResponseEntity<ConsentBaseResponse> httpResponse) {
        return new ExternalApiCallResult<>(httpResponse.getBody().getConsentId());
    }
}
