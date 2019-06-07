package se.tink.backend.aggregation.configuration;

/**
 * Exception thrown if configuration validator deduces that given configurations are no good.
 *
 * @see ConfigurationValidator
 */
public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(Exception innerException) {
        super(
                String.format("Configuration is invalid: %s", innerException.getMessage()),
                innerException);
    }
}
