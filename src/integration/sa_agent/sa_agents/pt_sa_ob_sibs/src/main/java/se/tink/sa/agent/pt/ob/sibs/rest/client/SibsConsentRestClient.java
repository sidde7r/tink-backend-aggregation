package se.tink.sa.agent.pt.ob.sibs.rest.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import se.tink.sa.agent.pt.ob.sibs.SibsConstants;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentRequest;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc.ConsentResponse;
import se.tink.sa.framework.rest.ConsentsRestClient;

@Component
public class SibsConsentRestClient implements ConsentsRestClient {

    @Value("${bank.rest.service.consents.path}")
    private String consentsBasePath;

    @Value("${bank.rest.service.consents.path.status}")
    private String consentsStatusPath;

    @Value("${bank.rest.service.url}")
    protected String baseUrl;

    @Value("${tink.redirect.url}")
    private String redirectUrl;

    @Autowired private RestTemplate restTemplate;

    public ConsentResponse getConsent(ConsentRequest request) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String code = UUID.randomUUID().toString().replace("-", "");
        String bankCode = "BCTT";

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

    protected String prepareUrl(String... path) {
        return StringUtils.join(path);
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

    @Override
    public <String, ConsentStatusResponse> ConsentStatusResponse checkConsentStatus(
            String request) {
        return null;
    }
}
