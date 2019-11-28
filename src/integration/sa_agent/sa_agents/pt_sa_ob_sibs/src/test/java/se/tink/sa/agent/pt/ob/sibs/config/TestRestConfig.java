package se.tink.sa.agent.pt.ob.sibs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;

@Configuration
@PropertySource("classpath:secrets.properties")
@Import({SibsStandaloneAgent.class})
public class TestRestConfig {

    //    @Value("${test.configuration.security.clientId}")
    //    private String clientId;
    //
    //    @Value("${test.configuration.security.keyStorePath}")
    //    private String keyStorePath;
    //    @Value("${test.configuration.security.keyStorePassword}")
    //    private String keyStorePassword;
    //    @Value("${test.configuration.security.keyStoreAlias}")
    //    private String keyStoreAlias;
    //    @Value("${test.configuration.security.keyPassword}")
    //    private String keyPassword;
    //
    //    @Bean
    //    public EncryptionCertificateTool encryptionService() throws IOException {
    //        Resource resource = new ClassPathResource(keyStorePath);
    //        InputStream keyStoreStream = resource.getInputStream();
    //        return LocalEncryptionCertificateToolFactory.buildKeyStoreKeyProvider(keyStoreStream,
    // keyStorePassword.toCharArray(), keyStoreAlias, keyPassword.toCharArray());
    //    }
    //
    //    @Bean
    //    public RestTemplate restTemplate() {
    //        RestTemplate restTemplate = new RestTemplate();

    //        restTemplate.setInterceptors(interceptors);
    //        return restTemplate;
    //    }
    //
    //    @Bean
    //    public SibsMessageSignInterceptor sibsMessageSignInterceptor(){
    //        return new SibsMessageSignInterceptor();
    //    }

    //    @Bean
    //    public SibsConsentRestClient sibsConsentRestClient() {
    //        return new SibsConsentRestClient();
    //    }

}
