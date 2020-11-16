package se.tink.backend.aggregation.agents_platform.agents_framework;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public final class AgentsFrameworkFatJarTest {

    @Test
    public void thirdPartyLibraryClassInFatJarIsRelocated() {
        Assertions.assertThatCode(
                        () ->
                                Class.forName(
                                        "agents_platform_agents_framework.com.github.tomakehurst.wiremock.client.WireMock"))
                .doesNotThrowAnyException();
    }
}
