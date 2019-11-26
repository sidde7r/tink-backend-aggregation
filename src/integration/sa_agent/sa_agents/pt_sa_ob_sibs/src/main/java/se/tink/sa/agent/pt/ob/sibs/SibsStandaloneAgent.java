package se.tink.sa.agent.pt.ob.sibs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import se.tink.sa.framework.StandaloneAgent;

@Slf4j
@ComponentScan(basePackageClasses = {StandaloneAgent.class, SibsStandaloneAgent.class})
@PropertySource("classpath:application.properties")
@Configuration
public class SibsStandaloneAgent extends StandaloneAgent {

    public static void main(String[] args) {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(SibsStandaloneAgent.class);
    }
}
