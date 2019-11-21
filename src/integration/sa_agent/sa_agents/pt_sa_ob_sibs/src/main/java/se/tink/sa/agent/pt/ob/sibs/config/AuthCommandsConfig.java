package se.tink.sa.agent.pt.ob.sibs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import se.tink.sa.framework.command.auth.AuthorizationBuildUrlCommand;

@Configuration
public class AuthCommandsConfig {

    @Bean
    public AuthorizationBuildUrlCommand authorizationBuildUrlCommand() {
        return new AuthorizationBuildUrlCommand();
    }
}
