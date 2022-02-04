package se.tink.agent.runtime.operation.aggregator_configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import se.tink.agent.sdk.operation.aggregator_configuration.AggregatorConfiguration;
import se.tink.agent.sdk.operation.aggregator_configuration.NonStandardAggregatorConfiguration;

public class AggregatorConfigurationImpl implements AggregatorConfiguration {
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final String qwaCertificate;
    private final String qsealCertificate;
    private final String redirectUrl;
    private final String clientId;
    private final String clientSecret;
    private final String keyId;
    private final Map<String, Object> nonStandardConfiguration;

    public AggregatorConfigurationImpl(
            String qwaCertificate,
            String qsealCertificate,
            String redirectUrl,
            String clientId,
            String clientSecret,
            String keyId,
            Map<String, Object> nonStandardConfiguration) {
        this.qwaCertificate = qwaCertificate;
        this.qsealCertificate = qsealCertificate;
        this.redirectUrl = redirectUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.keyId = keyId;
        this.nonStandardConfiguration = nonStandardConfiguration;
    }

    @Override
    public String getQwaCertificate() {
        return this.qwaCertificate;
    }

    @Override
    public String getQsealCertificate() {
        return this.qsealCertificate;
    }

    @Override
    public String getRedirectUrl() {
        return this.redirectUrl;
    }

    @Override
    public String getClientId() {
        return this.clientId;
    }

    @Override
    public String getClientSecret() {
        return this.clientSecret;
    }

    @Override
    public String getKeyId() {
        return this.keyId;
    }

    @Override
    public <T extends NonStandardAggregatorConfiguration> T getNonStandardConfiguration(
            Class<T> configClass) {
        return OBJECT_MAPPER.convertValue(nonStandardConfiguration, configClass);
    }
}
