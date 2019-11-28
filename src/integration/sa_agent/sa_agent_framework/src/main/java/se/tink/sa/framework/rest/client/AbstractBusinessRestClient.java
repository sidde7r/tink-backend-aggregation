package se.tink.sa.framework.rest.client;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

public abstract class AbstractBusinessRestClient {

    @Value("${bank.rest.service.url}")
    protected String baseUrl;

    @Autowired protected RestTemplate restTemplate;

    protected String prepareUrl(String... path) {
        return StringUtils.join(path);
    }
}
