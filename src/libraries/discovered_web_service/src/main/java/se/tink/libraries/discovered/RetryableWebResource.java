package se.tink.libraries.discovered;

import com.google.api.client.util.Sleeper;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Ticker;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryableWebResource {
    public static class Candidate implements Comparable<Candidate> {
        public static Candidate fromURI(URI uri) {
            return new Candidate(uri.getScheme(), uri.getHost(), uri.getPort());
        }

        private String host;
        private int port;

        private String scheme;

        public Candidate(String scheme, String host, int port) {
            this.scheme = scheme;
            this.host = host;
            this.port = port;
        }

        // Autogenerated by Eclipse.
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Candidate other = (Candidate) obj;
            return compareTo(other) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(host, port, scheme);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("scheme", scheme)
                    .add("host", host)
                    .add("port", port)
                    .toString();
        }

        @Override
        public int compareTo(Candidate o) {
            return ComparisonChain.start()
                    .compare(this.host, o.host)
                    .compare(this.scheme, o.scheme)
                    .compare(this.port, o.port)
                    .result();
        }
    }

    protected static class RetryFilter extends ClientFilter {

        private final List<Candidate> uris;
        private final String serviceName;
        private final Sleeper sleeper;
        private final Ticker ticker;

        // Something like 0+1+2+4+8=15 (five tries) seconds of sleeping and 14 seconds for actual
        // trying to connect
        // (total time).
        protected static final long MAX_TOTAL_TRY_TIME = TimeUnit.SECONDS.toNanos(30);

        public RetryFilter(
                String serviceName, List<Candidate> candidates, Sleeper sleeper, Ticker ticker) {
            Preconditions.checkArgument(
                    candidates.size() > 0, "Must have at least one candidate to connect to.");
            this.uris = ImmutableList.copyOf(candidates);
            this.serviceName = serviceName;
            this.sleeper = Preconditions.checkNotNull(sleeper);
            this.ticker = Preconditions.checkNotNull(ticker);
        }

        @Override
        public ClientResponse handle(ClientRequest cr) throws ClientHandlerException {
            ClientHandlerException lastException = null;
            Iterator<Candidate> it = uris.iterator();

            final long requestStarted = ticker.read();
            ExponentialBackoffPolicy retryPolicy =
                    new ExponentialBackoffPolicy(TimeUnit.SECONDS.toMillis(1));

            final URI originalURI = cr.getURI();

            // TODO: Use a Retryer here to simplify the retry code.
            do {

                // This iterator will always have a first element as we have a precondition in the
                // constructor to check
                // size of uris.
                Candidate uri = it.next();

                // Without the builder, we risk modifying the preconstructed path.
                final URI clientUri =
                        UriBuilder.fromUri(originalURI)
                                .host(uri.host)
                                .port(uri.port)
                                .scheme(uri.scheme)
                                .build();
                if (log.isTraceEnabled()) {
                    log.trace(
                            "Resolved URL for service '"
                                    + serviceName
                                    + "' to '"
                                    + clientUri
                                    + "' out of "
                                    + uris.size()
                                    + " alternatives.");
                }
                cr.setURI(clientUri);

                // Outcomes
                String errorReason = null;

                try {

                    ClientResponse response = getNext().handle(cr);
                    if (Objects.equal(
                            response.getStatusInfo().getStatusCode(),
                            Response.Status.SERVICE_UNAVAILABLE.getStatusCode())) {
                        errorReason =
                                String.format(
                                        "Service '%s' unavailable.", cr.getURI().toASCIIString());
                    } else {
                        return response;
                    }

                } catch (ClientHandlerException e) {

                    lastException = e;
                    // Reference:
                    // https://java.net/projects/jersey/lists/users/archive/2011-03/message/17
                    //
                    // Regarding SocketTimeoutException:
                    // From javadocs: Signals that a timeout has occurred on a socket read or
                    // accept.
                    // ref:
                    // https://docs.oracle.com/javase/7/docs/api/java/net/SocketTimeoutException.html
                    //
                    // This means that we can get a SocketTimeoutException whilst reading (i.e. data
                    // transfer has begun)
                    // We don't want to retry then. Only if connect failed.
                    if (e.getCause() instanceof ConnectException
                            || e.getCause() instanceof UnknownHostException
                            || e.getCause() instanceof NoRouteToHostException
                            || (e.getCause() instanceof SocketTimeoutException
                                    && e.getMessage() != null
                                    && e.getMessage().contains("connect timed out"))) {

                        errorReason =
                                String.format(
                                        "Unable to connect to '%s'.", cr.getURI().toASCIIString());
                    } else {
                        String errorMessage =
                                String.format(
                                        "Something went wrong when talking to '%s'.",
                                        cr.getURI().toASCIIString());
                        throw new ClientHandlerException(errorMessage, e);
                    }
                }

                // If we came here we know 1) an exception was caught and 2) it wasn't rethrown and
                // that errorReason
                // must have been set:

                Preconditions.checkState(errorReason != null);

                if (it.hasNext()) {

                    final long elapsedTime = ticker.read() - requestStarted;
                    final long sleepInterval = retryPolicy.getSleepDuration();

                    if ((elapsedTime + sleepInterval) > MAX_TOTAL_TRY_TIME) {
                        log.warn(errorReason);
                        throw contructClientHandlerException(
                                "Max retry time would be exceeded. Bailing.",
                                Optional.ofNullable(lastException));
                    }

                    log.warn(
                            "{} Sleeping for {} ms, then retrying another instance...",
                            errorReason,
                            sleepInterval);
                    try {
                        sleeper.sleep(sleepInterval);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(
                                "Retry sleep was interrupted. Quitting retry loop.", e);
                    }

                } else {

                    log.warn(errorReason);
                }

                // Using do-while mostly to not allow (ticker.read() - requestStarted) become too
                // big for
                // first loop.
            } while (it.hasNext() && (ticker.read() - requestStarted) < MAX_TOTAL_TRY_TIME);

            throw contructClientHandlerException(
                    "Retries exhausted. Last exception is attached as cause.",
                    Optional.ofNullable(lastException));
        }

        private ClientHandlerException contructClientHandlerException(
                String message, Optional<ClientHandlerException> cause) {
            if (cause.isPresent()) {
                return new ClientHandlerException(message, cause.get());
            } else {
                return new ClientHandlerException(message);
            }
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RetryableWebResource.class);

    public static void decorate(
            String serviceName,
            List<Candidate> candidates,
            WebResource resource,
            Sleeper sleeper,
            Ticker ticker) {

        resource.addFilter(new RetryFilter(serviceName, candidates, sleeper, ticker));
    }

    /**
     * Decorate the a {@link WebResource} to try a list of candidates on {@link ConnectException} or
     * {@link UnknownHostException}.
     *
     * @param resource the resource to be decorated.
     * @param candidates the list of alternatives in the order of which they should be tried in.
     *     Note that each fallback is using the same path from <code>resource</code>. Only scheme,
     *     host and port will be modified.
     * @param serviceName the name of the service. Only used for logging.
     */
    public static void decorate(
            WebResource resource, List<Candidate> candidates, String serviceName) {

        decorate(serviceName, candidates, resource, Sleeper.DEFAULT, Ticker.systemTicker());
    }
}
