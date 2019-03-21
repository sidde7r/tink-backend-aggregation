package se.tink.backend.nasa.metrics.utils;

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

}
