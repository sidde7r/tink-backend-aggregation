package se.tink.sa.agent.pt.ob.sibs.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.common.AbstractSibsRestClient;
import se.tink.sa.framework.rest.client.RequestUrlBuilder;

@Component
public class SibsConsentRestClient extends AbstractSibsRestClient {

    @Value("${bank.rest.service.consents.path}")
    private String consentsBasePath;

    @Value("${bank.rest.service.consents.path.status}")
    private String consentsStatusPath;

    @Value("${tink.redirect.url}")
    private String redirectUrl;

    public ConsentResponse getConsent(ConsentRequest request, String bankCode, String state) {
        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(consentsBasePath)
                        .pathVariable(SibsConstants.PathParameterKeys.ASPSP_CDE, bankCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.TPP_REDIRECT_URI, getRedirectUrlForState(state));

        HttpEntity<ConsentRequest> httpEntity = new HttpEntity<>(request, headers);

        ConsentResponse response =
                restTemplate.postForObject(builder.build(), httpEntity, ConsentResponse.class);

        return response;
    }

    protected String getRedirectUrlForState(String state) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(redirectUrl);
        uriBuilder.queryParam(SibsConstants.QueryKeys.STATE, state);
        return uriBuilder.toUriString();
    }

    public ConsentStatusResponse checkConsentStatus(ConsentStatusRequest request, String bankCode) {
        RequestUrlBuilder builder =
                RequestUrlBuilder.newInstance()
                        .appendUri(baseUrl)
                        .appendUri(consentsStatusPath)
                        .pathVariable(SibsConstants.PathParameterKeys.ASPSP_CDE, bankCode)
                        .pathVariable(
                                SibsConstants.PathParameterKeys.CONSENT_ID, request.getConsentId());

        ConsentStatusResponse response =
                restTemplate.getForObject(builder.build(), ConsentStatusResponse.class);

        return response;
    }
}
