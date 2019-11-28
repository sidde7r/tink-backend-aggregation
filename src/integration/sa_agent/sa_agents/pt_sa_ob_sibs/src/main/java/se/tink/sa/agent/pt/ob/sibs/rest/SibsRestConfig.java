package se.tink.sa.agent.pt.ob.sibs.rest;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import se.tink.sa.agent.pt.ob.sibs.rest.interceptor.SibsMessageSignInterceptor;
import se.tink.sa.agent.pt.ob.sibs.rest.interceptor.SibsStandardHeadersAppender;
import se.tink.sa.framework.rest.config.DefaultRestServicesConfig;
import se.tink.sa.framework.rest.interceptor.EidasHeadersAppender;
import se.tink.sa.framework.rest.interceptor.RestIoLogger;
import se.tink.sa.framework.tools.SecretsHandler;

@Configuration
public class SibsRestConfig extends DefaultRestServicesConfig {

    @Autowired private SecretsHandler secretsHandler;

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add(sibsStandardHeadersAppender());
        interceptors.add(sibsMessageSignInterceptor());
        interceptors.add(new RestIoLogger());
        interceptors.add(eidasHeadersAppender());
        restTemplate.setInterceptors(interceptors);
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        return restTemplate;
    }

    @Bean
    public SibsMessageSignInterceptor sibsMessageSignInterceptor() {
        return new SibsMessageSignInterceptor();
    }

    @Bean
    public SibsStandardHeadersAppender sibsStandardHeadersAppender() {
        return new SibsStandardHeadersAppender();
    }

    @Bean
    public EidasHeadersAppender eidasHeadersAppender() {
        return new EidasHeadersAppender();
    }
}
