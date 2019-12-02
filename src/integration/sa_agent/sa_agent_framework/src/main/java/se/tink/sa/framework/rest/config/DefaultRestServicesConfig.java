package se.tink.sa.framework.rest.config;

import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import se.tink.sa.framework.common.exceptions.StandaloneAgentException;
import se.tink.sa.framework.security.EidasServiceVerificationCertsLoader;

@Configuration
public class DefaultRestServicesConfig {

    @Value("${security.eidas.proxy.address}")
    private String eidasProxyAddress;

    @Value("${security.eidas.proxy.port:-1}")
    private int eidasProxyPort;

    @Value("${security.eidas.proxy.protocol}")
    private String eidasProxyProtocol;

    @Autowired private EidasServiceVerificationCertsLoader eidasServiceVerificationCertsLoader;

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
        CloseableHttpClient client = null;

        try {
            KeyStore trustStore = eidasServiceVerificationCertsLoader.getRootCaTrustStore();
            KeyStore keyStore = eidasServiceVerificationCertsLoader.getClientCertKeystore();

            SSLContext sslContext =
                    new SSLContextBuilder()
                            .loadKeyMaterial(keyStore, null)
                            .loadTrustMaterial(
                                    trustStore,
                                    TrustRootCaStrategy.createWithoutFallbackTrust(trustStore))
                            .loadKeyMaterial(keyStore, "changeme".toCharArray())
                            .build();

            client =
                    HttpClientBuilder.create()
                            .setProxy(
                                    new HttpHost(
                                            eidasProxyAddress, eidasProxyPort, eidasProxyProtocol))
                            .setSslcontext(sslContext)
                            .setHostnameVerifier(new AllowAllHostnameVerifier())
                            .build();
        } catch (Exception ex) {
            throw new StandaloneAgentException(ex);
        }
        return client;
    }
}
