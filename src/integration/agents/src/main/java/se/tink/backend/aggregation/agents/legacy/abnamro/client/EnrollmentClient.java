package se.tink.backend.aggregation.agents.abnamro.client;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.ErrorResponse;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment.CollectEnrollmentResponse;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment.InitiateEnrollmentRequest;
import se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment.InitiateEnrollmentResponse;
import se.tink.backend.aggregation.configuration.integrations.abnamro.AbnAmroEnrollmentConfiguration;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class EnrollmentClient {
    private final AbnAmroEnrollmentConfiguration enrollmentConfiguration;
    private final Client client;

    private final MetricId ABNAMRO_ENROLLMENT_CREATE_CLIENT_LATENCY =
            MetricId.newId("abnamro_enrollment_create_client_latency");
    private final MetricId ABNAMRO_ENROLLMENT_COLLECT_CLIENT_LATENCY =
            MetricId.newId("abnamro_enrollment_collect_client_latency");
    private MetricRegistry metricRegistry;

    public EnrollmentClient(Client client, AbnAmroEnrollmentConfiguration enrollmentConfiguration,
            MetricRegistry metricRegistry) {
        Preconditions.checkState(!Strings.isNullOrEmpty(enrollmentConfiguration.getUrl()),
                "Url must not be null or empty.");
        Preconditions.checkState(!Strings.isNullOrEmpty(enrollmentConfiguration.getApiKey()),
                "Api key must not be null or empty.");
        Preconditions.checkNotNull(client, "Client must not be null.");

        this.enrollmentConfiguration = enrollmentConfiguration;
        this.client = client;
        this.metricRegistry = metricRegistry;
    }

    /**
     * Initiate a new enrollment with the specified phone number.
     */
    public InitiateEnrollmentResponse initiate(String phoneNumber) {
        Preconditions.checkState(!Strings.isNullOrEmpty(phoneNumber), "Phone number must not be null or empty.");

        InitiateEnrollmentRequest request = new InitiateEnrollmentRequest();
        request.setPhoneNumber(phoneNumber);

        Timer.Context responseTimer = metricRegistry.timer(ABNAMRO_ENROLLMENT_CREATE_CLIENT_LATENCY).time();
        ClientResponse response = createClientRequest("/userenrolments").post(ClientResponse.class, request);
        responseTimer.stop();

        validateResponse(response);

        return response.getEntity(InitiateEnrollmentResponse.class);
    }

    /**
     * Collect the current status of the enrollment.
     */
    public CollectEnrollmentResponse collect(String token) {
        Preconditions.checkState(!Strings.isNullOrEmpty(token), "Token must not be null or empty.");

        final String path = String.format("/userenrolments?uuid=%s", token);

        Timer.Context responseTimer = metricRegistry.timer(ABNAMRO_ENROLLMENT_COLLECT_CLIENT_LATENCY).time();
        ClientResponse response = createClientRequest(path).get(ClientResponse.class);
        responseTimer.stop();

        validateResponse(response);

        return response.getEntity(CollectEnrollmentResponse.class);
    }

    private void validateResponse(ClientResponse response) {
        if (response.getStatus() == ClientResponse.Status.OK.getStatusCode()) {
            return;
        }

        if (Objects.equals(response.getType(), MediaType.APPLICATION_JSON_TYPE)) {
            ErrorResponse errorResponse = response.getEntity(ErrorResponse.class);

            if (errorResponse.getMessages() != null && !errorResponse.getMessages().isEmpty()) {
                ErrorEntity error = errorResponse.getMessages().get(0);

                throw new RuntimeException(String.format("Enrollment service error (Key = '%s', Reason = '%s')",
                        error.getMessageKey(), error.getReason()));
            }
        }

        response.close();
        throw new RuntimeException(String.format("Enrollment service error (Status = '%d')", response.getStatus()));
    }

    private WebResource.Builder createClientRequest(String queryString) {
        return client.resource(enrollmentConfiguration.getUrl() + queryString)
                .type(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .header("X-Api-Key", enrollmentConfiguration.getApiKey());
    }
}
