package se.tink.backend.aggregation.agents.abnamro.client;

import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource.Builder;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.InternetBankingUnavailableException;
import se.tink.backend.aggregation.agents.abnamro.client.exceptions.UnauthorizedAccessException;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;
import se.tink.backend.aggregation.agents.abnamro.client.model.SessionEntity;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.AuthenticationRequest;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.SessionResponse;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroConfiguration;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroProductsConfiguration;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.metrics.types.timers.Timer;

/**
 * In the context of ABN AMRO, the "IB" prefix stands for Internet Banking. The IB endpoints are the
 * same as they use for their Internet banking services, such as on the web or in their app.
 */
public class IBClient extends Client {

    private static final String SERVICE_VERSION_HEADER = "x-aab-serviceversion";
    private static final String SESSION_COOKIE_NAME = "SMSession";
    private static final Joiner COMMA_JOINER = Joiner.on(",").skipNulls();
    private static final String DEFAULT_LANGUAGE = "en";
    private static final MetricId AUTHENTICATION_OUTCOME_METRIC =
            MetricId.newId("ib_client_authentication_outcome");

    private final AbnAmroProductsConfiguration productsConfiguration;
    private final Counter authenticationErrors;
    private final MetricRegistry metricRegistry;

    private static class USER_PREFERENCE_IDS {
        private static String CREDIT_CARDS = "creditCards";
    }

    private static class USER_PREFERENCE_FIELDS {
        private static String APPROVED_CREDIT_CARDS = "hasGivenApprovalCreditCards";
    }

    protected IBClient(
            Class<? extends Client> cls,
            AbnAmroConfiguration abnAmroConfiguration,
            MetricRegistry metricRegistry) {
        super(
                cls,
                abnAmroConfiguration.getTrustStoreConfiguration(),
                abnAmroConfiguration.getInternetBankingConfiguration().getHost());

        this.productsConfiguration =
                abnAmroConfiguration.getInternetBankingConfiguration().getProducts();

        this.metricRegistry = metricRegistry;
        this.authenticationErrors =
                metricRegistry.meter(MetricId.newId("ib_client_authenticate_errors"));
    }

    private Timer getTimer(String source, String outcome) {
        return metricRegistry.timer(
                AUTHENTICATION_OUTCOME_METRIC.label("source", source).label("outcome", outcome));
    }

    public boolean authenticate(AuthenticationRequest request) {
        if (Strings.isNullOrEmpty(request.getBcNumber())) {
            log.error("Invalid request: missing bc number.");
            return false;
        }

        try {
            final Stopwatch watch = Stopwatch.createStarted();

            Optional<String> bcNumber = getCustomerNumber(request.getSessionToken());

            watch.stop();

            // No BC number in response, hence nothing to compare.
            if (!bcNumber.isPresent()) {
                log.error(String.format("Invalid response data for %s.", request.getBcNumber()));
                getTimer("authenticate", "no_bc_number")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return false;
            }

            if (request.getBcNumber().equals(bcNumber.get())) {
                log.debug(String.format("Successfully authenticated %s.", request.getBcNumber()));
                getTimer("authenticate", "success")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return true;
            } else {
                log.error(
                        String.format(
                                "Customer numbers don't match. Requested %s but received %s.",
                                request.getBcNumber(), bcNumber.get()));
                getTimer("authenticate", "customer_mismatch")
                        .update(watch.elapsed(TimeUnit.NANOSECONDS), TimeUnit.NANOSECONDS);
                return false;
            }
        } catch (Exception e) {
            authenticationErrors.inc();
            log.error(String.format("Failed to authenticate %s.", request.getBcNumber()), e);
            return false;
        }
    }

    public Optional<String> getCustomerNumber(String sessionToken)
            throws InternetBankingUnavailableException, UnauthorizedAccessException {
        ClientResponse sessionClientResponse =
                new IBClientRequestBuilder("/session")
                        .withServiceVersion("v3")
                        .withSession(sessionToken)
                        .build()
                        .get(ClientResponse.class);

        if (sessionClientResponse.getStatus() == Status.OK.getStatusCode()) {
            SessionResponse sessionResponse =
                    sessionClientResponse.getEntity(SessionResponse.class);
            SessionEntity session = sessionResponse.getSession();

            String bcNumber = session.getRepresentedCustomer();

            // This logic does not match the implementation on the clients and all sessions should
            // have a
            // represented customer. Will remove the below code when we are sure it isn't used.
            // /Erik
            if (Strings.isNullOrEmpty(bcNumber)) {
                log.warn(
                        "Could not retrieve 'represented customer'. Fallback to 'selected customer'.");
                bcNumber = session.getSelectedCustomer();
            }

            return Optional.ofNullable(bcNumber);
        } else if (sessionClientResponse.getStatus() == Status.UNAUTHORIZED.getStatusCode()) {
            throw new UnauthorizedAccessException();
        } else {
            ErrorResponse errorResponse = null;

            if (hasValidContentType(sessionClientResponse, MediaType.APPLICATION_JSON_TYPE)) {
                errorResponse = sessionClientResponse.getEntity(ErrorResponse.class);
            } else {
                sessionClientResponse.close();
            }

            String status = getStatusMessage(errorResponse);

            throw new InternetBankingUnavailableException(
                    String.format(
                            "Could not call session service (Status = '%s', Message = '%s').",
                            sessionClientResponse.getStatus(), status));
        }
    }

    private static String getStatusMessage(ErrorResponse errorResponse) {
        if (errorResponse == null) {
            return null;
        }

        if (errorResponse.getMessages() == null || errorResponse.getMessages().isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();

        for (ErrorEntity me : errorResponse.getMessages()) {
            sb.append("type:");
            sb.append(me.getMessageType());
            sb.append(", ");
            sb.append("key:");
            sb.append(me.getMessageKey());
            sb.append(", ");
            sb.append("text:");
            sb.append(me.getMessageText());
        }

        return sb.toString();
    }

    private boolean hasValidContentType(ClientResponse response, MediaType expected) {

        if (Objects.equals(response.getType(), expected)) {
            return true;
        }

        log.error(
                String.format(
                        "Unexpected content type(Expected = '%s', Received = '%s')",
                        expected, response.getType()));

        return false;
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

        IBClientRequestBuilder withServiceVersion(String serviceVersion) {
            this.serviceVersion = serviceVersion;
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
