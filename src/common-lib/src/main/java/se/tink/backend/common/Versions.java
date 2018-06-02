package se.tink.backend.common;

import se.tink.backend.core.TinkUserAgent;
import se.tink.libraries.cluster.Cluster;

public class Versions {
    public class Android {
        public static final String NewFeed = "4.0.0";
        public static final String AbnAmroNewFeed = "3.0.0";
    }

    public class Ios {
        public static final String NewFeed = "4.0.0";
        public static final String AbnAmroNewFeed = "3.0.0";
    }

    public static boolean shouldUseNewFeed(TinkUserAgent userAgent, Cluster cluster) {
        if (cluster == Cluster.ABNAMRO) {
            return userAgent.hasValidVersion(Ios.AbnAmroNewFeed, Android.AbnAmroNewFeed);
        }
        return userAgent.hasValidVersion(Ios.NewFeed, Android.NewFeed);
    }
}
