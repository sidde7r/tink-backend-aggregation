package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.filter;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import javax.ws.rs.core.MultivaluedMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants.Headers;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

/**
 * This filter replicates the transformation from bad responses into a HttpResponseException as
 * performed by the {@link RequestBuilder}, but with a more detailed message for SDC agents, which
 * return the error message in a special header instead of the message body. In some cases Special
 * Header is not returned but json with pin invalid message is returned.
 */
@Slf4j
public class SdcExceptionFilter extends Filter {

    private final Map<String, LoginException> messageToExceptionMapping;

    private static final Map<String, LoginException> defaultMessagesToExceptionMapping =
            new ImmutableMap.Builder<String, LoginException>()
                    .put(
                            ErrorMessage.PIN_4_CHARACTERS.getCriteria(),
                            LoginError.INCORRECT_CREDENTIALS.exception(
                                    "Your PIN has illegal characters."))
                    .put(
                            ErrorMessage.PIN_BLOCKED.getCriteria(),
                            LoginError.INCORRECT_CREDENTIALS.exception("Your PIN code is blocked."))
                    .build();

    public SdcExceptionFilter() {
        messageToExceptionMapping = defaultMessagesToExceptionMapping;
    }

    public SdcExceptionFilter(Map<String, Pair<LoginError, String>> messageToErrorReasonMapping) {
        ImmutableMap.Builder<String, LoginException> builder =
                new ImmutableMap.Builder<String, LoginException>()
                        .putAll(defaultMessagesToExceptionMapping);
        messageToErrorReasonMapping.forEach(
                (message, errorReasonPair) -> {
                    LoginError loginError = errorReasonPair.getKey();
                    String reason = errorReasonPair.getValue();
                    LoginException loginException =
                            reason == null ? loginError.exception() : loginError.exception(reason);
                    builder.put(message, loginException);
                });
        messageToExceptionMapping = builder.build();
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        HttpResponse httpResponse = nextFilter(httpRequest);
        if (httpResponse.getStatus() >= 400) {
            handleIntegrationError(httpRequest, httpResponse);
        }
        return httpResponse;
    }

    private void handleIntegrationError(HttpRequest httpRequest, HttpResponse httpResponse) {
        String headerErrorMessage = getErrorMessageFromHeader(httpResponse);
        String bodyMessage = getBody(httpResponse);
        handleKnownErrors(headerErrorMessage, bodyMessage);
        handleOtherErrors(httpRequest, httpResponse, headerErrorMessage, bodyMessage);
    }

    private void handleKnownErrors(String headerErrorMessage, String bodyMessage) {
        String errorMessage = headerErrorMessage != null ? headerErrorMessage : bodyMessage;
        Optional.ofNullable(messageToExceptionMapping.get(errorMessage))
                .ifPresent(
                        exception -> {
                            log.info("Handle known error: " + errorMessage);
                            throw exception;
                        });
    }

    private void handleOtherErrors(
            HttpRequest httpRequest,
            HttpResponse httpResponse,
            String headerErrorMessage,
            String bodyMessage) {
        String message =
                "Response statusCode: "
                        + httpResponse.getStatus()
                        + "\n    with "
                        + Headers.X_SDC_ERROR_MESSAGE
                        + " header: "
                        + headerErrorMessage
                        + " body: "
                        + bodyMessage;
        log.error("Unknown error " + message);
        throw new HttpResponseException(message, httpRequest, httpResponse);
    }

    private String getErrorMessageFromHeader(HttpResponse httpResponse) {
        MultivaluedMap<String, String> headers = httpResponse.getHeaders();
        if (headers.containsKey(Headers.X_SDC_ERROR_MESSAGE)) {
            return headers.getFirst(Headers.X_SDC_ERROR_MESSAGE);
        }
        return null;
    }

    private String getBody(HttpResponse httpResponse) {
        try {
            return httpResponse.getBody(String.class);
        } catch (HttpClientException | HttpResponseException e) {
            log.error("Couldn't getBody from response", e);
            return null;
        }
    }
}
