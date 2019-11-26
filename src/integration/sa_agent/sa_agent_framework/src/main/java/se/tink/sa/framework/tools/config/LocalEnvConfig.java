package se.tink.sa.framework.tools.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.tink.sa.framework.tools.SecretsHandler;
import se.tink.sa.framework.tools.impl.LocalSecretsHandlerImpl;

@Configuration
public class LocalEnvConfig {

    @Bean
    public SecretsHandler secretsHandler() {
        return new LocalSecretsHandlerImpl();
    }
}
