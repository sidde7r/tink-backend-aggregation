package se.tink.backend.common.utils;

import com.google.common.base.CaseFormat;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Provider;

// TODO: This should be moved into the classes themselves (provider.getMetricName())
public class MetricsUtils {

    /**
     * Clean graphite metrics names.
     * <p>
     * This was created because I was seeing a lot of stacktraces in Carbon log due to broken metric names.
     * 
     * @param proposal
     *            the proposed metrics' name.
     * @return cleaned metric's name
     */
    public static String cleanMetricName(String proposal) {
        return proposal.replace("'", "").replace("*", "").replace(")", "_").replace("(", "_");
    }

    @Deprecated
    public static String providerTypeName(Provider provider) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, provider.getType().name());
    }

    @Deprecated
    public static String credentialName(Credentials credentials) {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, credentials.getType().name());
    }
}
