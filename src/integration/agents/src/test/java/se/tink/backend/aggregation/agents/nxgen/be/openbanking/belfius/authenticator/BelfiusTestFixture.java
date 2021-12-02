package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.authenticator;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import lombok.SneakyThrows;
import org.junit.Ignore;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.agentfactory.utils.ProviderReader;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration.BelfiusConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.user.rpc.User;

@Ignore
public class BelfiusTestFixture {

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/be/openbanking/belfius/authenticator/resources/";

    User belfiusUser() {
        return fileContent("belfius_user_template.json", User.class);
    }

    Provider providerConfiguration() {
        return new ProviderReader()
                        .getProviderConfigurations(
                                "external/tink_backend/src/provider_configuration/data/seeding")
                        .stream()
                        .findFirst()
                        .orElseThrow(RuntimeException::new);
    }

    UserAvailability userAvailability() {
        UserAvailability userAvailability = new UserAvailability();
        userAvailability.setUserPresent(true);
        userAvailability.setUserAvailableForInteraction(true);
        userAvailability.setOriginatingUserIp("127.0.0.1");
        return userAvailability;
    }

    AgentConfiguration<BelfiusConfiguration> belfiusAgentConfiguration() {
        BelfiusConfiguration belfiusConfiguration =
                fileContent("belfius_configuration.json", BelfiusConfiguration.class);
        return new AgentConfiguration.Builder<BelfiusConfiguration>()
                .setProviderSpecificConfiguration(belfiusConfiguration)
                .setRedirectUrl("https://test.tink.com")
                .build();
    }

    Credentials belfiusCredentials() {
        return fileContent("belfius_credentials.json", Credentials.class);
    }

    @SneakyThrows
    <T> T fileContent(String fileName, Class<T> className) {
        String consentResponse =
                new String(
                        Files.readAllBytes(Paths.get(RESOURCES_PATH).resolve(fileName)),
                        StandardCharsets.UTF_8);
        return SerializationUtils.deserializeFromString(consentResponse, className);
    }

    Date sessionExpiryDate() {
        return new Date(new ConstantLocalDateTimeSource().getSystemCurrentTimeMillis());
    }
}
