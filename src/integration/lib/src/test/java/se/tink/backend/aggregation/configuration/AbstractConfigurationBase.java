package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import javax.validation.Validation;
import javax.validation.Validator;

public abstract class AbstractConfigurationBase {

    private static final Validator VALIDATOR =
            Validation.buildDefaultValidatorFactory().getValidator();
    protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    protected ConfigurationFactory<AgentsServiceConfigurationWrapper> CONFIGURATION_FACTORY =
            new ConfigurationFactory<>(
                    AgentsServiceConfigurationWrapper.class, VALIDATOR, MAPPER, "");

    protected AgentsServiceConfiguration configuration;
}
