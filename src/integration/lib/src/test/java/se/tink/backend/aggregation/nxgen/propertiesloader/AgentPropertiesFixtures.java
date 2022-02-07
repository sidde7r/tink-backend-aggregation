package se.tink.backend.aggregation.nxgen.propertiesloader;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.junit.Ignore;

@Ignore
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AgentPropertiesFixtures {

    static final String PROPERTIES_LOADER_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/propertiesloader";

    static final String PROPERTIES_RESOURCE_PATH = PROPERTIES_LOADER_PATH + "/resources";

    static void assertPropertiesAreEqualToExpectedValues(AgentPropertiesTestEntity properties) {
        assertThat(properties.getApiVersion()).isEqualTo("v3");
        assertThat(properties.getEnv()).isEqualTo("prod");
        assertThat(properties.getCertainDate())
                .isEqualTo(LocalDateTime.of(2019, 4, 17, 10, 15, 30));
        assertThat(properties.getUrlList().size()).isEqualTo(2);
    }
}
