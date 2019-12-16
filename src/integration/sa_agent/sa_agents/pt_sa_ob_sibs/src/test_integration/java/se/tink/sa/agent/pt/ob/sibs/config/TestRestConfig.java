package se.tink.sa.agent.pt.ob.sibs.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import se.tink.sa.agent.pt.ob.sibs.SibsStandaloneAgent;

@Configuration
@PropertySource("classpath:secrets.properties")
@Import({SibsStandaloneAgent.class})
public class TestRestConfig {}
