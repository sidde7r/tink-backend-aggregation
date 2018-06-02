package se.tink.backend.common.admin;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.Versions;
import se.tink.backend.core.TinkUserAgent;
import se.tink.libraries.cluster.Cluster;

public class VersionsTest {
    @Test
    public void shouldUseLegacyFeed_forLegacyTinkApp() {
        TinkUserAgent userAgentIos = TinkUserAgent.of(Optional.of("Tink Mobile/2.5.30 (iOS; 11.2.6, iPhone)"));
        TinkUserAgent userAgentAndroid = TinkUserAgent.of(Optional.of("Tink Mobile/3.0.26 (Android; 8.0.0, samsung)"));
        Assert.assertEquals("Tink Mobile/2.5.30 for iOS should use legacy feed",
                false, Versions.shouldUseNewFeed(userAgentIos, Cluster.TINK));
        Assert.assertEquals("Tink Mobile/3.0.26 for Android should use legacy feed",
                false, Versions.shouldUseNewFeed(userAgentAndroid, Cluster.TINK));
    }

    @Test
    public void shouldUseLegacyFeed_forLegacyGripApp() {
        TinkUserAgent tinkUserAgent = TinkUserAgent.of(Optional.of("Tink Mobile/2.0.1 (iOS; 11.2.6, iPhone)"));
        TinkUserAgent gripUserAgent = TinkUserAgent.of(Optional.of("Grip/1.4.2 (Android; 5.0.2, samsung SM-G360F)"));
        Assert.assertEquals("Tink Mobile/2.0.1 for iOS should use legacy feed",
                false, Versions.shouldUseNewFeed(tinkUserAgent, Cluster.ABNAMRO));
        Assert.assertEquals("Grip/1.4.2 for Android should use legacy feed",
                false, Versions.shouldUseNewFeed(gripUserAgent, Cluster.ABNAMRO));
    }

    @Test
    public void shouldUseNewFeed_forNewTinkApp() {
        TinkUserAgent userAgentIos = TinkUserAgent.of(Optional.of("Tink Mobile/4.0.0 (iOS; 11.2.6, iPhone)"));
        TinkUserAgent userAgentAndroid = TinkUserAgent.of(Optional.of("Tink Mobile/4.0.0 (Android; 8.0.0, samsung)"));
        Assert.assertEquals("Tink Mobile/4.0.0 for iOS should use new feed",
                true, Versions.shouldUseNewFeed(userAgentIos, Cluster.TINK));
        Assert.assertEquals("Tink Mobile/4.0.0 for Android should use new feed",
                true, Versions.shouldUseNewFeed(userAgentAndroid, Cluster.TINK));
    }

    @Test
    public void shouldUseNewFeed_forNewGripApp() {
        TinkUserAgent userAgentIos = TinkUserAgent.of(Optional.of("Tink Mobile/3.0.0 (iOS; 11.2.6, iPhone)"));
        TinkUserAgent userAgentAndroid = TinkUserAgent.of(Optional.of("Tink Mobile/3.0.0 (Android; 8.0.0, samsung)"));
        Assert.assertEquals("Tink Mobile/3.0.0 for iOS should use new feed",
                true, Versions.shouldUseNewFeed(userAgentIos, Cluster.ABNAMRO));
        Assert.assertEquals("Tink Mobile/3.0.0 for Android should use new feed",
                true, Versions.shouldUseNewFeed(userAgentAndroid, Cluster.ABNAMRO));
    }
}
