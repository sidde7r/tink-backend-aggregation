package se.tink.libraries.jersey.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.sun.jersey.spi.container.CachedEntityContainerRequest;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.accesslogging.AccessLoggingRequestDetails;
import se.tink.libraries.accesslogging.AccessLoggingUtils;
import se.tink.libraries.api.headers.TinkHttpHeaders;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class AccessLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Context private HttpServletRequest servletRequest;

    private static final Logger log = LoggerFactory.getLogger(AccessLoggingFilter.class);
    private static final Splitter SPLITTER_SPACE = Splitter.on(CharMatcher.WHITESPACE);
    private static final Splitter SPLITTER_SLASH = Splitter.on("/");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ImmutableSet<String> PATHS_TO_LOG_ENTITY = ImmutableSet.of("transfers");

    private static final String requestStopwatchKey = "requestStopwatch";

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if (request.getProperties().containsKey(requestStopwatchKey)) {
            log.warn("Filter already applied to request");
        } else {
            request.getProperties().put(requestStopwatchKey, Stopwatch.createStarted());
        }
        return request;
    }

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {

        String httpMethod = request.getMethod();

        String remoteHost = request.getHeaderValue("X-Forwarded-For");
        if (Strings.isNullOrEmpty(remoteHost)) {
            remoteHost = this.servletRequest.getRemoteAddr();
        }

        String requestString =
                String.format(
                        "\"%s %s\"",
                        StringUtils.defaultString(httpMethod),
                        StringUtils.defaultString(request.getRequestUri()));

        String responseTimeString = "-";
        Object maybeStopwatch = request.getProperties().get(requestStopwatchKey);
        if (maybeStopwatch instanceof Stopwatch) {
            Stopwatch requestStopwatch = (Stopwatch) maybeStopwatch;
            if (requestStopwatch.isRunning()) {
                responseTimeString = Long.toString(requestStopwatch.elapsed(TimeUnit.MILLISECONDS));
            } else {
                log.warn("Stopwatch is not running");
            }
        }

        List<String> oauthClients =
                request.getRequestHeader(TinkHttpHeaders.OAUTH_CLIENT_ID_HEADER_NAME);

        AccessLoggingRequestDetails.AccessLoggingCommandBuilder accessLoggingCommandBuilder =
                AccessLoggingRequestDetails.builder()
                        .setRemoteHost(remoteHost)
                        .setRequestString(requestString)
                        .setResponseStatus(Integer.toString(response.getStatus()))
                        .setUserAgent(
                                StringUtils.defaultString(
                                        request.getHeaderValue(ContainerRequest.USER_AGENT)))
                        .setResponseTimeString(responseTimeString)
                        .setUserId(Objects.toString(request.getProperties().get("userId"), null))
                        .setOauthClientId(
                                oauthClients == null
                                        ? null
                                        : oauthClients.stream().collect(Collectors.joining(",")));

        if (request.getRequestHeaders().containsKey("Authorization")) {
            List<String> authHeaders = request.getRequestHeader("Authorization");
            for (String authHeader : authHeaders) {

                Iterable<String> headerParts = SPLITTER_SPACE.split(authHeader);

                if (Iterables.size(headerParts) > 1) {
                    HttpAuthenticationMethod method =
                            HttpAuthenticationMethod.fromMethod(Iterables.get(headerParts, 0));

                    accessLoggingCommandBuilder.setHttpAuthenticationMethod(method);
                    if (method == HttpAuthenticationMethod.SESSION) {
                        accessLoggingCommandBuilder.setSessionId(Iterables.get(headerParts, 1));
                    }
                }
            }
        }

        // Log body for listed end points.

        String path = request.getPath();

        Iterable<String> urlParts = SPLITTER_SLASH.split(path);

        if (Iterables.size(urlParts) > 0) {

            boolean isPostOrPut = ("POST".equals(httpMethod) || "PUT".equals(httpMethod));

            // Special case for applications where we only want the submit form to be logged
            boolean shouldLogEntity =
                    PATHS_TO_LOG_ENTITY.contains(Iterables.get(urlParts, 0))
                            || (path.startsWith("applications") && path.endsWith("form"));

            if (shouldLogEntity && isPostOrPut) {
                ContainerRequest cachedEntityContainerRequest =
                        new CachedEntityContainerRequest(response.getContainerRequest());
                accessLoggingCommandBuilder.setBody(
                        writeValueAsString(cachedEntityContainerRequest.getEntity(Object.class)));
            }
        }

        AccessLoggingUtils.log(accessLoggingCommandBuilder.build());

        return response;
    }

    private String writeValueAsString(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.warn("Cannot serialize request body to JSON");
            return "";
        }
    }
}
