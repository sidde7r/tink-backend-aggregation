package se.tink.backend.product.execution.integration.configurations;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import javax.validation.Validation;
import javax.validation.Validator;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;

public class ConfigurationFactory {
    private static final LogUtils LOG = new LogUtils(ConfigurationFactory.class);

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final ObjectMapper MAPPER = Jackson.newObjectMapper();
    private static final io.dropwizard.configuration.ConfigurationFactory<ProductExecutorConfiguration> CONFIGURATION_FACTORY
            = new io.dropwizard.configuration.ConfigurationFactory<>(ProductExecutorConfiguration.class, VALIDATOR,
            MAPPER, "");

    private static ProductExecutorConfiguration configuration;

    public static synchronized ProductExecutorConfiguration get(String configPath) {
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
