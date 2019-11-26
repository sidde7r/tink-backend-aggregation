package se.tink.sa.agent.pt.ob.sibs.rest.client;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.RequestBuilder;
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
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.set(
                SibsConstants.HeaderKeys.TPP_REDIRECT_URI,
                getRedirectUrlForState(prepareParamsMap(state, code)));

        HttpEntity<ConsentRequest> httpEntity = new HttpEntity<>(request, headers);

        ConsentResponse response =
                restTemplate.postForObject(url, httpEntity, ConsentResponse.class, params);

        return response;
    }

    protected String prepareUrl(String... path) {
        return StringUtils.join(path);
    }

    protected String getRedirectUrlForState(Map<String, String> params) {
        RequestBuilder rb = RequestBuilder.create(redirectUrl);
        if (MapUtils.isNotEmpty(params)) {
            params.keySet().stream().forEach(key -> rb.addParameter(key, params.get(key)));
        }

        return rb.build().getURI().toString();
    }

    private Map<String, String> prepareParamsMap(String state, String code) {
        Map<String, String> params = new HashMap<>();
        params.put(SibsConstants.QueryKeys.STATE, state);
        params.put("code", code);
        return params;
    }

    @Override
    public <String, ConsentStatusResponse> ConsentStatusResponse checkConsentStatus(
            String request) {
        return null;
    }
}
