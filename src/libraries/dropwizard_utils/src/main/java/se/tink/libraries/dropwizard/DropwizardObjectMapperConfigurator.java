package se.tink.libraries.dropwizard;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Bootstrap;

/**
 * This class is a hacky way to force Dropwizard to not fail on unknown properties in configuration
 * which we need to be able to maintain forward compatibility: older versions of applications should
 * work with newer versions of the configuration. The root cause of the problem is that the object
 * mapper feature is enabled unconditionally without offering any easy ways to override it in:
 * <href>https://github.com/dropwizard/dropwizard/blob/901a459af922491bb556d62e046d1475760fb8ce/dropwizard-configuration/src/main/java/io/dropwizard/configuration/ConfigurationFactory.java#L60</href>
 * The situation is improved in the newer version of Dropwizard see
 * <href>https://github.com/dropwizard/dropwizard/pull/1677</href> for example. Please use a simpler
 * way to configure object mapper and remove the code when Dropwizard is upgraded.
 */
public class DropwizardObjectMapperConfigurator {

    private static class ObjectMapperProxy extends ObjectMapper {

        private ObjectMapperProxy(ObjectMapper objectMapper) {
            super(objectMapper);
        }

        private ObjectMapperProxy(ObjectMapperProxy proxy) {
            super(proxy);
        }

        /**
         * Denies enabling {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES} without
         * affecting enabling all the other calls.
         */
        @Override
        public ObjectMapper enable(DeserializationFeature feature) {
            // do not allow Dropwizard to enable deserialization properties
            if (!feature.equals(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)) {
                super.enable(feature);
            }
            return this;
        }

        /**
         * Copy must to be overwritten in each subclass to be supported. And {@link
         * ConfigurationFactory} requires support for copying.
         *
         * @return a copy of the object mapper
         */
        @Override
        public ObjectMapper copy() {
            return new ObjectMapperProxy(this);
        }
    }

    public static void doNotFailOnUnknownProperties(Bootstrap<?> bootstrap) {
        bootstrap.setConfigurationFactoryFactory(
                (klass, validator, objectMapper, propertyPrefix) -> {
                    objectMapper.configure(
                            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    // Create a proxy that doesn't allow enabling deserialization properties so the
                    // feature
                    // is not enabled back.
                    ObjectMapperProxy objectMapperProxy = new ObjectMapperProxy(objectMapper);
                    return new ConfigurationFactory<>(
                            klass, validator, objectMapperProxy, propertyPrefix);
                });
    }

    public static ObjectMapper newUnknownPropertiesSafeObjectMapper() {
        return new ObjectMapperProxy(
                Jackson.newObjectMapper()
                        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES));
    }
}
