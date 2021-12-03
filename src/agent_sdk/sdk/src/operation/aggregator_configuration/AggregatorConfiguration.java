package se.tink.agent.sdk.operation.aggregator_configuration;

import java.util.Optional;

public interface AggregatorConfiguration {
    String getQwaCertificate();

    String getQsealCertificate();

    // TODO: should these return `Optional<T>` or `T` + blow up if not set? (the second option is
    // how it works today)
    Optional<String> getRedirectUrl();

    Optional<String> getClientId();

    Optional<String> getClientSecret();

    Optional<String> getKeyId();

    // TODO: should return `Optional<T>` or `T` + blow up if not set? (the second option is how it
    // works today)
    <T extends NonStandardAggregatorConfiguration> T getNonStandardConfiguration(
            final Class<T> configClass);
}
