package se.tink.backend.aggregation.provider.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import javax.validation.Validation;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;

public class ProviderConfigurationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(ProviderConfigurationFactory.class);

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final io.dropwizard.configuration.ConfigurationFactory<ProviderServiceConfiguration> CONFIGURATION_FACTORY
            = new io.dropwizard.configuration.ConfigurationFactory<>(ProviderServiceConfiguration.class, VALIDATOR,
            MAPPER, "");

    private static ProviderServiceConfiguration configuration;

    public static synchronized ProviderServiceConfiguration get(String configPath) {
        if (configuration == null) {
            try {
                configuration = CONFIGURATION_FACTORY.build(new File(configPath));
            } catch (Exception e) {
                String errorMessage = String
                        .format("Could not load configuration %s for %s", configPath, e.getMessage());
                LOG.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
        }

        return configuration;
    }
}
