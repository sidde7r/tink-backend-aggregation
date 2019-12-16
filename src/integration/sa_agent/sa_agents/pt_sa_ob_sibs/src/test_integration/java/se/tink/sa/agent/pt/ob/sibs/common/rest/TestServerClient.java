package se.tink.sa.agent.pt.ob.sibs.common.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

public abstract class TestServerClient {

    @Autowired
    @Qualifier("testServerRestTemplate")
    protected RestTemplate restTemplate;

    @Value("${tpp.host}")
    private String host;

    @Value("${tpp.base.url}")
    private String baseUrl;

    protected String prepareUrl(String path) {
        return host + "/" + baseUrl + "/" + path;
    }
}
