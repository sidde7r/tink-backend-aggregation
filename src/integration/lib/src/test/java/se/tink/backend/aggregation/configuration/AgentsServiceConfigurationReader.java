package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import java.io.IOException;
import javax.validation.Validation;
import javax.validation.Validator;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;

public final class AgentsServiceConfigurationReader {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static ConfigurationFactory<AgentsServiceConfigurationWrapper> CONFIGURATION_FACTORY =
            new ConfigurationFactory<>(
                    AgentsServiceConfigurationWrapper.class, VALIDATOR, MAPPER, "");

    public static AgentsServiceConfiguration read(String path)
            throws IOException, ConfigurationException {
        AgentsServiceConfigurationWrapper agentsServiceConfigurationWrapper =
                CONFIGURATION_FACTORY.build(new File(path));
        return agentsServiceConfigurationWrapper.getAgentsServiceConfiguration();
    }
}
