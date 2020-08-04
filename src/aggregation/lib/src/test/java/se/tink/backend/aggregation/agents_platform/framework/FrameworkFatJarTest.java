package se.tink.backend.aggregation.agents_platform.framework;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public final class FrameworkFatJarTest {

    @Test
    public void thirdPartyLibraryClassInFatJarIsRelocated() {
        Assertions.assertThatCode(
                        () ->
                                Class.forName(
                                        "agents_platform_framework.com.fasterxml.jackson.core.io.CharTypes"))
                .doesNotThrowAnyException();
    }
}
