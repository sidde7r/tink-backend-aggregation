package se.tink.sa.framework.rest.config;

import org.apache.http.HttpHost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
public class DefaultRestServicesConfig {

    @Value("${security.eidas.proxy.address}")
    private String eidasProxyAddress;

    @Value("${security.eidas.proxy.port}")
    private int eidasProxyPort;

    @Value("${security.eidas.proxy.protocol}")
    private String eidasProxyProtocol;

    @Bean
    @Primary
    public HttpComponentsClientHttpRequestFactory clientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(httpClient());
        return clientHttpRequestFactory;
    }

    @Bean
    @Primary
    public CloseableHttpClient httpClient() {
        CloseableHttpClient client =
                HttpClientBuilder.create()
                        .setProxy(
                                new HttpHost(eidasProxyAddress, eidasProxyPort, eidasProxyProtocol))
                        .build();
        return client;
    }
}
