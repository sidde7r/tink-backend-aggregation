package se.tink.backend.aggregation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import javax.validation.Validation;
import javax.validation.Validator;
import se.tink.backend.common.config.ServiceConfiguration;

public abstract class AbstractConfigurationBase {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    protected static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    protected ConfigurationFactory<ServiceConfiguration> CONFIGURATION_FACTORY = new ConfigurationFactory<>(
            ServiceConfiguration.class, VALIDATOR, MAPPER, "");

    protected ServiceConfiguration configuration;

}
