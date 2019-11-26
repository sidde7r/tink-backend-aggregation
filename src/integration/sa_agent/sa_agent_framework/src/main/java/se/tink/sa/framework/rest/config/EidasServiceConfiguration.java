package se.tink.sa.framework.rest.config;

import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import se.tink.sa.framework.rest.interceptor.RestIoLogger;
import se.tink.sa.framework.security.EidasServiceVerificationCertsLoader;

@Configuration
public class EidasServiceConfiguration {

    @Autowired private EidasServiceVerificationCertsLoader eidasServiceVerificationCertsLoader;

    @Bean
    public RestTemplate eidasRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(eidasClientHttpRequestFactory());
        restTemplate.getInterceptors().add(new RestIoLogger());
        return restTemplate;
    }

    @Bean
    public HttpComponentsClientHttpRequestFactory eidasClientHttpRequestFactory() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory();
        clientHttpRequestFactory.setHttpClient(eidasHttpClient());
        return clientHttpRequestFactory;
    }

    @Bean
    public CloseableHttpClient eidasHttpClient() {
        CloseableHttpClient client = null;
        try {

            KeyStore trustStore = eidasServiceVerificationCertsLoader.getRootCaTrustStore();
            KeyStore keyStore = eidasServiceVerificationCertsLoader.getClientCertKeystore();

            SSLContext sslContext =
                    new SSLContextBuilder()
                            .loadTrustMaterial(
                                    trustStore,
                                    TrustRootCaStrategy.createWithoutFallbackTrust(trustStore))
                            .loadKeyMaterial(keyStore, "changeme".toCharArray())
                            .build();

            client =
                    HttpClients.custom()
                            .setHostnameVerifier(new AllowAllHostnameVerifier())
                            .setSslcontext(sslContext)
                            .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return client;
    }
}
