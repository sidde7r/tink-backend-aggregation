package se.tink.backend.aggregation.agents.abnamro.client;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.WebResource.Builder;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;

/**
 * In the context of ABN AMRO, the "IB" prefix stands for Internet Banking. The IB endpoints are the
 * same as they use for their Internet banking services, such as on the web or in their app.
 */
public class IBClient extends Client {

    private static final String SERVICE_VERSION_HEADER = "x-aab-serviceversion";
    private static final String SESSION_COOKIE_NAME = "SMSession";
    private static final String DEFAULT_LANGUAGE = "en";

    protected IBClient(
            Class<? extends Client> cls,
            AbnAmroConfiguration abnAmroConfiguration,
            MetricRegistry metricRegistry) {
        super(
                cls,
                abnAmroConfiguration.getTrustStoreConfiguration(),
                abnAmroConfiguration.getInternetBankingConfiguration().getHost());

        abnAmroConfiguration.getInternetBankingConfiguration().getProducts(); // Remove maybe

        metricRegistry.meter(MetricId.newId("ib_client_authenticate_errors")); // Remove maybe
    }

    public class IBClientRequestBuilder {

        private Builder builder;
        private String sessionToken;
        private String language;
        private String serviceVersion;

        IBClientRequestBuilder(String path) {
            this.builder = createClientRequest(path);
        }

        IBClientRequestBuilder withSession(String sessionToken) {
            this.sessionToken = sessionToken;
            return this;
        }

        Builder build() {
            builder =
                    builder.header(
                            "Accept-Language",
                            Strings.isNullOrEmpty(language) ? DEFAULT_LANGUAGE : language);

            if (!Strings.isNullOrEmpty(serviceVersion)) {
                builder = builder.header(SERVICE_VERSION_HEADER, serviceVersion);
            }

            if (!Strings.isNullOrEmpty(sessionToken)) {
                builder = builder.cookie(new NewCookie(SESSION_COOKIE_NAME, sessionToken));
            }

            return builder;
        }
    }
}
