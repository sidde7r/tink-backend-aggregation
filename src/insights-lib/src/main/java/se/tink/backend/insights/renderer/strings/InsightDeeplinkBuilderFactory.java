package se.tink.backend.insights.renderer.strings;

import com.google.inject.Inject;
import se.tink.backend.common.utils.DeepLinkBuilderFactory;

/**
 * A deeplink builder for the Actionable Insights MVP
 */
public class InsightDeeplinkBuilderFactory extends DeepLinkBuilderFactory {

    private final String prefix;

    @Inject
    public InsightDeeplinkBuilderFactory() {
        super("tink://");
        this.prefix = "tink://";
    }

    public class Builder extends DeepLinkBuilderFactory.Builder {
        private StringBuilder deepLink;
        private boolean haveAppendedParameters = false;

        public Builder(String link) {
            super(link);
            this.deepLink = new StringBuilder(prefix + link);
        }

        public Builder withInsightId(String id) {
            if (id != null) {
                this.deepLink.append(queryParameter("aai_id", id));
            }
            return this;
        }

        public Builder withOptionKey(String key) {
            if (key != null) {
                this.deepLink.append(queryParameter("aai_option", key));
            }
            return this;
        }

        private String queryParameter(String name, String value) {
            String separator = haveAppendedParameters ? "&" : "/?";
            haveAppendedParameters = true;
            return separator + name + "=" + value;
        }

        public String build() {
            return this.deepLink.toString();
        }
    }

    public Builder setBaseDeeplink(String type, String id) {
        return new Builder(String.format(type, id));
    }
}
