package se.tink.backend.aggregation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import javax.validation.Validation;
import javax.validation.Validator;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;

public abstract class AbstractConfigurationBase {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    protected ConfigurationFactory<AgentsServiceConfiguration> CONFIGURATION_FACTORY = new ConfigurationFactory<>(
            AgentsServiceConfiguration.class, VALIDATOR, MAPPER, "");

    protected AgentsServiceConfiguration configuration;

}
