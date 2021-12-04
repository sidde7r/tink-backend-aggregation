package se.tink.agent.sdk.operation.aggregator_configuration;

public interface AggregatorConfiguration {
    String getQwaCertificate();

    String getQsealCertificate();

    String getRedirectUrl();

    String getClientId();

    String getClientSecret();

    String getKeyId();

    <T extends NonStandardAggregatorConfiguration> T getNonStandardConfiguration(
            final Class<T> configClass);
}
