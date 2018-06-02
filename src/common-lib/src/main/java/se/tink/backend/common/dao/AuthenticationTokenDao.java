package se.tink.backend.common.dao;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import se.tink.backend.common.repository.mysql.main.AuthenticationTokenRepository;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class AuthenticationTokenDao {
    private static final Seconds AUTHENTICATION_TIME_TO_LIVE = Minutes.minutes(5).toStandardSeconds();
    private static final MetricId AUTHENTICATION_TOKENS_SAVED_METRIC_ID = MetricId.newId("authentication_tokens_saved");
    private static final MetricId AUTHENTICATION_TOKENS_EXPIRED_METRIC_ID = MetricId
            .newId("authentication_tokens_expired");

    private AuthenticationTokenRepository authenticationTokenRepository;
    private MetricRegistry metricRegistry;

    @Inject
    public AuthenticationTokenDao(AuthenticationTokenRepository authenticationTokenRepository,
            MetricRegistry metricRegistry) {
        this.authenticationTokenRepository = authenticationTokenRepository;
        this.metricRegistry = metricRegistry;
    }

    public AuthenticationToken save(AuthenticationToken token) {
        token = authenticationTokenRepository.save(token);

        metricRegistry.meter(AUTHENTICATION_TOKENS_SAVED_METRIC_ID
                .label("method", token.getMethod().toString())
                .label("status", token.getStatus().toString()))
                .inc();

        return token;
    }

    public AuthenticationToken consume(String authenticationToken) throws AuthenticationTokenNotFoundException,
            AuthenticationTokenExpiredException {

        AuthenticationToken authentication = authenticationTokenRepository.findOne(authenticationToken);

        if (authentication == null) {
            throw new AuthenticationTokenNotFoundException();
        }

        // Always delete the token so that it only can be consumed once
        authenticationTokenRepository.delete(authenticationToken);

        Seconds timeSinceCreated = Seconds.secondsBetween(new DateTime(authentication.getCreated()), DateTime.now());

        if (timeSinceCreated.isGreaterThan(AUTHENTICATION_TIME_TO_LIVE)) {
            // The authentication entry has expired. Signal once to the client that it has expired.
            // Consecutive calls should yield the `AuthenticationTokenNotFoundException` above instead.
            throw new AuthenticationTokenExpiredException();
        }

        return authentication;
    }

    /**
     * Delete all the tokens that have expired.
     */
    public int deleteExpiredTokens() {
        int deleted = authenticationTokenRepository.deleteExpiredTokens(2 * AUTHENTICATION_TIME_TO_LIVE.getSeconds());

        metricRegistry.meter(AUTHENTICATION_TOKENS_EXPIRED_METRIC_ID).inc(deleted);

        return deleted;
    }
}
