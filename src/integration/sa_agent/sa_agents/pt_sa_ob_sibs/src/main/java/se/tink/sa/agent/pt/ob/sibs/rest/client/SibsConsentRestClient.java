package se.tink.sa.agent.pt.ob.sibs.rest.client;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentStatusResponse;
import se.tink.sa.framework.rest.client.AbstractBusinessRestClient;

@Component
public class SibsConsentRestClient extends AbstractBusinessRestClient {

    @Value("${bank.rest.service.consents.path}")
    private String consentsBasePath;

    @Value("${bank.rest.service.consents.path.status}")
    private String consentsStatusPath;

    @Value("${tink.redirect.url}")
    private String redirectUrl;

    public ConsentResponse getConsent(ConsentRequest request, String bankCode, String state) {
        String url = prepareUrl(baseUrl, consentsBasePath);
        Map<String, String> params = new HashMap<>();
        params.put(SibsConstants.PathParameterKeys.ASPSP_CDE, bankCode);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.set(SibsConstants.HeaderKeys.TPP_REDIRECT_URI, getRedirectUrlForState(state));

        HttpEntity<ConsentRequest> httpEntity = new HttpEntity<>(request, headers);

        ConsentResponse response =
                restTemplate.postForObject(url, httpEntity, ConsentResponse.class, params);

        return response;
    }

    protected String getRedirectUrlForState(String state) {
        StringBuilder sb =
                new StringBuilder()
                        .append(redirectUrl)
                        .append("?")
                        .append(SibsConstants.QueryKeys.STATE)
                        .append("=")
                        .append(state);
        return sb.toString();
    }

    public ConsentStatusResponse checkConsentStatus(ConsentStatusRequest request, String bankCode) {
        String url = prepareUrl(baseUrl, consentsStatusPath);

        Map<String, String> params = new HashMap<>();
        params.put(SibsConstants.PathParameterKeys.ASPSP_CDE, bankCode);
        params.put(SibsConstants.PathParameterKeys.CONSENT_ID, request.getConsentId());

        ConsentStatusResponse response =
                restTemplate.getForObject(url, ConsentStatusResponse.class, params);

        return response;
    }
}
